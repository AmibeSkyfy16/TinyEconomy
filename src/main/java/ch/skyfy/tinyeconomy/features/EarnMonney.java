package ch.skyfy.tinyeconomy.features;

import ch.skyfy.tinyeconomy.Configurator;
import ch.skyfy.tinyeconomy.database.Database;
import me.bymartrixx.playerevents.api.event.PlayerKillEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.text.Text;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.skyfy.tinyeconomy.TinyEconomy.MOD_CONFIG_DIR;

public class EarnMonney {

    private static class EarnMonneyHolder {
        public static final EarnMonney INSTANCE = new EarnMonney();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static EarnMonney getInstance() {
        return EarnMonney.EarnMonneyHolder.INSTANCE;
    }

    private final Integer[] priceAlerts = new Integer[]{100, 500, 1000, 2000, 5000, 10000, 15000, 20000, 50000, 100000};

    private final Map<String, List<Integer>> priceAlertsDone;

    public EarnMonney() {
        this.priceAlertsDone = loadData();
        initialize();
    }

    public void initialize() {
        earnMinedBlock();
        earnKillingEntity();
        alertPlayer();
    }

    /**
     * The player will receive an alert when they reach certain prizes
     */
    @SuppressWarnings("deprecation")
    private void alertPlayer() {
        Economy.EconomyEvent.DEPOSIT.register((uuid, amount) -> {
            var gameInstance = FabricLoader.getInstance().getGameInstance();
            if (gameInstance == null) return;
            if (gameInstance instanceof MinecraftDedicatedServer minecraftDedicatedServer) {
                var player = minecraftDedicatedServer.getPlayerManager().getPlayer(UUID.fromString(uuid));
                if (player == null) return;
                for (var priceAlert : priceAlerts) {
                    priceAlertsDone.compute(uuid, (s, list) -> {
                        if (list == null) list = new ArrayList<>();
                        if (!list.contains(priceAlert)) {
                            var playerBalance = Economy.getInstance().getBalance(uuid);
                            if (playerBalance == null) return list;
                            if (playerBalance >= priceAlert) {
                                MySounds.playSound(player, MySounds.PRICE_ALERT_ID.getPath(), 1f, 1f);
                                player.sendMessage(Text.of("Congratulations, your capacity is now over" + priceAlert + " Kukukiol"), false);
                                list.add(priceAlert);
                            }
                        }
                        return list;
                    });
                    saveData();
                }
            }
        });
    }

    /**
     * Player will earn monney when mining blocks
     */
    private void earnMinedBlock() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            var cancel = new AtomicBoolean(false);
            // Cancel earning if player has silk touch
            player.getItemsHand().iterator().forEachRemaining(itemStack -> {
                EnchantmentHelper.get(itemStack).forEach((enchantment, integer) -> {
                    if (enchantment.getTranslationKey().equals("enchantment.minecraft.silk_touch")) cancel.set(true);
                });
            });

            if (cancel.get()) return true;

            for (var entry : Configurator.getInstance().earnData.blockMinedRewards.entrySet()) {
                var translationKey = entry.getKey();
                var price = entry.getValue();
                if (state.getBlock().getTranslationKey().equals(translationKey)) {
                    Economy.getInstance().deposit(player.getUuidAsString(), price);
                    Database.getInstance().blockMinedTransaction(player.getUuidAsString(), state.getBlock().getTranslationKey(), price);
                }
            }
            return true;
        });
    }

    /**
     * Player will earn monney when killing entities
     */
    private void earnKillingEntity() {
        PlayerKillEntityCallback.EVENT.register((player, killedEntity) -> {
            if (killedEntity instanceof HostileEntity) {
                Configurator.getInstance().earnData.entityRewards.forEach((translationKey, price) -> {
                    if (killedEntity.getType().getTranslationKey().equalsIgnoreCase(translationKey)) {
                        Economy.getInstance().deposit(player.getUuidAsString(), price);
                        Database.getInstance().entityKilledTransaction(player.getUuidAsString(), killedEntity.getType().getTranslationKey(), price);
                    }
                });
            }
        });
    }

    /**
     * Save alert that player received
     */
    private void saveData() {
        try {
            var so = new ObjectOutputStream(new FileOutputStream(MOD_CONFIG_DIR.resolve("priceAlertPlayers").toFile()));
            so.writeObject(priceAlertsDone);
            so.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * load alert that player received
     */
    private static Map<String, List<Integer>> loadData() {
        Map<String, List<Integer>> priceAlertsDone = null;
        try {
            var dataFile = MOD_CONFIG_DIR.resolve("priceAlertPlayers").toFile();
            if (!dataFile.exists()) return new HashMap<>();
            var so = new ObjectInputStream(new FileInputStream(MOD_CONFIG_DIR.resolve("priceAlertPlayers").toFile()));
            //noinspection unchecked
            priceAlertsDone = (Map<String, List<Integer>>) so.readObject();
            so.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return priceAlertsDone == null ? new HashMap<>() : priceAlertsDone;
    }
}

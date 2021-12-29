package ch.skyfy.tinyeconomy.features;

import ch.skyfy.tinyeconomy.database.Database;
import ch.skyfy.tinyeconomy.utils.JsonUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import static ch.skyfy.tinyeconomy.TinyEconomy.MOD_CONFIG_DIR;

// WHAT IS THIS CODE WTF
// On traite l'action du joueur (la transaction s'il a cliqué sur le panneau / ou on l'empêche d'ouvrir le barrel afin qu'il ne puisse pas voler)
// si l'acheteur est le vendeur, alors oui il pourra ouvrir
//        var shopResult = isAShop(hitResult.getBlockPos(), world, player, UseBlockCallback.class);
//        var blockEn = world.getBlockEntity(hitResult.getBlockPos());
//        if (blockEn != null)
//            if (blockEn.getType() == BlockEntityType.BARREL && shopResult.isShop() && !shopResult.vendorName().equals(player.getName().asString()))
//                return ActionResult.CONSUME;

public class Economy {

    private static class EconomyHolder {
        public static final Economy INSTANCE = new Economy();
    }

    public static Economy getInstance() {
        return Economy.EconomyHolder.INSTANCE;
    }

    public enum Result {
        SUCCESS,
        IOEXCEPTION,
        NO_ENOUGH_MONNEY()
    }

    public Result deposit(String uuid, double amount) {
        if(Database.getInstance().deposit(uuid, amount)){
            EconomyEvent.DEPOSIT.invoker().deposit(uuid, amount);
            return Result.SUCCESS;
        }
        return Result.IOEXCEPTION;
    }

    public Result withdraw(String uuid, double amount) {
        return Database.getInstance().withdraw(uuid, amount);
    }

    public Float getBalance(String uuid){
        return Database.getInstance().getBalance(uuid);
    }

//    public Result old_withdraw(String uuid, double amount) {
//        var playersMonneyFile = MOD_CONFIG_DIR.resolve("PlayersMonney.json").toFile();
//        var jsonUtils = new JsonUtils();
//
//        try {
//            var jsonObject = jsonUtils.readOrCreateNew(playersMonneyFile);
//            if (jsonObject.get(uuid) != null) {
//                var el = jsonObject.get(uuid);
//                var monney = el.getAsDouble();
//                if (monney >= amount) {
//                    jsonObject.addProperty(uuid, monney - amount);
//                    jsonUtils.writeToFile(playersMonneyFile, jsonObject);
//                } else {
//                    return Result.NO_ENOUGH_MONNEY;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return Result.IOEXCEPTION;
//        }
//        return Result.SUCCESS;
//    }


//    public Result old_deposit(String uuid, double amount){
//        var playersMonneyFile = MOD_CONFIG_DIR.resolve("PlayersMonney.json").toFile();
//        JsonUtils jsonUtils = new JsonUtils();
//        try {
//            var jsonObject = jsonUtils.readOrCreateNew(playersMonneyFile);
//            if (jsonObject.get(uuid) == null) {
//                jsonObject.addProperty(uuid, amount);
//            } else {
//                var el = jsonObject.get(uuid);
//                var monney = el.getAsDouble();
//                jsonObject.addProperty(uuid, monney + amount);
//            }
//            jsonUtils.writeToFile(playersMonneyFile, jsonObject);
//            EconomyEvent.DEPOSIT.invoker().deposit(uuid, amount);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return Result.IOEXCEPTION;
//        }
//        return Result.SUCCESS;
//    }


//    @Nullable
//    public Double old_getBalance(String uuid) {
//        var playersMonneyFile = MOD_CONFIG_DIR.resolve("PlayersMonney.json").toFile();
//        var jsonUtils = new JsonUtils();
//        try {
//            var jsonObject = jsonUtils.readOrCreateNew(playersMonneyFile);
//            if (jsonObject.get(uuid) != null) {
//                var el = jsonObject.get(uuid);
//                return el.getAsDouble();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static class EconomyEvent{

        public static final Event<EconomyEvent.Deposit> DEPOSIT = EventFactory.createArrayBacked(EconomyEvent.Deposit.class, callbacks -> (uuid, amount) -> {
            for (EconomyEvent.Deposit callback : callbacks) {
                callback.deposit( uuid, amount);
            }
        });

        @FunctionalInterface
        public interface Deposit {
            void deposit(String uuid, double amount);
        }

    }

}

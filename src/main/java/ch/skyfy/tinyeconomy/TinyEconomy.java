package ch.skyfy.tinyeconomy;

import ch.skyfy.tinyeconomy.commands.CmdMonney;
import ch.skyfy.tinyeconomy.database.Database;
import ch.skyfy.tinyeconomy.features.EarnMonney;
import ch.skyfy.tinyeconomy.features.MySounds;
import ch.skyfy.tinyeconomy.features.ShopFeature;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"ResultOfMethodCallIgnored", "unused"})
public class TinyEconomy implements ModInitializer {

    public static final String MOD_ID = "tiny_economy";

    public static Path MOD_CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("TinyEconomy");

    public static final AtomicBoolean DISABLED = new AtomicBoolean(false);

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            // If this return true, the mod has been disabled due to errors
            if (createConfigDir() || createConfiguration()){
                System.out.println("[TinyEconomy] -> MOD HAS BEEN DISABLED DUE TO ERRORS -> PLEASE CHECK THE STACKSTRACE AND OPEN AN ISSUE ON GITHUB");
                return;
            }
            registerFeatures();
            registerCommands();
        }
    }

    private boolean createConfiguration() {
        Configurator.getInstance();
        Database.getInstance();
        return DISABLED.get();
    }

    private void registerFeatures() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            ShopFeature.getInstance(); // Eanble shop feature
            EarnMonney.getInstance(); // Enable earn feature
            MySounds.initialize(); // Registering some custom sounds
        }
    }

    /**
     * Enregistre les diverses commandes qui seront utilisé lors de l'aventure
     */
    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            CmdMonney.registerMonneyCommand(dispatcher);
        });
    }

    private boolean createConfigDir() {
        var configDir = MOD_CONFIG_DIR.toFile();
        if (!configDir.exists()) {
            if (!configDir.mkdir()) {
                System.out.println("[ERROR] cannot create config directory");
                DISABLED.set(true);
            } else {
                System.out.println("[SUCCESS] config directory created");
            }
        }

        var earnDataFolder = MOD_CONFIG_DIR.resolve("earnData").toFile();
        if (!earnDataFolder.exists())
            if (!earnDataFolder.mkdir()) {
                System.out.println("[TinyEconomy] -> [ERROR] -> Folder earnData cannot be created");
                DISABLED.set(true);
            } else {
                System.out.println("[SUCCESS] Folder earnData created");
            }

        return DISABLED.get();
    }
}

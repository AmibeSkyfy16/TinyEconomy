package ch.skyfy.tinyeconomy;

import ch.skyfy.tinyeconomy.features.earn.DefaultEarnData;
import ch.skyfy.tinyeconomy.features.earn.EarnData;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static ch.skyfy.tinyeconomy.TinyEconomy.DISABLED;
import static ch.skyfy.tinyeconomy.TinyEconomy.MOD_CONFIG_DIR;

public class Configurator {

    private static class ConfiguratorHolder {
        public static final Configurator INSTANCE = new Configurator();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Configurator getInstance() {
        return Configurator.ConfiguratorHolder.INSTANCE;
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public final EarnData earnData;

    public Configurator() {
        this.earnData = writeOrLoadRewards();
        if(this.earnData == null)DISABLED.set(true);
    }

    public @Nullable EarnData writeOrLoadRewards() {
        Map<String, Double> blockMinedRewards;
        Map<String, Double> advancementRewards;
        Map<String, Double> entityRewards;

        var earnDataFolder = MOD_CONFIG_DIR.resolve("earnData").toFile();

        try {
            var blockMinedRewardsFile = earnDataFolder.toPath().resolve("blockMinedRewards.json").toFile();
            if (blockMinedRewardsFile.exists()) {
                blockMinedRewards = getRewards(blockMinedRewardsFile, DefaultEarnData.DEFAULT_BLOCK_MINED_REWARDS);
            } else {
                blockMinedRewards = DefaultEarnData.DEFAULT_BLOCK_MINED_REWARDS;
                saveRewards(blockMinedRewardsFile, blockMinedRewards);
            }

            var advancementRewardsFile = earnDataFolder.toPath().resolve("advancementRewards.json").toFile();
            if (advancementRewardsFile.exists()) {
                advancementRewards = getRewards(advancementRewardsFile, DefaultEarnData.DEFAULT_ADVANCEMENT_REWARDS);
            } else {
                advancementRewards = DefaultEarnData.DEFAULT_ADVANCEMENT_REWARDS;
                saveRewards(advancementRewardsFile, advancementRewards);
            }

            var entityRewardsFile = earnDataFolder.toPath().resolve("entityRewards.json").toFile();
            if (entityRewardsFile.exists()) {
                entityRewards = getRewards(entityRewardsFile, DefaultEarnData.DEFAULT_ENTITY_REWARDS);
            } else {
                entityRewards = DefaultEarnData.DEFAULT_ENTITY_REWARDS;
                saveRewards(entityRewardsFile, entityRewards);
            }
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

        return new EarnData(blockMinedRewards,advancementRewards, entityRewards);
    }

    @SuppressWarnings({"UnstableApiUsage"})
    private Map<String, Double> getRewards(File file, Map<String, Double> defaultRewards) {
        Map<String, Double> rewards;
        try (var reader = new FileReader(file)) {
            rewards = gson.fromJson(reader, new TypeToken<Map<String, Double>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
            rewards = defaultRewards;
        }
        return rewards;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void saveRewards(File file, Map<String, Double> rewards) throws IOException {
        try (var writer = new FileWriter(file)) {
            gson.toJson(rewards, new TypeToken<Map<String, Double>>(){}.getType(), writer);
        }
    }

}

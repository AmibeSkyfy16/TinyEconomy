package ch.skyfy.tinyeconomy.features.earn;

import java.util.Map;

/**
 * This is the data red from json files configuration
 * if there are none json configuration files, this data will be equal to the data inside EarnDefaultData
 */
@SuppressWarnings("ALL")
public class EarnData {

    public final Map<String, Double> blockMinedRewards;

    public final Map<String, Double> advancementRewards;

    public final Map<String, Double> entityRewards;

    public EarnData(Map<String, Double> blockMinedRewards, Map<String, Double> advancementRewards, Map<String, Double> entityRewards) {
        this.blockMinedRewards = blockMinedRewards;
        this.advancementRewards = advancementRewards;
        this.entityRewards = entityRewards;
    }

}

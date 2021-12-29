package ch.skyfy.tinyeconomy.client;

import ch.skyfy.tinyeconomy.features.MySounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TinyEconomyClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MySounds.initialize();
    }
}

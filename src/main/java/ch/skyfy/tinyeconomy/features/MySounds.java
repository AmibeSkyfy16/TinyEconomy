package ch.skyfy.tinyeconomy.features;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class MySounds {

    public static final Identifier PRICE_ALERT_ID = new Identifier("tinyeconomy", "price_alert");
    public static SoundEvent PRICE_ALERT_EVENT = new SoundEvent(PRICE_ALERT_ID);

    private static final List<SoundEvent> sounds = new ArrayList<>() {{
        add(PRICE_ALERT_EVENT);
    }};

    public static void initialize() {
        registerSounds();
    }

    private static void registerSounds() {
        Registry.register(Registry.SOUND_EVENT, PRICE_ALERT_ID, PRICE_ALERT_EVENT);
        System.out.println("REGISTERED SOUND: " + PRICE_ALERT_EVENT.getId().getPath());
    }

    public static void playSound(ServerPlayerEntity player, String soundName, float volume, float pitch) {
//        System.out.println("SENDING TO PLAYER");
        var soundEvent = sounds.stream().filter(soundEvent1 -> soundEvent1.getId().getPath().equalsIgnoreCase(soundName)).findFirst();
        soundEvent.ifPresent(soundEvent1 -> {
            player.getWorld().playSound(null, player.getBlockPos(), soundEvent1, SoundCategory.BLOCKS, volume, pitch);
        });
    }

}

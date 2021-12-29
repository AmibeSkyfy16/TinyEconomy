package ch.skyfy.tinyeconomy.mixin;

import ch.skyfy.tinyeconomy.Configurator;
import ch.skyfy.tinyeconomy.database.Database;
import ch.skyfy.tinyeconomy.features.EarnMonney;
import ch.skyfy.tinyeconomy.features.Economy;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
    @Final
    @Shadow
    private Set<Advancement> visibleAdvancements;
    @Final
    @Shadow
    private Map<Advancement, AdvancementProgress> advancementToProgress;

    @Inject(method = "sendUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectAdvancementLoader(ServerPlayerEntity player, CallbackInfo ci, Map<Identifier, AdvancementProgress> map, Set<Advancement> set, Set<Identifier> set2) {
        for (var advancementAdvancementProgressEntry : advancementToProgress.entrySet()) {
            if (advancementAdvancementProgressEntry.getValue().isDone()) {
                var currentAdvancementId = advancementAdvancementProgressEntry.getKey().getId().toString(); // the value is : minecraft:story/mine_stone
                Configurator.getInstance().earnData.advancementRewards.forEach((advancementId, price) -> {
                    if(advancementId.equalsIgnoreCase(currentAdvancementId)){
                        var advancementDones = Database.getInstance().getAdvancementDone(player.getUuidAsString());
                        if(!advancementDones.contains(currentAdvancementId)){
                            Economy.getInstance().deposit(player.getUuidAsString(), price);
                            Database.getInstance().advancementDoneTransaction(player.getUuidAsString(), currentAdvancementId, price);
                        }
                    }
                });
            }
        }
    }
}
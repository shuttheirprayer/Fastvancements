package com.misanthropy.linggango.fastvancements.mixin;

import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerTrigger.class)
public class PlayerTriggerMixin {

    @Shadow @Final ResourceLocation id;

    @Unique
    private final Map<UUID, Integer> fastvancements$skipTicks = new HashMap<>();

    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private void fastvancements$throttleTrigger(ServerPlayer player, CallbackInfo ci) {
        String triggerPath = this.id.getPath();

        if (triggerPath.equals("location") || triggerPath.equals("tick")) {
            int ticks = fastvancements$skipTicks.getOrDefault(player.getUUID(), 0);

            if (ticks < 10) {
                fastvancements$skipTicks.put(player.getUUID(), ticks + 1);
                ci.cancel();
            } else {
                fastvancements$skipTicks.put(player.getUUID(), 0);
            }
        }
    }
}
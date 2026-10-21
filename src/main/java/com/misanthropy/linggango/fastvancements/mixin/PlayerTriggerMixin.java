package com.misanthropy.linggango.fastvancements.mixin;

import com.misanthropy.linggango.fastvancements.core.FastvancementsConfig;
import com.misanthropy.linggango.fastvancements.core.ThrottleMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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

import java.util.UUID;

@Mixin(PlayerTrigger.class)
public class PlayerTriggerMixin {

    @Shadow @Final ResourceLocation id;

    @Unique
    private final Object2IntOpenHashMap<UUID> fastvancements$skipTicks = ThrottleMaps.track(new Object2IntOpenHashMap<>());

    @Unique
    private byte fastvancements$throttled;

    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("HEAD"), cancellable = true)
    private void fastvancements$throttleTrigger(ServerPlayer player, CallbackInfo ci) {
        if (!FastvancementsConfig.locationTickOpt) return;

        byte throttled = fastvancements$throttled;
        if (throttled == 0) {
            String path = this.id.getPath();
            throttled = path.equals("location") || path.equals("tick") ? (byte) 1 : (byte) 2;
            fastvancements$throttled = throttled;
        }
        if (throttled != 1) return;

        UUID uuid = player.getUUID();
        int ticks = fastvancements$skipTicks.getInt(uuid);
        if (ticks < FastvancementsConfig.locationSkipTicks) {
            fastvancements$skipTicks.put(uuid, ticks + 1);
            ci.cancel();
        } else if (ticks != 0) {
            fastvancements$skipTicks.put(uuid, 0);
        }
    }
}

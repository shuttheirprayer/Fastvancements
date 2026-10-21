package com.misanthropy.linggango.fastvancements;

import com.misanthropy.linggango.fastvancements.core.AdvancementThresholdManager;
import com.misanthropy.linggango.fastvancements.core.FastvancementsConfig;
import com.misanthropy.linggango.fastvancements.core.ThrottleMaps;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Fastvancements.MODID)
public class Fastvancements {

    public static final String MODID = "fastvancements";

    public Fastvancements() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FastvancementsConfig.COMMON_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AdvancementThresholdManager());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ThrottleMaps.purge(event.getEntity().getUUID());
    }
}
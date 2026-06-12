package com.misanthropy.linggango.fastvancements;

import com.misanthropy.linggango.fastvancements.core.AdvancementThresholdManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Fastvancements.MODID)
public class Fastvancements {

    public static final String MODID = "fastvancements";

    public Fastvancements() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AdvancementThresholdManager());
    }
}
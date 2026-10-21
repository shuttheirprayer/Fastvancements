package com.misanthropy.linggango.fastvancements.core;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = "fastvancements", bus = Mod.EventBusSubscriber.Bus.MOD)
public class FastvancementsConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    public static boolean containerMenuOpt = true;
    public static boolean inventoryChangeOpt = true;
    public static boolean locationTickOpt = true;
    public static int locationSkipTicks = 10;
    public static boolean fastItemMatching = true;

    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            containerMenuOpt = COMMON.enableContainerMenuOptimization.get();
            inventoryChangeOpt = COMMON.enableInventoryChangeOptimization.get();
            locationTickOpt = COMMON.enableLocationTickOptimization.get();
            locationSkipTicks = COMMON.locationTickSkipTicks.get();
            fastItemMatching = COMMON.enableFastItemMatching.get();
        }
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue enableContainerMenuOptimization;
        public final ForgeConfigSpec.BooleanValue enableInventoryChangeOptimization;
        public final ForgeConfigSpec.BooleanValue enableLocationTickOptimization;
        public final ForgeConfigSpec.IntValue locationTickSkipTicks;
        public final ForgeConfigSpec.BooleanValue enableFastItemMatching;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("Optimizations");

            enableContainerMenuOptimization = builder
                    .comment("Enable container menu slot tracking optimizations.")
                    .define("enableContainerMenuOptimization", true);

            enableInventoryChangeOptimization = builder
                    .comment("Enable optimized inventory change triggers.")
                    .define("enableInventoryChangeOptimization", true);

            enableLocationTickOptimization = builder
                    .comment("Enable optimized location and tick triggers.")
                    .define("enableLocationTickOptimization", true);

            locationTickSkipTicks = builder
                    .comment("Ticks to skip between location/tick checks. Vanilla is 0 (Default: 10).")
                    .defineInRange("locationTickSkipTicks", 10, 0, 1200);

            enableFastItemMatching = builder
                    .comment("Enable fast item predicate matching for advancements.")
                    .define("enableFastItemMatching", true);

            builder.pop();
        }
    }
}
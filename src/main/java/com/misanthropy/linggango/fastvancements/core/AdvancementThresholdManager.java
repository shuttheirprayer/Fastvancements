package com.misanthropy.linggango.fastvancements.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class AdvancementThresholdManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final SortedSet<Integer> THRESHOLDS = new TreeSet<>(Collections.singleton(1));

    public AdvancementThresholdManager() {
        super(GSON, "advancements");
    }

    public static void clear() {
        THRESHOLDS.clear();
        THRESHOLDS.add(1);
    }

    public static boolean passesThreshold(ItemStack stack) {
        int prevValue = ((FastDuck.ItemStackDuck) (Object) stack).fastvancements$getPrevSize();
        int newValue = stack.getCount();

        for (int thr : THRESHOLDS) {
            if (prevValue < thr && newValue >= thr) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        clear();
        String triggerId = CriteriaTriggers.INVENTORY_CHANGED.getId().toString();

        for (JsonElement element : object.values()) {
            JsonObject criteria = element.getAsJsonObject().getAsJsonObject("criteria");
            if (criteria != null && !criteria.isJsonNull()) {
                for (var entry : criteria.entrySet()) {
                    JsonObject criterion = entry.getValue().getAsJsonObject();
                    JsonElement trigger = criterion.get("trigger");
                    JsonObject conditions = criterion.getAsJsonObject("conditions");

                    if (trigger != null && conditions != null && !trigger.isJsonNull() && !conditions.isJsonNull()
                            && trigger.getAsString().equals(triggerId) && conditions.has("items")) {
                        for (JsonElement itemElem : conditions.getAsJsonArray("items")) {
                            JsonElement count = itemElem.getAsJsonObject().get("count");
                            if (count != null && !count.isJsonNull()) {
                                int min = 0;
                                if (count.isJsonObject()) {
                                    JsonElement minElem = count.getAsJsonObject().get("min");
                                    if (minElem != null && !minElem.isJsonNull()) {
                                        min = minElem.getAsInt();
                                    }
                                } else {
                                    min = count.getAsInt();
                                }
                                if (min > 1) {
                                    THRESHOLDS.add(min);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
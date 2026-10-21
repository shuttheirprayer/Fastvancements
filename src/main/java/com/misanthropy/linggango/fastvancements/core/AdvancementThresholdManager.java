package com.misanthropy.linggango.fastvancements.core;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AdvancementThresholdManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static volatile int[] thresholds = {1};

    public AdvancementThresholdManager() {
        super(GSON, "advancements");
    }

    public static boolean passesThreshold(ItemStack stack) {
        int prev = ((FastDuck.ItemStackDuck) (Object) stack).fastvancements$getPrevSize();
        int cur = stack.getCount();
        if (cur <= prev) return false;

        int[] t = thresholds;
        int n = t.length;
        int lo = 0, hi = n;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (t[mid] <= prev) lo = mid + 1;
            else hi = mid;
        }
        return lo < n && t[lo] <= cur;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        IntSortedSet collected = new IntAVLTreeSet();
        collected.add(1);

        String triggerId = CriteriaTriggers.INVENTORY_CHANGED.getId().toString();

        for (JsonElement element : object.values()) {
            JsonObject criteria = element.getAsJsonObject().getAsJsonObject("criteria");
            if (criteria == null) continue;

            for (var entry : criteria.entrySet()) {
                JsonObject criterion = entry.getValue().getAsJsonObject();
                JsonElement trigger = criterion.get("trigger");
                JsonObject conditions = criterion.getAsJsonObject("conditions");

                if (trigger == null || conditions == null || trigger.isJsonNull()
                        || !trigger.getAsString().equals(triggerId) || !conditions.has("items")) continue;

                for (JsonElement itemElem : conditions.getAsJsonArray("items")) {
                    JsonElement count = itemElem.getAsJsonObject().get("count");
                    if (count == null || count.isJsonNull()) continue;

                    int min;
                    if (count.isJsonObject()) {
                        JsonElement minElem = count.getAsJsonObject().get("min");
                        if (minElem == null || minElem.isJsonNull()) continue;
                        min = minElem.getAsInt();
                    } else {
                        min = count.getAsInt();
                    }
                    if (min > 1) collected.add(min);
                }
            }
        }

        thresholds = collected.toIntArray();
    }
}

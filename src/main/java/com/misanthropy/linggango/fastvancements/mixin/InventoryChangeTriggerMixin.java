package com.misanthropy.linggango.fastvancements.mixin;

import com.misanthropy.linggango.fastvancements.core.AdvancementThresholdManager;
import com.misanthropy.linggango.fastvancements.core.FastvancementsConfig;
import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(InventoryChangeTrigger.class)
abstract class InventoryChangeTriggerMixin {

    @Unique
    private final Map<UUID, Map<Item, Integer>> fastvancements$skipTicks = new HashMap<>();

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "HEAD"), cancellable = true)
    public void trigger(ServerPlayer player, Inventory inv, ItemStack stack, CallbackInfo ci) {
        if (!FastvancementsConfig.inventoryChangeOpt) return;

        Map<Item, Integer> skipTickMap = fastvancements$skipTicks.computeIfAbsent(player.getUUID(), k -> new HashMap<>());

        Item item = stack.getItem();
        int skipTicks = skipTickMap.getOrDefault(item, FastvancementsConfig.inventorySkipTicks + 1);

        if (skipTicks < FastvancementsConfig.inventorySkipTicks) {
            skipTickMap.put(item, skipTicks + 1);
            ci.cancel();
            return;
        }
        skipTickMap.put(item, 0);

        if (stack.isEmpty()
                || stack.getCount() < ((FastDuck.ItemStackDuck) (Object) stack).fastvancements$getPrevSize()
                || !AdvancementThresholdManager.passesThreshold(stack)) {
            ci.cancel();
            return;
        }

        Map<PlayerAdvancements, Set<SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>>> playersMap =
                ((SimpleCriterionTriggerAccessor<InventoryChangeTrigger.TriggerInstance>) this).fastvancements$getPlayers();

        Set<SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>> listeners = playersMap.get(player.getAdvancements());

        if (listeners != null && !listeners.isEmpty()) {
            int full = 0, empty = 0, occ = 0;
            for (int l = 0; l < inv.getContainerSize(); ++l) {
                ItemStack invStack = inv.getItem(l);
                if (invStack.isEmpty()) { ++empty; }
                else { ++occ; if (invStack.getCount() >= invStack.getMaxStackSize()) { ++full; } }
            }

            List<SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>> validListeners = null;

            for (SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener : listeners) {
                InventoryChangeTrigger.TriggerInstance instance = listener.getTriggerInstance();

                if (((FastDuck.TriggerInstanceDuck) instance).fastvancements$canFastFail(stack)) continue;

                if (instance.matches(inv, stack, full, empty, occ)) {
                    if (validListeners == null) validListeners = new ArrayList<>();
                    validListeners.add(listener);
                }
            }

            if (validListeners != null) {
                for (SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener : validListeners) {
                    listener.run(player.getAdvancements());
                }
            }
        }

        ci.cancel();
    }
}
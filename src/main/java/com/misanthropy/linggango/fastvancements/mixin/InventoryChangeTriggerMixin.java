package com.misanthropy.linggango.fastvancements.mixin;

import com.misanthropy.linggango.fastvancements.core.AdvancementThresholdManager;
import com.misanthropy.linggango.fastvancements.core.FastvancementsConfig;
import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(InventoryChangeTrigger.class)
abstract class InventoryChangeTriggerMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "HEAD"), cancellable = true)
    public void trigger(ServerPlayer player, Inventory inv, ItemStack stack, CallbackInfo ci) {
        if (!FastvancementsConfig.inventoryChangeOpt) return;
        ci.cancel();

        if (stack.isEmpty()
                || stack.getCount() < ((FastDuck.ItemStackDuck) (Object) stack).fastvancements$getPrevSize()
                || !AdvancementThresholdManager.passesThreshold(stack)) return;

        PlayerAdvancements advancements = player.getAdvancements();
        Set<SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>> listeners =
                ((SimpleCriterionTriggerAccessor<InventoryChangeTrigger.TriggerInstance>) this).fastvancements$getPlayers().get(advancements);
        if (listeners == null || listeners.isEmpty()) return;

        List<SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance>> valid = null;
        int full = 0, empty = 0, occupied = 0;
        boolean counted = false;

        for (SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener : listeners) {
            InventoryChangeTrigger.TriggerInstance instance = listener.getTriggerInstance();
            if (((FastDuck.TriggerInstanceDuck) instance).fastvancements$canFastFail(stack)) continue;

            if (!counted) {
                counted = true;
                int size = inv.getContainerSize();
                for (int slot = 0; slot < size; ++slot) {
                    ItemStack invStack = inv.getItem(slot);
                    if (!invStack.isEmpty()) {
                        ++occupied;
                        if (invStack.getCount() >= invStack.getMaxStackSize()) ++full;
                    }
                }
                empty = size - occupied;
            }

            if (instance.matches(inv, stack, full, empty, occupied)) {
                if (valid == null) valid = new ArrayList<>();
                valid.add(listener);
            }
        }

        if (valid != null) {
            for (SimpleCriterionTrigger.Listener<InventoryChangeTrigger.TriggerInstance> listener : valid) {
                listener.run(advancements);
            }
        }
    }
}

package com.misanthropy.linggango.fastvancements.mixin;

import com.misanthropy.linggango.fastvancements.core.FastvancementsConfig;
import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(AbstractContainerMenu.class)
abstract class AbstractContainerMenuMixin {

    @Redirect(method = "addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;add(Ljava/lang/Object;)Z", ordinal = 1))
    protected boolean addSlot(NonNullList<ItemStack> lastSlots, Object emptyStack, Slot slot) {
        boolean seed = !FastvancementsConfig.containerMenuOpt
                || (slot.container instanceof Inventory && slot.hasItem());
        lastSlots.add(seed ? slot.getItem().copy() : ItemStack.EMPTY);
        return true;
    }

    @Inject(method = "triggerSlotListeners(ILnet/minecraft/world/item/ItemStack;Ljava/util/function/Supplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void triggerSlotListeners(int slotNumber, ItemStack newStack, Supplier<ItemStack> newStackSupplier, CallbackInfo ci, ItemStack oldStack, ItemStack newStack1) {
        if (FastvancementsConfig.containerMenuOpt && ItemStack.isSameItem(newStack1, oldStack)) {
            ((FastDuck.ItemStackDuck) (Object) newStack1).fastvancements$setPrevSize(oldStack.getCount());
        }
    }
}

package com.misanthropy.linggango.fastvancements.mixin;

import com.misanthropy.linggango.fastvancements.core.FastvancementsConfig;
import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Set;
// yo
@Mixin(InventoryChangeTrigger.TriggerInstance.class)
abstract class InventoryChangeTriggerInstanceMixin implements FastDuck.TriggerInstanceDuck {

    @Shadow @Final private ItemPredicate[] predicates;

    @Unique private Set<Item> fastvancements$exactItems;
    @Unique private boolean fastvancements$analyzed;

    @Override
    public boolean fastvancements$canFastFail(ItemStack stack) {
        if (!FastvancementsConfig.fastItemMatching) return false;

        if (!fastvancements$analyzed) {
            fastvancements$analyzed = true;
            fastvancements$exactItems = fastvancements$collectExactItems();
        }

        Set<Item> exact = fastvancements$exactItems;
        return exact != null && !exact.contains(stack.getItem());
    }

    @Unique
    private Set<Item> fastvancements$collectExactItems() {
        Set<Item> exact = null;
        for (ItemPredicate p : predicates) {
            FastDuck.ItemPredicateDuck duck = (FastDuck.ItemPredicateDuck) p;
            Set<Item> items = duck.fastvancements$getSpecificItems();
            if (duck.fastvancements$isComplex() || p == ItemPredicate.ANY || items == null) return null;
            if (exact == null) exact = new ObjectOpenHashSet<>();
            exact.addAll(items);
        }
        return exact;
    }

    @Inject(method = "matches(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;III)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getContainerSize()I", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void matches(Inventory inventory, ItemStack itemStack, int i, int j, int k, CallbackInfoReturnable<Boolean> cir, int predicatesLength, List<ItemPredicate> predicatesList) {
        if (FastvancementsConfig.fastItemMatching
                && !predicatesList.removeIf(p -> ((FastDuck.ItemPredicateDuck) p).fastvancements$fastMatch(itemStack))) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "matches(Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;III)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ItemPredicate;matches(Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean itemPredicateMatches(ItemPredicate itemPredicate, ItemStack itemStack) {
        return FastvancementsConfig.fastItemMatching
                ? ((FastDuck.ItemPredicateDuck) itemPredicate).fastvancements$fastMatch(itemStack)
                : itemPredicate.matches(itemStack);
    }
}

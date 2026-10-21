package com.misanthropy.linggango.fastvancements.mixin;

import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;
import java.util.Set;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateMixin implements FastDuck.ItemPredicateDuck {

    @Final @Shadow private TagKey<Item> tag;
    @Final @Shadow private Set<Item> items;
    @Final @Shadow private MinMaxBounds.Ints count;
    @Final @Shadow private MinMaxBounds.Ints durability;
    @Final @Shadow private EnchantmentPredicate[] enchantments;
    @Final @Shadow private EnchantmentPredicate[] storedEnchantments;
    @Final @Shadow private Potion potion;
    @Final @Shadow private NbtPredicate nbt;

    @Unique
    private static boolean fastvancements$allContainedIn(EnchantmentPredicate[] predicates, Map<Enchantment, Integer> present) {
        for (EnchantmentPredicate p : predicates) {
            if (!p.containedIn(present)) return false;
        }
        return true;
    }

    @Override
    public boolean fastvancements$fastMatch(ItemStack itemStack) {
        if ((Object) this == ItemPredicate.ANY) return true;
        if (((Object) this).getClass() != ItemPredicate.class) return ((ItemPredicate) (Object) this).matches(itemStack);
        if (this.tag != null && !itemStack.is(this.tag)) return false;
        if (this.items != null && !this.items.contains(itemStack.getItem())) return false;
        if (!this.count.matches(itemStack.getCount())) return false;
        if (!this.durability.isAny() && (!itemStack.isDamageableItem() || !this.durability.matches(itemStack.getMaxDamage() - itemStack.getDamageValue()))) return false;
        if (!this.nbt.matches(itemStack)) return false;
        if (this.enchantments.length > 0 && !fastvancements$allContainedIn(this.enchantments, itemStack.getAllEnchantments())) return false;
        if (this.storedEnchantments.length > 0 && !fastvancements$allContainedIn(this.storedEnchantments, EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(itemStack)))) return false;
        return this.potion == null || this.potion == PotionUtils.getPotion(itemStack);
    }

    @Override
    public Set<Item> fastvancements$getSpecificItems() {
        return this.items;
    }

    @Override
    public boolean fastvancements$isComplex() {
        return ((Object) this).getClass() != ItemPredicate.class || this.tag != null || this.nbt != NbtPredicate.ANY || this.enchantments.length > 0 || this.storedEnchantments.length > 0 || this.potion != null;
    }
}

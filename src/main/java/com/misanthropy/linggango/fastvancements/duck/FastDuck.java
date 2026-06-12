package com.misanthropy.linggango.fastvancements.duck;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Set;

public interface FastDuck {
    interface ItemStackDuck {
        void fastvancements$setPrevSize(int value);
        int fastvancements$getPrevSize();
    }

    interface ItemPredicateDuck {
        boolean fastvancements$fastMatch(ItemStack stack);
        Set<Item> fastvancements$getSpecificItems();
        boolean fastvancements$isComplex();
    }

    interface TriggerInstanceDuck {
        boolean fastvancements$canFastFail(ItemStack stack);
    }
}
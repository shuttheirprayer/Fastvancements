package com.misanthropy.linggango.fastvancements.mixin;

import com.misanthropy.linggango.fastvancements.duck.FastDuck;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackMixin implements FastDuck.ItemStackDuck {

    @Unique
    private int fastvancements$prevSize;

    @Override
    public void fastvancements$setPrevSize(int value) {
        fastvancements$prevSize = value;
    }

    @Override
    public int fastvancements$getPrevSize() {
        return fastvancements$prevSize;
    }
}
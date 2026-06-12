package com.misanthropy.linggango.fastvancements.mixin;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(SimpleCriterionTrigger.class)
public interface SimpleCriterionTriggerAccessor<T extends AbstractCriterionTriggerInstance> {
    @Accessor("players")
    Map<PlayerAdvancements, Set<SimpleCriterionTrigger.Listener<T>>> fastvancements$getPlayers();
}
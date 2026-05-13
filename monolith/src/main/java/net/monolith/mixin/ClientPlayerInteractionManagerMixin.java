package net.monolith.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.monolith.visual.WorldParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerInteractionManager.class})
public abstract class ClientPlayerInteractionManagerMixin {
   @Inject(
      method = {"attackEntity"},
      at = {@At("HEAD")}
   )
   private void monolith$spawnWorldParticles(PlayerEntity player, Entity target, CallbackInfo ci) {
      if (target instanceof LivingEntity living) {
         WorldParticles.spawn(living);
      }
   }
}

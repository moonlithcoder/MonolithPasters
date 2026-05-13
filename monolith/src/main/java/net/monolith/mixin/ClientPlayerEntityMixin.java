package net.monolith.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.monolith.combat.RotationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin {
   @Redirect(
      method = {"sendMovementPackets"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"
      )
   )
   private float monolith$serverYaw(ClientPlayerEntity player) {
      return RotationManager.active() ? RotationManager.yaw() : player.getYaw();
   }

   @Redirect(
      method = {"sendMovementPackets"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"
      )
   )
   private float monolith$serverPitch(ClientPlayerEntity player) {
      return RotationManager.active() ? RotationManager.pitch() : player.getPitch();
   }
}

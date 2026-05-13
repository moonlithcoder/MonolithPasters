package net.monolith.mixin;

import net.minecraft.util.math.Vec3d;
import net.minecraft.client.world.ClientWorld;
import net.monolith.visual.WorldRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ClientWorld.class})
public class ClientWorldMixin {
   @Inject(
      method = {"getSkyColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
      if (WorldRender.customWorldEnabled()) {
         cir.setReturnValue(WorldRender.skyColor());
      }
   }

   @Inject(
      method = {"getCloudsColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetCloudsColor(float tickDelta, CallbackInfoReturnable<Integer> cir) {
      if (WorldRender.customWorldEnabled()) {
         cir.setReturnValue(WorldRender.skyColor());
      }
   }
}

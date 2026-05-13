package net.monolith.mixin;

import net.minecraft.client.render.LightmapTextureManager;
import net.monolith.visual.WorldRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LightmapTextureManager.class})
public class LightmapTextureManagerMixin {
   @Inject(
      method = {"getBrightness(FI)F"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onGetBrightness(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
      if (WorldRender.fullbrightEnabled()) {
         cir.setReturnValue(1.0F);
      }
   }
}

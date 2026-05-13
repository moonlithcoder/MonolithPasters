package net.monolith.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.BackgroundRenderer;
import net.monolith.visual.WorldRender;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BackgroundRenderer.class})
public class BackgroundRendererMixin {
   @Inject(
      method = {"getFogColor"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void onGetFogColor(
      Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir
   ) {
      if (WorldRender.customWorldEnabled()) {
         int color = WorldRender.skyColor();
         float red = (float)(color >> 16 & 0xFF) / 255.0F;
         float green = (float)(color >> 8 & 0xFF) / 255.0F;
         float blue = (float)(color & 0xFF) / 255.0F;
         cir.setReturnValue(new Vector4f(red, green, blue, 1.0F));
      }
   }
}

package net.monolith.mixin;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.GameRenderer;
import net.monolith.visual.Removals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"tiltViewWhenHurt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (Removals.shake()) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"bobView"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
      if (Removals.shake()) {
         ci.cancel();
      }
   }
}

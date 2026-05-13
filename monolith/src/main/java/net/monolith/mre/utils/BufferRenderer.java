package net.monolith.mre.utils;

import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;

public final class BufferRenderer {
   private BufferRenderer() {
   }

   public static void renderBuffer(BuiltBuffer buffer) {
      BufferRenderer.drawWithGlobalProgram(buffer);
   }
}

package net.monolith.mre.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.monolith.mre.builders.states.QuadColorState;
import net.monolith.mre.builders.states.QuadRadiusState;
import net.monolith.mre.builders.states.SizeState;
import net.monolith.mre.renderers.IRenderer;
import org.joml.Matrix4f;

public record BuiltRectangle(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness) implements IRenderer {
   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      float width = this.size.width();
      float height = this.size.height();
      if (!(width <= 0.0F) && !(height <= 0.0F)) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         float r = Math.min(Math.min(Math.max(0.0F, this.maxRadius()), width / 2.0F), height / 2.0F);
         if (r <= 0.0F) {
            this.renderQuad(matrix, x, y, z, width, height);
         } else {
            this.renderRounded(matrix, x, y, z, width, height, r);
         }

         RenderSystem.enableCull();
      }
   }

   private void renderQuad(Matrix4f matrix, float x, float y, float z, float width, float height) {
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      builder.vertex(matrix, x, y, z).color(this.color.color1());
      builder.vertex(matrix, x, y + height, z).color(this.color.color2());
      builder.vertex(matrix, x + width, y + height, z).color(this.color.color3());
      builder.vertex(matrix, x + width, y, z).color(this.color.color4());
      BufferRenderer.drawWithGlobalProgram(builder.end());
   }

   private void renderRounded(Matrix4f matrix, float x, float y, float z, float width, float height, float r) {
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
      int color = this.averageColor();
      builder.vertex(matrix, x + width / 2.0F, y + height / 2.0F, z).color(color);
      this.corner(builder, matrix, x + width - r, y + r, z, r, -90.0F, 0.0F, color);
      this.corner(builder, matrix, x + width - r, y + height - r, z, r, 0.0F, 90.0F, color);
      this.corner(builder, matrix, x + r, y + height - r, z, r, 90.0F, 180.0F, color);
      this.corner(builder, matrix, x + r, y + r, z, r, 180.0F, 270.0F, color);
      builder.vertex(matrix, x + width - r, y, z).color(color);
      BufferRenderer.drawWithGlobalProgram(builder.end());
   }

   private void corner(BufferBuilder builder, Matrix4f matrix, float cx, float cy, float z, float r, float start, float end, int color) {
      int segments = Math.max(5, (int)Math.ceil((double)(r * 1.25F)));

      for (int i = 0; i <= segments; i++) {
         float angle = (float)Math.toRadians((double)(start + (end - start) * (float)i / (float)segments));
         builder.vertex(matrix, cx + (float)Math.cos((double)angle) * r, cy + (float)Math.sin((double)angle) * r, z).color(color);
      }
   }

   private float maxRadius() {
      return Math.max(Math.max(this.radius.radius1(), this.radius.radius2()), Math.max(this.radius.radius3(), this.radius.radius4()));
   }

   private int averageColor() {
      int a = (alpha(this.color.color1()) + alpha(this.color.color2()) + alpha(this.color.color3()) + alpha(this.color.color4())) / 4;
      int r = (red(this.color.color1()) + red(this.color.color2()) + red(this.color.color3()) + red(this.color.color4())) / 4;
      int g = (green(this.color.color1()) + green(this.color.color2()) + green(this.color.color3()) + green(this.color.color4())) / 4;
      int b = (blue(this.color.color1()) + blue(this.color.color2()) + blue(this.color.color3()) + blue(this.color.color4())) / 4;
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int alpha(int c) {
      return c >>> 24 & 0xFF;
   }

   private static int red(int c) {
      return c >>> 16 & 0xFF;
   }

   private static int green(int c) {
      return c >>> 8 & 0xFF;
   }

   private static int blue(int c) {
      return c & 0xFF;
   }
}

package net.monolith.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

public final class Render2D {
   private static final Identifier CUSTOM_FONT = Identifier.of("monolith", "main");
   private final DrawContext context;

   private Render2D(DrawContext context) {
      this.context = context;
   }

   public static Render2D of(DrawContext context) {
      return new Render2D(context);
   }

   public MatrixStack matrices() {
      return this.context.getMatrices();
   }

   public void push() {
      this.matrices().push();
   }

   public void pop() {
      this.matrices().pop();
   }

   public void translate(float x, float y, float z) {
      this.matrices().translate(x, y, z);
   }

   public void scale(float x, float y, float z) {
      this.matrices().scale(x, y, z);
   }

   public void scissor(int x1, int y1, int x2, int y2) {
      this.context.enableScissor(x1, y1, x2, y2);
   }

   public void disableScissor() {
      this.context.disableScissor();
   }

   public void rect(int x, int y, int width, int height, int color) {
      if (width > 0 && height > 0 && alpha(color) > 0) {
         this.context.fill(x, y, x + width, y + height, color);
      }
   }

   public void gradient(int x, int y, int width, int height, int topColor, int bottomColor) {
      if (width > 0 && height > 0) {
         this.context.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
      }
   }

   public void outline(int x, int y, int width, int height, int thickness, int color) {
      if (width > 0 && height > 0 && thickness > 0 && alpha(color) > 0) {
         this.rect(x, y, width, thickness, color);
         this.rect(x, y + height - thickness, width, thickness, color);
         this.rect(x, y + thickness, thickness, height - thickness * 2, color);
         this.rect(x + width - thickness, y + thickness, thickness, height - thickness * 2, color);
      }
   }

   public void roundedOutline(int x, int y, int width, int height, int radius, int thickness, int color) {
      if (width > 0 && height > 0 && thickness > 0 && alpha(color) > 0) {
         int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
         if (r == 0) {
            this.outline(x, y, width, height, thickness, color);
         } else {
            this.rect(x + r, y, width - r * 2, thickness, color);
            this.rect(x + r, y + height - thickness, width - r * 2, thickness, color);
            this.rect(x, y + r, thickness, height - r * 2, color);
            this.rect(x + width - thickness, y + r, thickness, height - r * 2, color);
            int segments = Math.max(8, (int)Math.ceil((double)((float)r * 1.5F)));
            this.arcLine((float)(x + width - r), (float)(y + r), (float)r, -90.0F, 0.0F, segments, (float)thickness, color);
            this.arcLine((float)(x + width - r), (float)(y + height - r), (float)r, 0.0F, 90.0F, segments, (float)thickness, color);
            this.arcLine((float)(x + r), (float)(y + height - r), (float)r, 90.0F, 180.0F, segments, (float)thickness, color);
            this.arcLine((float)(x + r), (float)(y + r), (float)r, 180.0F, 270.0F, segments, (float)thickness, color);
         }
      }
   }

   public void circle(float centerX, float centerY, float radius, int color) {
      if (!(radius <= 0.0F) && alpha(color) > 0) {
         this.context.draw();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         Matrix4f matrix = this.matrices().peek().getPositionMatrix();
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
         addVertex(builder, matrix, centerX, centerY, color);
         int segments = Math.max(16, (int)Math.ceil((double)(radius * 2.0F)));
         addArc(builder, matrix, centerX, centerY, radius, 0.0F, 360.0F, segments, color);
         BufferRenderer.drawWithGlobalProgram(builder.end());
      }
   }

   public void line(float x1, float y1, float x2, float y2, float thickness, int color) {
      if (!(thickness <= 0.0F) && alpha(color) > 0) {
         float dx = x2 - x1;
         float dy = y2 - y1;
         float length = (float)Math.sqrt((double)(dx * dx + dy * dy));
         if (length <= 1.0E-4F) {
            this.circle(x1, y1, thickness / 2.0F, color);
         } else {
            float nx = -dy / length * thickness / 2.0F;
            float ny = dx / length * thickness / 2.0F;
            this.context.draw();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Matrix4f matrix = this.matrices().peek().getPositionMatrix();
            BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            addVertex(builder, matrix, x1 + nx, y1 + ny, color);
            addVertex(builder, matrix, x1 - nx, y1 - ny, color);
            addVertex(builder, matrix, x2 - nx, y2 - ny, color);
            addVertex(builder, matrix, x2 + nx, y2 + ny, color);
            BufferRenderer.drawWithGlobalProgram(builder.end());
         }
      }
   }

   public void roundedRect(int x, int y, int width, int height, int radius, int color) {
      if (width > 0 && height > 0 && alpha(color) > 0) {
         int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
         if (r == 0) {
            this.rect(x, y, width, height, color);
         } else {
            this.context.draw();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            Matrix4f matrix = this.matrices().peek().getPositionMatrix();
            BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
            addVertex(builder, matrix, (float)x + (float)width / 2.0F, (float)y + (float)height / 2.0F, color);
            int segments = Math.max(8, (int)Math.ceil((double)((float)r * 1.5F)));
            addArc(builder, matrix, (float)(x + width - r), (float)(y + r), (float)r, -90.0F, 0.0F, segments, color);
            addArc(builder, matrix, (float)(x + width - r), (float)(y + height - r), (float)r, 0.0F, 90.0F, segments, color);
            addArc(builder, matrix, (float)(x + r), (float)(y + height - r), (float)r, 90.0F, 180.0F, segments, color);
            addArc(builder, matrix, (float)(x + r), (float)(y + r), (float)r, 180.0F, 270.0F, segments, color);
            addVertex(builder, matrix, (float)(x + width - r), (float)y, color);
            BufferRenderer.drawWithGlobalProgram(builder.end());
         }
      }
   }

   public void text(TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         this.context.drawText(renderer, styledText(text), x, y, color, shadow);
      }
   }

   public void vanillaText(TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         this.context.drawText(renderer, styledText(text), x, y, color, shadow);
      }
   }

   public void centeredText(TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         Text styled = styledText(text);
         this.context.drawText(renderer, styled, centerX - renderer.getWidth(styled) / 2, y, color, shadow);
      }
   }

   public void centeredVanillaText(TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         this.centeredText(renderer, text, centerX, y, color, shadow);
      }
   }

   public int textWidth(TextRenderer renderer, String text) {
      return renderer.getWidth(styledText(text));
   }

   public int vanillaTextWidth(TextRenderer renderer, String text) {
      return this.textWidth(renderer, text);
   }

   public void texture(Identifier texture, int x, int y, int width, int height) {
      if (width > 0 && height > 0) {
         this.context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 0.0F, 0.0F, width, height, width, height);
      }
   }

   public static Text styledText(String text) {
      return text == null ? Text.empty() : Text.literal(text).setStyle(Style.EMPTY.withFont(CUSTOM_FONT));
   }

   private static int alpha(int color) {
      return color >>> 24 & 0xFF;
   }

   private static void addArc(
      BufferBuilder builder, Matrix4f matrix, float centerX, float centerY, float radius, float startDeg, float endDeg, int segments, int color
   ) {
      for (int i = 0; i <= segments; i++) {
         float angle = (float)Math.toRadians((double)(startDeg + (endDeg - startDeg) * (float)i / (float)segments));
         addVertex(builder, matrix, centerX + (float)Math.cos((double)angle) * radius, centerY + (float)Math.sin((double)angle) * radius, color);
      }
   }

   private void arcLine(float centerX, float centerY, float radius, float startDeg, float endDeg, int segments, float thickness, int color) {
      float prevX = 0.0F;
      float prevY = 0.0F;

      for (int i = 0; i <= segments; i++) {
         float angle = (float)Math.toRadians((double)(startDeg + (endDeg - startDeg) * (float)i / (float)segments));
         float nextX = centerX + (float)Math.cos((double)angle) * radius;
         float nextY = centerY + (float)Math.sin((double)angle) * radius;
         if (i > 0) {
            this.line(prevX, prevY, nextX, nextY, thickness, color);
         }

         prevX = nextX;
         prevY = nextY;
      }
   }

   private static void addVertex(BufferBuilder builder, Matrix4f matrix, float x, float y, int color) {
      builder.vertex(matrix, x, y, 0.0F).color(color);
   }
}

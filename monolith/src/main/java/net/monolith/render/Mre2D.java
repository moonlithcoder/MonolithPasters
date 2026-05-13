package net.monolith.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.monolith.mre.builders.Builder;
import net.monolith.mre.builders.states.QuadColorState;
import net.monolith.mre.builders.states.QuadRadiusState;
import net.monolith.mre.builders.states.SizeState;
import org.joml.Matrix4f;

public final class Mre2D {
   private final DrawContext context;

   private Mre2D(DrawContext context) {
      this.context = context;
   }

   public static Mre2D of(DrawContext context) {
      return new Mre2D(context);
   }

   public MatrixStack matrices() {
      return this.context.getMatrices();
   }

   public Matrix4f matrix() {
      return this.matrices().peek().getPositionMatrix();
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

   public void rect(float x, float y, float width, float height, int color) {
      if (!(width <= 0.0F) && !(height <= 0.0F) && alpha(color) > 0) {
         Builder.rectangle().size(new SizeState(width, height)).color(new QuadColorState(color)).build().render(this.matrix(), x, y, 0.0F);
      }
   }

   public void gradient(float x, float y, float width, float height, int topColor, int bottomColor) {
      if (!(width <= 0.0F) && !(height <= 0.0F)) {
         Builder.rectangle()
            .size(new SizeState(width, height))
            .color(new QuadColorState(topColor, bottomColor, bottomColor, topColor))
            .build()
            .render(this.matrix(), x, y, 0.0F);
      }
   }

   public void roundedRect(float x, float y, float width, float height, float radius, int color) {
      if (!(width <= 0.0F) && !(height <= 0.0F) && alpha(color) > 0) {
         Builder.rectangle()
            .size(new SizeState(width, height))
            .radius(new QuadRadiusState(radius))
            .color(new QuadColorState(color))
            .build()
            .render(this.matrix(), x, y, 0.0F);
      }
   }

   public void blur(float x, float y, float width, float height, float radius, float blurRadius, int color) {
      if (!(width <= 0.0F) && !(height <= 0.0F) && alpha(color) > 0) {
         Builder.blur()
            .size(new SizeState(width, height))
            .radius(new QuadRadiusState(radius))
            .blurRadius(blurRadius)
            .color(new QuadColorState(color))
            .build()
            .render(this.matrix(), x, y, 0.0F);
      }
   }

   public void roundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
      if (!(width <= 0.0F) && !(height <= 0.0F) && !(thickness <= 0.0F) && alpha(color) > 0) {
         Builder.border()
            .size(new SizeState(width, height))
            .radius(new QuadRadiusState(radius))
            .thickness(thickness)
            .color(new QuadColorState(color))
            .build()
            .render(this.matrix(), x, y, 0.0F);
      }
   }

   public void circle(float centerX, float centerY, float radius, int color) {
      this.roundedRect(centerX - radius, centerY - radius, radius * 2.0F, radius * 2.0F, radius, color);
   }

   public void text(TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         this.context.drawText(renderer, Render2D.styledText(text), x, y, color, shadow);
      }
   }

   public void vanillaText(TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         this.context.drawText(renderer, Render2D.styledText(text), x, y, color, shadow);
      }
   }

   public void centeredText(TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         Text styled = Render2D.styledText(text);
         this.context.drawText(renderer, styled, centerX - renderer.getWidth(styled) / 2, y, color, shadow);
      }
   }

   public void centeredVanillaText(TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow) {
      if (text != null && !text.isEmpty()) {
         Text styled = Render2D.styledText(text);
         this.context.drawText(renderer, styled, centerX - renderer.getWidth(styled) / 2, y, color, shadow);
      }
   }

   public int textWidth(TextRenderer renderer, String text) {
      return renderer.getWidth(Render2D.styledText(text));
   }

   public int vanillaTextWidth(TextRenderer renderer, String text) {
      return renderer.getWidth(Render2D.styledText(text));
   }

   public void texture(Identifier texture, int x, int y, int width, int height) {
      if (width > 0 && height > 0) {
         this.context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 0.0F, 0.0F, width, height, width, height);
      }
   }

   public void texture(Identifier texture, int x, int y, int width, int height, int color) {
      if (width > 0 && height > 0 && alpha(color) > 0) {
         this.context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 0.0F, 0.0F, width, height, width, height, color);
      }
   }

   private static int alpha(int color) {
      return color >>> 24 & 0xFF;
   }
}

package net.monolith.utils;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.monolith.render.Mre2D;
import net.monolith.render.Render2D;

public class RenderUtils {
   public static final Identifier CUSTOM_FONT = Identifier.of("monolith", "main");

   public static Text getCustomText(String text) {
      return Render2D.styledText(text);
   }

   public static void drawText(DrawContext context, TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
      Mre2D.of(context).text(renderer, text, x, y, color, shadow);
   }

   public static void drawCenteredText(DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color, boolean shadow) {
      Mre2D.of(context).centeredText(renderer, text, centerX, y, color, shadow);
   }

   public static int getTextWidth(TextRenderer renderer, String text) {
      return renderer.getWidth(Render2D.styledText(text));
   }

   public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
      Mre2D.of(context).rect((float)x, (float)y, (float)width, (float)height, color);
   }

   public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
      Mre2D.of(context).roundedRect((float)x, (float)y, (float)width, (float)height, (float)radius, color);
   }

   public static float lerp(float start, float end, float delta) {
      return start + (end - start) * delta;
   }

   public static int lerpColor(int from, int to, float delta) {
      int r1 = from >> 16 & 0xFF;
      int g1 = from >> 8 & 0xFF;
      int b1 = from & 0xFF;
      int a1 = from >> 24 & 0xFF;
      int r2 = to >> 16 & 0xFF;
      int g2 = to >> 8 & 0xFF;
      int b2 = to & 0xFF;
      int a2 = to >> 24 & 0xFF;
      int r = (int)((float)r1 + (float)(r2 - r1) * delta);
      int g = (int)((float)g1 + (float)(g2 - g1) * delta);
      int b = (int)((float)b1 + (float)(b2 - b1) * delta);
      int a = (int)((float)a1 + (float)(a2 - a1) * delta);
      return a << 24 | r << 16 | g << 8 | b;
   }
}

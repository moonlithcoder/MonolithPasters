package net.monolith.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.monolith.render.Mre2D;
import net.monolith.utils.RenderUtils;

public class PotionHudElement extends HudElement {
   private float heightAnim = 20.0F;
   private float widthAnim = 70.0F;
   private float alphaAnim = 0.0F;

   public PotionHudElement(int x, int y) {
      super(x, y, 70, 20);
   }

   @Override
   public void render(DrawContext context, float tickDelta) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null) {
         List<StatusEffectInstance> effects = new ArrayList<>(mc.player.getStatusEffects());
         effects.sort(Comparator.comparing(effectx -> ((StatusEffect)effectx.getEffectType().value()).getName().getString().toLowerCase(Locale.ROOT)));
         boolean isChatOpen = mc.currentScreen instanceof ChatScreen;
         boolean visible = !effects.isEmpty() || isChatOpen;
         this.alphaAnim = RenderUtils.lerp(this.alphaAnim, visible ? 1.0F : 0.0F, 0.3F * Math.max(1.0F, tickDelta));
         if (!(this.alphaAnim < 0.02F) || visible) {
            int rows = Math.max(effects.size(), isChatOpen ? 1 : 0);
            float targetWidth = Math.max(76.0F, this.maxWidth(mc, effects, isChatOpen));
            float targetHeight = 20.0F + (float)rows * 17.0F;
            this.widthAnim = RenderUtils.lerp(this.widthAnim, targetWidth, 0.2F * Math.max(1.0F, tickDelta));
            this.heightAnim = RenderUtils.lerp(this.heightAnim, targetHeight, 0.2F * Math.max(1.0F, tickDelta));
            this.width = (int)this.widthAnim;
            this.height = (int)this.heightAnim;
            Mre2D renderer = Mre2D.of(context);
            int alpha = Math.min(255, Math.max(0, (int)(this.alphaAnim * 255.0F)));
            int bg = (int)(170.0F * this.alphaAnim) << 24;
            int text = alpha << 24 | 16777215;
            int levelColor = alpha << 24 | 15422825;
            renderer.blur((float)this.x, (float)this.y, (float)this.width, 17.0F, 5.0F, 10.0F, 1728053247);
            renderer.roundedRect((float)this.x, (float)this.y, (float)this.width, 17.0F, 5.0F, bg);
            renderer.text(mc.textRenderer, "E", this.x + 5, this.y + 5, text, false);
            renderer.text(mc.textRenderer, "Active Potions", this.x + 20, this.y + 5, text, false);
            int rowY = this.y + 21;
            if (effects.isEmpty() && isChatOpen) {
               this.renderPotionRow(context, renderer, mc, "Preview", "10", "**:**", this.previewIcon(), rowY, text, levelColor, bg);
            } else {
               for (StatusEffectInstance effect : effects) {
                  String name = ((StatusEffect)effect.getEffectType().value()).getName().getString();
                  String level = String.valueOf(effect.getAmplifier() + 1);
                  String duration = this.formatDuration(effect.getDuration() / 20);
                  this.renderPotionRow(context, renderer, mc, name, level, duration, this.icon(effect), rowY, text, levelColor, bg);
                  rowY += 17;
               }
            }
         }
      }
   }

   private void renderPotionRow(
      DrawContext context,
      Mre2D renderer,
      MinecraftClient mc,
      String name,
      String level,
      String duration,
      Identifier icon,
      int rowY,
      int textColor,
      int levelColor,
      int bg
   ) {
      renderer.blur((float)this.x, (float)(rowY - 3), (float)this.width, 15.0F, 5.0F, 10.0F, 1728053247);
      renderer.roundedRect((float)this.x, (float)(rowY - 3), (float)this.width, 15.0F, 5.0F, bg);
      renderer.roundedRect((float)(this.x + 15), (float)(rowY - 1), 1.0F, 11.0F, 1.0F, textColor);
      context.drawTexture(RenderLayer::getGuiTextured, icon, this.x + 4, rowY - 1, 0.0F, 0.0F, 9, 9, 9, 9);
      int durationWidth = renderer.textWidth(mc.textRenderer, duration);
      int levelX = this.x + 20 + renderer.textWidth(mc.textRenderer, name) + 4;
      String visibleName = mc.textRenderer.trimToWidth(name, Math.max(20, this.width - durationWidth - renderer.textWidth(mc.textRenderer, level) - 31));
      renderer.text(mc.textRenderer, visibleName, this.x + 20, rowY, textColor, false);
      renderer.text(mc.textRenderer, level, levelX, rowY, levelColor, false);
      renderer.text(mc.textRenderer, duration, this.x + this.width - durationWidth - 4, rowY, textColor, false);
   }

   private float maxWidth(MinecraftClient mc, List<StatusEffectInstance> effects, boolean edit) {
      float max = (float)(mc.textRenderer.getWidth("Active Potions") + 28);
      if (effects.isEmpty() && edit) {
         return Math.max(max, (float)(mc.textRenderer.getWidth("Preview") + mc.textRenderer.getWidth("10") + mc.textRenderer.getWidth("**:**") + 38));
      } else {
         for (StatusEffectInstance effect : effects) {
            String name = ((StatusEffect)effect.getEffectType().value()).getName().getString();
            String level = String.valueOf(effect.getAmplifier() + 1);
            String duration = this.formatDuration(effect.getDuration() / 20);
            max = Math.max(max, (float)(mc.textRenderer.getWidth(name) + mc.textRenderer.getWidth(level) + mc.textRenderer.getWidth(duration) + 38));
         }

         return max;
      }
   }

   private Identifier icon(StatusEffectInstance effect) {
      RegistryEntry<?> entry = effect.getEffectType();
      Identifier id = Registries.STATUS_EFFECT.getId((StatusEffect)entry.value());
      return Identifier.of("minecraft", "textures/mob_effect/" + id.getPath() + ".png");
   }

   private Identifier previewIcon() {
      int index = (int)(System.currentTimeMillis() / 1000L % (long)Math.max(1, Registries.STATUS_EFFECT.size()));
      StatusEffect effect = (StatusEffect)Registries.STATUS_EFFECT.get(index);
      Identifier id = Registries.STATUS_EFFECT.getId(effect);
      return Identifier.of("minecraft", "textures/mob_effect/" + id.getPath() + ".png");
   }

   private String formatDuration(int seconds) {
      int minutes = seconds / 60;
      int sec = seconds % 60;
      return String.format(Locale.ROOT, "%d:%02d", minutes, sec);
   }
}

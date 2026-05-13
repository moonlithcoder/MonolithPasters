package net.monolith.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.monolith.module.ModuleManager;
import net.monolith.render.Mre2D;

public class HudManager {
   public static final List<HudElement> elements = new ArrayList<>();
   public static boolean visible = true;
   private static float fpsAnim;
   private static float xAnim;
   private static float yAnim;
   private static float zAnim;
   private static float speedAnim;
   private static float pingAnim;

   public static void init() {
      elements.add(
         new HudElement(10, 10, 140, 16) {
            @Override
            public void render(DrawContext context, float tickDelta) {
               MinecraftClient mc = MinecraftClient.getInstance();
               Mre2D renderer = Mre2D.of(context);
               if (mc.player != null) {
                  HudManager.fpsAnim = HudManager.animate(HudManager.fpsAnim, (float)mc.getCurrentFps());
                  HudManager.xAnim = HudManager.animate(HudManager.xAnim, (float)mc.player.getX());
                  HudManager.yAnim = HudManager.animate(HudManager.yAnim, (float)mc.player.getY());
                  HudManager.zAnim = HudManager.animate(HudManager.zAnim, (float)mc.player.getZ());
                  Vec3d velocity = mc.player.getVelocity();
                  HudManager.speedAnim = HudManager.animate(
                     HudManager.speedAnim, (float)(Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z) * 20.0)
                  );
                  HudManager.pingAnim = HudManager.animate(HudManager.pingAnim, (float)HudManager.ping(mc));
                  String username = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
                  String fps = Math.round(HudManager.fpsAnim) + " fps";
                  String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " time";
                  String xyz = String.format(Locale.ROOT, "xyz: %.1f, %.1f, %.1f", HudManager.xAnim, HudManager.yAnim, HudManager.zAnim);
                  String speed = String.format(Locale.ROOT, "b/s: %.1f", HudManager.speedAnim);
                  String ping = "ping: " + Math.round(HudManager.pingAnim);
                  int usernameWidth = renderer.textWidth(mc.textRenderer, username) + 20;
                  int fpsWidth = renderer.textWidth(mc.textRenderer, fps) + 20;
                  int timeWidth = renderer.textWidth(mc.textRenderer, time) + 20;
                  int speedWidth = renderer.textWidth(mc.textRenderer, speed) + 14;
                  this.width = Math.max(154, Math.max(renderer.textWidth(mc.textRenderer, xyz) + 12, usernameWidth + fpsWidth + 34));
                  this.height = 34;
                  HudManager.drawPill(renderer, this.x, this.y, 22, 15);
                  HudManager.drawPill(renderer, this.x + 25, this.y, usernameWidth, 15);
                  HudManager.drawPill(renderer, this.x + 28 + usernameWidth, this.y, fpsWidth, 15);
                  HudManager.drawPill(renderer, this.x, this.y + 16, timeWidth, 15);
                  renderer.centeredText(mc.textRenderer, "M", this.x + 11, this.y + 4, -1, false);
                  renderer.text(mc.textRenderer, username, this.x + 35, this.y + 5, -1, false);
                  renderer.text(mc.textRenderer, fps, this.x + 38 + usernameWidth, this.y + 5, -1, false);
                  renderer.text(mc.textRenderer, time, this.x + 8, this.y + 21, -1, false);
                  int baseY = mc.getWindow().getScaledHeight() - 25;
                  renderer.text(mc.textRenderer, xyz, 2, baseY, -1, false);
                  renderer.text(mc.textRenderer, speed, 2, baseY - 10, -1, false);
                  renderer.text(mc.textRenderer, ping, mc.getWindow().getScaledWidth() - renderer.textWidth(mc.textRenderer, ping) - 3, baseY - 10, -1, false);
               }
            }
         }
      );
      elements.add(new PotionHudElement(10, 48));
      elements.add(new TargetHudElement(10, 112));
      elements.add(new KeybindsHudElement(10, 178));
   }

   public static void render(DrawContext context, float tickDelta) {
      if (ModuleManager.getModule("HUD") == null || ModuleManager.getModule("HUD").enabled) {
         MinecraftClient mc = MinecraftClient.getInstance();
         boolean inChat = mc.currentScreen instanceof ChatScreen;
         if (visible || inChat) {
            for (HudElement el : elements) {
               if (inChat) {
                  int mouseX = (int)(mc.mouse.getX() * (double)mc.getWindow().getScaledWidth() / (double)mc.getWindow().getWidth());
                  int mouseY = (int)(mc.mouse.getY() * (double)mc.getWindow().getScaledHeight() / (double)mc.getWindow().getHeight());
                  el.renderEditMode(context, mouseX, mouseY);
               } else {
                  el.render(context, tickDelta);
               }
            }
         }
      }
   }

   public static boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (ModuleManager.getModule("HUD") != null && !ModuleManager.getModule("HUD").enabled) {
         return false;
      } else {
         for (HudElement el : elements) {
            if (el.mouseClicked(mouseX, mouseY, button)) {
               return true;
            }
         }

         return false;
      }
   }

   public static void mouseReleased(int button) {
      for (HudElement el : elements) {
         el.mouseReleased(button);
      }
   }

   private static void drawPill(Mre2D renderer, int x, int y, int width, int height) {
      renderer.blur((float)x, (float)y, (float)width, (float)height, 5.0F, 10.0F, 1728053247);
      renderer.roundedRect((float)x, (float)y, (float)width, (float)height, 5.0F, -1442840576);
   }

   private static float animate(float current, float target) {
      return current + (target - current) * 0.02F;
   }

   private static int ping(MinecraftClient mc) {
      if (mc.getNetworkHandler() != null && mc.player != null) {
         PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
         return entry == null ? 0 : entry.getLatency();
      } else {
         return 0;
      }
   }
}

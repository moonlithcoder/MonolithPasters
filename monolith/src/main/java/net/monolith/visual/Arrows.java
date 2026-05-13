package net.monolith.visual;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.RotationAxis;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Mre2D;

public final class Arrows {
   private static final Identifier ARROW = Identifier.of("monolith", "ui/arrow.png");
   private static float animatedDistance = 62.0F;

   private Arrows() {
   }

   public static void render(DrawContext context, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("Arrows");
      if (module != null && module.enabled && client.player != null && client.world != null) {
         if (client.options.getPerspective() == Perspective.FIRST_PERSON) {
            float distance = (float)module.getSettingValue("Distance", 62.0);
            animatedDistance = MathHelper.lerp(0.22F, animatedDistance, distance + inventoryOffset(client));
            float size = (float)module.getSettingValue("Size", 18.0);
            double range = module.getSettingValue("Range", 80.0);
            double rangeSq = range * range;
            int centerX = context.getScaledWindowWidth() / 2;
            int centerY = context.getScaledWindowHeight() / 2;
            float yaw = client.gameRenderer.getCamera().getYaw();

            for (PlayerEntity player : client.world.getPlayers()) {
               if (player != client.player && player.isAlive() && !player.isSpectator() && !(client.player.squaredDistanceTo(player) > rangeSq)) {
                  Vec3d pos = lerp(player, tickDelta).subtract(client.gameRenderer.getCamera().getPos());
                  double cos = Math.cos(Math.toRadians((double)yaw));
                  double sin = Math.sin(Math.toRadians((double)yaw));
                  double rotY = -(pos.z * cos - pos.x * sin);
                  double rotX = -(pos.x * cos + pos.z * sin);
                  float angle = (float)Math.toDegrees(Math.atan2(rotY, rotX));
                  float x = (float)centerX + animatedDistance * MathHelper.cos((float)Math.toRadians((double)angle));
                  float y = (float)centerY + animatedDistance * MathHelper.sin((float)Math.toRadians((double)angle));
                  drawArrow(context, module, x, y, size, angle + 90.0F, color(module, player));
               }
            }
         }
      }
   }

   private static Vec3d lerp(PlayerEntity player, float tickDelta) {
      return new Vec3d(
         MathHelper.lerp((double)tickDelta, player.prevX, player.getX()),
         MathHelper.lerp((double)tickDelta, player.prevY, player.getY()),
         MathHelper.lerp((double)tickDelta, player.prevZ, player.getZ())
      );
   }

   private static float inventoryOffset(MinecraftClient client) {
      return client.currentScreen instanceof InventoryScreen ? 45.0F : 0.0F;
   }

   private static int color(Module module, PlayerEntity player) {
      String design = module.selectedOption("Design", "Client");
      int alpha = player.isInvisible() ? 120 : 190;
      if (design.equals("Celestial")) {
         return alpha << 24 | 10181887;
      } else {
         return design.equals("Nursultan") ? alpha << 24 | 58879 : alpha << 24 | 16777215;
      }
   }

   private static void drawArrow(DrawContext context, Module module, float x, float y, float size, float angle, int color) {
      String design = module.selectedOption("Design", "Client");
      Mre2D r = Mre2D.of(context);
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(x, y, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
      if (design.equals("Celestial")) {
         r.circle(0.0F, 0.0F, size * 0.62F, 570425344 | color & 16777215);
      } else if (design.equals("Nursultan")) {
         r.circle(0.0F, 0.0F, size * 0.7F, 637593087);
         r.circle(0.0F, 0.0F, size * 0.42F, 402653184);
      } else {
         r.circle(0.0F, 0.0F, size * 0.58F, 570425344);
      }

      int drawSize = Math.round(size);
      r.texture(ARROW, -drawSize / 2, -drawSize / 2, drawSize, drawSize, color);
      matrices.pop();
   }
}

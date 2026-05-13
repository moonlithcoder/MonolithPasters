package net.monolith.visual;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.option.Perspective;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Render3D;

public final class Trails {
   private static final Deque<Trails.Point> POINTS = new ArrayDeque<>();
   private static Vec3d lastPoint = Vec3d.ZERO;

   private Trails() {
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("Trails");
      if (module != null && module.enabled && client.player != null && client.world != null && !hiddenByPerspective(client, module)) {
         float tickDelta = context.tickCounter().getTickDelta(true);
         Trails.Point point = point(client, tickDelta, module);
         if (!POINTS.isEmpty()) {
            point = point.smooth(POINTS.peekFirst(), 0.28);
         }

         Vec3d spine = point.center();
         double move = lastPoint == Vec3d.ZERO ? 1.0 : lastPoint.squaredDistanceTo(spine);
         if (POINTS.isEmpty() || move > 0.0025) {
            POINTS.addFirst(point);
            lastPoint = spine;
         } else if (!POINTS.isEmpty()) {
            POINTS.removeFirst();
            POINTS.addFirst(point);
            lastPoint = spine;
         }

         long now = System.currentTimeMillis();
         int length = Math.max(4, (int)Math.round(module.getSettingValue("Length", 36.0)));

         while (POINTS.size() > length) {
            POINTS.removeLast();
         }

         POINTS.removeIf(saved -> now - saved.time > 2200L);
         if (POINTS.size() >= 2) {
            Render3D renderer = Render3D.of(context);
            Iterator<Trails.Point> iterator = POINTS.iterator();
            Trails.Point prev = iterator.next();

            for (int index = 0; iterator.hasNext(); index++) {
               Trails.Point next = iterator.next();
               float fade1 = fade(index, POINTS.size(), now - prev.time);
               float fade2 = fade(index + 1, POINTS.size(), now - next.time);
               int color1 = color(fade1);
               int color2 = color(fade2);
               renderer.ribbon(prev.bottom, prev.top, next.top, next.bottom, color1, color2);
               prev = next;
            }
         }
      } else {
         POINTS.clear();
         lastPoint = Vec3d.ZERO;
      }
   }

   private static Trails.Point point(MinecraftClient client, float tickDelta, Module module) {
      double x = MathHelper.lerp((double)tickDelta, client.player.lastRenderX, client.player.getX());
      double y = MathHelper.lerp((double)tickDelta, client.player.lastRenderY, client.player.getY());
      double z = MathHelper.lerp((double)tickDelta, client.player.lastRenderZ, client.player.getZ());
      float yaw = MathHelper.lerpAngleDegrees(tickDelta, client.player.prevBodyYaw, client.player.bodyYaw);
      Vec3d back = new Vec3d(Math.sin(Math.toRadians((double)yaw)), 0.0, -Math.cos(Math.toRadians((double)yaw))).normalize().multiply(0.3);
      double playerHeight = (double)client.player.getHeight();
      double bottom = y + Math.max(0.58, playerHeight * 0.36);
      double topLimit = y + Math.max(1.25, playerHeight - 0.24);
      double height = module.getSettingValue("Height", 0.9) - module.getSettingValue("Top Gap", 0.04);
      double top = Math.min(topLimit, bottom + Math.max(0.45, height));
      Vec3d base = new Vec3d(x, 0.0, z).add(back);
      Vec3d bottomPos = new Vec3d(base.x, bottom, base.z);
      Vec3d topPos = new Vec3d(base.x, top, base.z);
      return new Trails.Point(bottomPos, topPos, bottomPos.add(topPos).multiply(0.5), System.currentTimeMillis());
   }

   private static boolean hiddenByPerspective(MinecraftClient client, Module module) {
      return module.isOptionSelected("View", "Third person") && client.options.getPerspective() == Perspective.FIRST_PERSON;
   }

   private static float fade(int index, int size, long age) {
      float byIndex = 1.0F - (float)index / (float)Math.max(1, size - 1);
      float byAge = 1.0F - MathHelper.clamp((float)age / 2200.0F, 0.0F, 1.0F);
      return MathHelper.clamp((float)Math.pow((double)Math.min(byIndex, byAge), 1.35), 0.0F, 1.0F);
   }

   private static int color(float fade) {
      int alpha = (int)(MathHelper.clamp(fade, 0.0F, 1.0F) * 230.0F);
      return alpha << 24 | 1635071;
   }

   private static record Point(Vec3d bottom, Vec3d top, Vec3d center, long time) {
      private Trails.Point smooth(Trails.Point previous, double strength) {
         Vec3d newBottom = lerp(this.bottom, previous.bottom, strength);
         Vec3d newTop = lerp(this.top, previous.top, strength);
         return new Trails.Point(newBottom, newTop, newBottom.add(newTop).multiply(0.5), this.time);
      }

      private static Vec3d lerp(Vec3d current, Vec3d previous, double strength) {
         return current.multiply(1.0 - strength).add(previous.multiply(strength));
      }
   }
}

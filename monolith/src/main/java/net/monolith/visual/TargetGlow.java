package net.monolith.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.monolith.combat.AttackAura;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Render3D;

public final class TargetGlow {
   private static float time;

   private TargetGlow() {
   }

   public static void tick() {
      time += 0.1F;
   }

   public static void render(WorldRenderContext context) {
      Module module = ModuleManager.getModule("Target glow");
      LivingEntity target = AttackAura.target();
      if (module != null && module.enabled && target != null) {
         Render3D renderer = Render3D.of(context);
         float size = (float)module.getSettingValue("Size", 1.0);
         Vec3d base = target.getPos();
         float radius = Math.max(0.45F, target.getWidth() * 1.05F * size);

         for (int i = 0; i < 5; i++) {
            float y = (float)(0.16 + (double)((float)i * target.getHeight()) / 5.0 + Math.sin((double)(time + (float)i)) * 0.025);
            renderer.ring(base, radius + (float)i * 0.025F, y, i % 2 == 0 ? -1426116750 : -1727994369);
         }
      }
   }
}

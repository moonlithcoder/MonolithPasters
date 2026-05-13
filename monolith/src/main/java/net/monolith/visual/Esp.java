package net.monolith.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.TypeFilter;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Render3D;

public final class Esp {
   private Esp() {
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("ESP");
      if (module != null && module.enabled && client.player != null && client.world != null) {
         int alpha = (int)(module.getSettingValue("Opacity", 45.0) * 2.55);
         int color = Math.max(25, Math.min(255, alpha)) << 24;
         Render3D renderer = Render3D.of(context);
         Box area = client.player.getBoundingBox().expand(96.0);

         for (LivingEntity entity : client.world.getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), area, entityx -> shouldRender(entityx, module))) {
            if (client.gameRenderer.getCamera().getPos().squaredDistanceTo(entity.getPos()) <= 9216.0) {
               Box box = entity.getBoundingBox().expand(0.025);
               int outline = entity instanceof PlayerEntity ? -16718337 : (entity instanceof MobEntity ? -41892 : -11141238);
               renderer.filledBox(box, color);
               renderer.box(box, outline);
            }
         }
      }
   }

   public static boolean shouldRender(LivingEntity entity, Module module) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (entity == client.player || !entity.isAlive() || entity.isSpectator() || entity.getType() == EntityType.ARMOR_STAND) {
         return false;
      } else if (entity instanceof PlayerEntity) {
         return module.isOptionSelected("Targets", "Players");
      } else {
         return entity instanceof PassiveEntity ? module.isOptionSelected("Targets", "Animals") : module.isOptionSelected("Targets", "Mobs");
      }
   }
}

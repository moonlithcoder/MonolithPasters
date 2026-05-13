package net.monolith.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Render3D;

public final class Prediction {
   private Prediction() {
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("Prediction");
      if (module != null && module.enabled && client.world != null && client.player != null) {
         Render3D renderer = Render3D.of(context);

         for (Entity entity : client.world.getEntities()) {
            if (entity.isAlive()) {
               Prediction.PredictionData data = dataForEntity(entity, module);
               if (data != null) {
                  Prediction.Impact impact = simulate(client, entity, entity.getPos(), entity.getVelocity(), data.gravity, data.drag);
                  if (impact != null) {
                     draw(renderer, impact, data.color);
                  }
               }
            }
         }

         Prediction.PredictionData held = dataForHeld(client.player, module);
         if (held != null) {
            Prediction.Impact impact = simulate(
               client, client.player, client.player.getEyePos().add(0.0, -0.1, 0.0), held.velocity, held.gravity, held.drag
            );
            if (impact != null) {
               draw(renderer, impact, held.color);
            }
         }
      }
   }

   private static Prediction.PredictionData dataForEntity(Entity entity, Module module) {
      if (entity instanceof EnderPearlEntity && module.isOptionSelected("Show", "Pearl")) {
         return new Prediction.PredictionData(entity.getVelocity(), 0.03, 0.99, -6595329);
      } else if (entity instanceof TridentEntity && module.isOptionSelected("Show", "Trident")) {
         return new Prediction.PredictionData(entity.getVelocity(), 0.05, entity.isTouchingWater() ? 0.8 : 0.99, -10682392);
      } else {
         return entity instanceof ArrowEntity && !(entity instanceof TridentEntity) && module.isOptionSelected("Show", "Bow")
            ? new Prediction.PredictionData(entity.getVelocity(), 0.05, entity.isTouchingWater() ? 0.6 : 0.99, -11684)
            : null;
      }
   }

   private static Prediction.PredictionData dataForHeld(LivingEntity entity, Module module) {
      if (!entity.isUsingItem()) {
         return entity.getMainHandStack().isOf(Items.CROSSBOW) && module.isOptionSelected("Show", "Crossbow")
            ? new Prediction.PredictionData(entity.getRotationVec(1.0F).multiply(3.15), 0.05, 0.99, -29604)
            : null;
      } else {
         ItemStack stack = entity.getActiveItem();
         if (stack.isOf(Items.ENDER_PEARL) && module.isOptionSelected("Show", "Pearl")) {
            return new Prediction.PredictionData(entity.getRotationVec(1.0F).multiply(1.5), 0.03, 0.99, -6595329);
         } else if (stack.isOf(Items.TRIDENT) && module.isOptionSelected("Show", "Trident")) {
            return new Prediction.PredictionData(entity.getRotationVec(1.0F).multiply(2.5), 0.05, 0.99, -10682392);
         } else if (stack.isOf(Items.BOW) && module.isOptionSelected("Show", "Bow")) {
            double power = Math.min(1.0, (double)entity.getItemUseTime() / 20.0 * ((double)entity.getItemUseTime() / 20.0 + 2.0) / 3.0);
            return power < 0.1 ? null : new Prediction.PredictionData(entity.getRotationVec(1.0F).multiply(power * 3.0), 0.05, 0.99, -11684);
         } else {
            return stack.isOf(Items.CROSSBOW) && module.isOptionSelected("Show", "Crossbow")
               ? new Prediction.PredictionData(entity.getRotationVec(1.0F).multiply(3.15), 0.05, 0.99, -29604)
               : null;
         }
      }
   }

   private static Prediction.Impact simulate(MinecraftClient client, Entity source, Vec3d start, Vec3d velocity, double gravity, double drag) {
      Vec3d pos = start;
      Vec3d vel = velocity;

      for (int i = 0; i < 180; i++) {
         Vec3d next = pos.add(vel);
         BlockHitResult block = client.world.raycast(new RaycastContext(pos, next, ShapeType.COLLIDER, FluidHandling.NONE, client.player));
         if (block.getType() != Type.MISS) {
            return new Prediction.Impact(block.getPos(), true);
         }

         Box box = Box.of(next, 0.35, 0.35, 0.35);

         for (Entity entity : client.world.getOtherEntities(source, box, entityx -> entityx.isAlive() && entityx.canHit())) {
            if (entity != client.player) {
               return new Prediction.Impact(entity.getPos().add(0.0, (double)entity.getHeight() * 0.5, 0.0), false);
            }
         }

         pos = next;
         vel = vel.multiply(drag).add(0.0, -gravity, 0.0);
         if (next.y < (double)client.world.getBottomY()) {
            break;
         }
      }

      return null;
   }

   private static void draw(Render3D renderer, Prediction.Impact impact, int color) {
      renderer.ring(impact.pos, impact.block ? 0.55F : 0.75F, 0.03F, color);
      renderer.star(impact.pos.add(0.0, impact.block ? 0.12 : 0.0, 0.0), 0.18F, color);
   }

   private static record Impact(Vec3d pos, boolean block) {
   }

   private static record PredictionData(Vec3d velocity, double gravity, double drag, int color) {
   }
}

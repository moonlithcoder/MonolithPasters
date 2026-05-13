package net.monolith.combat;

import java.util.Comparator;
import java.util.List;
import net.minecraft.util.Hand;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.TypeFilter;
import net.monolith.friend.FriendManager;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;

public final class AttackAura {
   private static LivingEntity target;
   private static int delay;
   private static int rotationSyncTicks;
   private static int lastTargetId = -1;

   private AttackAura() {
   }

   public static void tick(MinecraftClient client) {
      Module module = ModuleManager.getModule("AttackAura");
      if (module != null && module.enabled && client.player != null && client.world != null && client.interactionManager != null) {
         double range = module.getSettingValue("Range", 3.2);
         double vision = module.getSettingValue("Vision", 4.2);
         target = findTarget(client, module, Math.max(range, vision));
         if (target == null) {
            rotationSyncTicks = 0;
            lastTargetId = -1;
         } else {
            if (lastTargetId != target.getId()) {
               rotationSyncTicks = 0;
               lastTargetId = target.getId();
            }

            AttackAura.Rotation rotation = rotation(client, module, target);
            if (rotationSyncTicks == 0) {
               RotationManager.lockForAttack(rotation.yaw, rotation.pitch);
            } else {
               RotationManager.set(rotation.yaw, rotation.pitch);
            }

            rotationSyncTicks = Math.min(rotationSyncTicks + 1, 4);
            if (delay > 0) {
               delay--;
            } else if (canAttack(client, module, target, range)) {
               if (rotationSyncTicks >= 2 && RotationManager.readyToAttack()) {
                  RotationManager.send(rotation.yaw, rotation.pitch, client);
                  client.interactionManager.attackEntity(client.player, target);
                  client.player.swingHand(Hand.MAIN_HAND);
                  delay = 3;
               }
            }
         }
      } else {
         target = null;
      }
   }

   public static LivingEntity target() {
      return target;
   }

   private static LivingEntity findTarget(MinecraftClient client, Module module, double vision) {
      Box box = client.player.getBoundingBox().expand(vision);
      List<LivingEntity> entities = client.world
         .getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), box, entity -> validTarget(client, module, entity, vision));
      return entities.stream().min(Comparator.comparingDouble(entity -> client.player.squaredDistanceTo(entity))).orElse(null);
   }

   private static boolean validTarget(MinecraftClient client, Module module, LivingEntity entity, double vision) {
      if (entity != client.player && entity.isAlive() && !entity.isDead() && !entity.isSpectator()) {
         if (client.player.squaredDistanceTo(entity) > vision * vision) {
            return false;
         } else if (module.isOptionSelected("Extra", "Don't attack through walls") && !client.player.canSee(entity)) {
            return false;
         } else if (entity instanceof PlayerEntity) {
            boolean friend = FriendManager.isFriend(entity.getName().getString());
            return friend ? module.isOptionSelected("Targets", "Friends") : module.isOptionSelected("Targets", "Players");
         } else if (entity instanceof AnimalEntity) {
            return module.isOptionSelected("Targets", "Animals");
         } else {
            return entity instanceof MobEntity ? module.isOptionSelected("Targets", "Mobs") : false;
         }
      } else {
         return false;
      }
   }

   private static boolean canAttack(MinecraftClient client, Module module, LivingEntity entity, double range) {
      if (client.player.squaredDistanceTo(entity) > range * range) {
         return false;
      } else if (module.isOptionSelected("Extra", "Don't attack while eating") && client.player.isUsingItem()) {
         return false;
      } else {
         return module.isOptionSelected("Extra", "Only with weapon") && !holdingWeapon(client) ? false : client.player.getAttackCooldownProgress(0.5F) >= 0.92F;
      }
   }

   private static boolean holdingWeapon(MinecraftClient client) {
      Item item = client.player.getMainHandStack().getItem();
      return item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem;
   }

   private static AttackAura.Rotation rotation(MinecraftClient client, Module module, LivingEntity entity) {
      Vec3d eyes = client.player.getEyePos();
      Vec3d aim = entity.getPos().add(0.0, Math.max(0.2, (double)entity.getHeight() * 0.55), 0.0);
      Vec3d diff = aim.subtract(eyes);
      double horizontal = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
      float targetYaw = (float)(MathHelper.atan2(diff.z, diff.x) * 180.0F / (float)Math.PI) - 90.0F;
      float targetPitch = (float)(-(MathHelper.atan2(diff.y, horizontal) * 180.0F / (float)Math.PI));
      String mode = module.selectedOption("Rotations", "SpookyTime duels");
      if (module.isOptionSelected("Bypass features", "Randomize aim")) {
         double wobble = Math.sin((double)System.currentTimeMillis() / 85.0);
         targetYaw += (float)wobble * randomYaw(mode);
         targetPitch += (float)Math.cos((double)System.currentTimeMillis() / 110.0) * randomPitch(mode);
      }

      float yawSpeed = yawSpeed(mode);
      float pitchSpeed = pitchSpeed(mode);
      float currentYaw = RotationManager.active() ? RotationManager.yaw() : client.player.getYaw();
      float currentPitch = RotationManager.active() ? RotationManager.pitch() : client.player.getPitch();
      float yaw = smooth(currentYaw, targetYaw, yawSpeed);
      float pitch = smooth(currentPitch, targetPitch, pitchSpeed);
      pitch = MathHelper.clamp(pitch, -89.0F, 89.0F);
      return new AttackAura.Rotation(yaw, pitch);
   }

   private static float smooth(float current, float target, float speed) {
      float delta = MathHelper.wrapDegrees(target - current);
      return current + MathHelper.clamp(delta, -speed, speed);
   }

   private static float yawSpeed(String mode) {
      return switch (mode) {
         case "Polar" -> 34.0F;
         case "Vulcan" -> 29.0F;
         case "Grim" -> 22.0F;
         case "Funtime" -> 38.0F;
         default -> 31.0F;
      };
   }

   private static float pitchSpeed(String mode) {
      return switch (mode) {
         case "Polar" -> 18.0F;
         case "Vulcan" -> 15.0F;
         case "Grim" -> 11.0F;
         case "Funtime" -> 20.0F;
         default -> 16.0F;
      };
   }

   private static float randomYaw(String mode) {
      return switch (mode) {
         case "Grim" -> 1.4F;
         case "Vulcan" -> 2.1F;
         case "Funtime" -> 3.2F;
         default -> 2.6F;
      };
   }

   private static float randomPitch(String mode) {
      return switch (mode) {
         case "Grim" -> 0.7F;
         case "Vulcan" -> 1.1F;
         case "Funtime" -> 1.8F;
         default -> 1.3F;
      };
   }

   private static record Rotation(float yaw, float pitch) {
   }
}

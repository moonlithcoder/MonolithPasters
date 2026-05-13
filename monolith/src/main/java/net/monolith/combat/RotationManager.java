package net.monolith.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;

public final class RotationManager {
   private static boolean active;
   private static float yaw;
   private static float pitch;
   private static int ticks;
   private static int lockedTicks;

   private RotationManager() {
   }

   public static void set(float yaw, float pitch) {
      RotationManager.yaw = yaw;
      RotationManager.pitch = pitch;
      active = true;
      ticks = 4;
   }

   public static void lockForAttack(float yaw, float pitch) {
      RotationManager.yaw = yaw;
      RotationManager.pitch = pitch;
      active = true;
      ticks = 6;
      lockedTicks = 2;
   }

   public static boolean active() {
      return active;
   }

   public static float yaw() {
      return yaw;
   }

   public static float pitch() {
      return pitch;
   }

   public static void tick() {
      if (ticks > 0 && --ticks <= 0) {
         active = false;
      }

      if (lockedTicks > 0) {
         lockedTicks--;
      }
   }

   public static boolean readyToAttack() {
      return active && lockedTicks == 0;
   }

   public static void sendSilentRotation(MinecraftClient client) {
      if (active && client.player != null && client.player.networkHandler != null) {
         client.player.networkHandler.sendPacket(new LookAndOnGround(yaw, pitch, client.player.isOnGround(), client.player.horizontalCollision));
      }
   }

   public static void send(float yaw, float pitch, MinecraftClient client) {
      if (client.player != null && client.player.networkHandler != null) {
         client.player.networkHandler.sendPacket(new LookAndOnGround(yaw, pitch, client.player.isOnGround(), client.player.horizontalCollision));
      }
   }
}

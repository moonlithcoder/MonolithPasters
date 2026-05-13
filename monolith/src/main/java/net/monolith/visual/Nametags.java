package net.monolith.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.TypeFilter;
import net.monolith.friend.FriendManager;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Render3D;

public final class Nametags {
   private Nametags() {
   }

   public static boolean shouldHideVanilla() {
      Module module = ModuleManager.getModule("Nametags");
      return module != null && module.enabled;
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("Nametags");
      if (module != null && module.enabled && client.player != null && client.world != null) {
         Render3D renderer = Render3D.of(context);
         Box area = client.player.getBoundingBox().expand(64.0);

         for (PlayerEntity player : client.world
            .getEntitiesByType(
               TypeFilter.instanceOf(PlayerEntity.class), area, entity -> entity != client.player && entity.isAlive() && !entity.isSpectator()
            )) {
            Vec3d pos = player.getPos().add(0.0, (double)player.getHeight() + 0.5, 0.0);
            double distance = Math.max(2.0, (double)client.player.distanceTo(player));
            float scale = (float)(0.022 * Math.min(2.5, distance / 4.0));
            renderer.nametag(pos, line(player, module), scale, -1, -2006487775);
         }
      }
   }

   private static String line(PlayerEntity player, Module module) {
      StringBuilder builder = new StringBuilder(role(player));
      builder.append(" ").append(player.getName().getString());
      builder.append(" §f- ").append(Math.round(player.getHealth() + player.getAbsorptionAmount())).append("HP");
      if (module.isOptionSelected("Show", "Right item")) {
         append(builder, "R", player.getMainHandStack());
      }

      if (module.isOptionSelected("Show", "Left item")) {
         append(builder, "L", player.getOffHandStack());
      }

      if (module.isOptionSelected("Show", "Armor")) {
         int count = 0;

         for (ItemStack stack : player.getArmorItems()) {
            if (!stack.isEmpty()) {
               count++;
            }
         }

         builder.append("  Armor ").append(count).append("/4");
      }

      return builder.toString();
   }

   private static void append(StringBuilder builder, String side, ItemStack stack) {
      if (!stack.isEmpty()) {
         builder.append("  ").append(side).append(": ").append(stack.getName().getString());
      }
   }

   private static String role(PlayerEntity player) {
      if (FriendManager.isFriend(player.getName().getString())) {
         return "§aдруг";
      } else {
         Text display = player.getDisplayName();
         if (display != null) {
            String text = display.getString().trim();
            int space = text.indexOf(' ');
            if (space > 0 && space < text.length() - 1 && !text.substring(0, space).equals(player.getName().getString())) {
               return "§c" + text.substring(0, space);
            }
         }

         return "§cкрушитель";
      }
   }
}

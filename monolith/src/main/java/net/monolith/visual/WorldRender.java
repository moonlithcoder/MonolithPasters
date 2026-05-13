package net.monolith.visual;

import net.minecraft.client.MinecraftClient;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;

public final class WorldRender {
   private static Double previousGamma;
   private static boolean fullbrightActive;

   private WorldRender() {
   }

   public static void tick(MinecraftClient client) {
      Module module = ModuleManager.getModule("Fullbright");
      if (module == null) {
         restoreGamma(client);
      } else {
         if (module.enabled) {
            if (previousGamma == null) {
               previousGamma = (Double)client.options.getGamma().getValue();
            }

            client.options.getGamma().setValue(15.0);
            fullbrightActive = true;
         } else {
            restoreGamma(client);
         }

         if (client.world != null) {
            client.world.calculateAmbientDarkness();
         }
      }
   }

   public static boolean customWorldEnabled() {
      Module module = ModuleManager.getModule("World render");
      return module != null && module.isOptionSelected("Custom world", "On");
   }

   public static int skyColor() {
      Module module = ModuleManager.getModule("World render");
      int color = module == null ? 6728447 : (int)Math.round(module.getSettingValue("Sky Color", 6728447.0));
      return 0xFF000000 | color & 16777215;
   }

   public static boolean fullbrightEnabled() {
      Module module = ModuleManager.getModule("Fullbright");
      return module != null && module.enabled;
   }

   private static void restoreGamma(MinecraftClient client) {
      if (fullbrightActive) {
         client.options.getGamma().setValue(previousGamma == null ? 0.5 : previousGamma);
         previousGamma = null;
         fullbrightActive = false;
      }
   }
}

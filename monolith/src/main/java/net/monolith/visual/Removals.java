package net.monolith.visual;

import net.monolith.module.Module;
import net.monolith.module.ModuleManager;

public final class Removals {
   private Removals() {
   }

   public static boolean fire() {
      Module module = ModuleManager.getModule("Removals");
      return module != null && module.enabled && module.isOptionSelected("Remove", "Fire");
   }

   public static boolean shake() {
      Module module = ModuleManager.getModule("Removals");
      return module != null && module.enabled && module.isOptionSelected("Remove", "Shake");
   }
}

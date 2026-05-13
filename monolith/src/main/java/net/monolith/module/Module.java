package net.monolith.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Module {
   public String name;
   public String category;
   public String description;
   public boolean enabled;
   public List<String> modes = new ArrayList<>();
   public String currentMode = "";
   public boolean expanded = false;
   public Map<String, Module.Setting> settings = new LinkedHashMap<>();
   public List<Module.OptionSetting> optionSettings = new ArrayList<>();
   public int keyCode = -1;
   public boolean binding = false;
   public int hudKeyCode = -1;
   public boolean hudBinding = false;
   public float toggleAnim = 0.0F;
   public float expandAnim = 0.0F;
   public float hoverAnim = 0.0F;

   public Module(String name, String category) {
      this(name, category, "");
   }

   public Module(String name, String category, String description) {
      this.name = name;
      this.category = category;
      this.description = description;
      this.enabled = false;
   }

   public void toggle() {
      this.enabled = !this.enabled;
   }

   public void cycleMode() {
      if (!this.modes.isEmpty()) {
         int idx = this.modes.indexOf(this.currentMode);
         idx = (idx + 1) % this.modes.size();
         this.currentMode = this.modes.get(idx);
      }
   }

   public Module.Setting addSetting(String name, double value, double min, double max, double step) {
      return this.addSetting(name, value, min, max, step, "");
   }

   public Module.Setting addSetting(String name, double value, double min, double max, double step, String description) {
      Module.Setting setting = new Module.Setting(name, value, min, max, step, false, description);
      this.settings.put(name, setting);
      return setting;
   }

   public Module.Setting addColorSetting(String name, int rgb) {
      return this.addColorSetting(name, rgb, "");
   }

   public Module.Setting addColorSetting(String name, int rgb, String description) {
      Module.Setting setting = new Module.Setting(name, (double)rgb, 0.0, 1.6777215E7, 1.0, true, description);
      this.settings.put(name, setting);
      return setting;
   }

   public Module.Setting getSetting(String name) {
      return this.settings.get(name);
   }

   public double getSettingValue(String name, double fallback) {
      Module.Setting setting = this.settings.get(name);
      return setting == null ? fallback : setting.value;
   }

   public Module.OptionSetting addOptionSetting(String name, boolean multiple, String selected, String... options) {
      Module.OptionSetting setting = new Module.OptionSetting(name, multiple, selected, "", options);
      this.optionSettings.add(setting);
      return setting;
   }

   public Module.OptionSetting addOptionSettingDescription(String name, boolean multiple, String selected, String description, String... options) {
      Module.OptionSetting setting = new Module.OptionSetting(name, multiple, selected, description, options);
      this.optionSettings.add(setting);
      return setting;
   }

   public Module.OptionSetting getOptionSetting(String name) {
      for (Module.OptionSetting setting : this.optionSettings) {
         if (setting.name.equalsIgnoreCase(name)) {
            return setting;
         }
      }

      return null;
   }

   public boolean isOptionSelected(String settingName, String option) {
      Module.OptionSetting setting = this.getOptionSetting(settingName);
      return setting != null && setting.isSelected(option);
   }

   public String selectedOption(String settingName, String fallback) {
      Module.OptionSetting setting = this.getOptionSetting(settingName);
      return setting == null ? fallback : setting.selectedOption(fallback);
   }

   public static final class OptionSetting {
      public final String name;
      public final boolean multiple;
      public final String description;
      public final List<String> options;
      public final Set<String> selected = new LinkedHashSet<>();

      public OptionSetting(String name, boolean multiple, String selected, String... options) {
         this(name, multiple, selected, "", options);
      }

      public OptionSetting(String name, boolean multiple, String selected, String description, String... options) {
         this.name = name;
         this.multiple = multiple;
         this.description = description;
         this.options = new ArrayList<>(Arrays.asList(options));
         if (this.options.contains(selected)) {
            this.selected.add(selected);
         } else if (!this.options.isEmpty()) {
            this.selected.add(this.options.get(0));
         }
      }

      public void click(String option) {
         if (this.options.contains(option)) {
            if (this.multiple) {
               if (this.selected.contains(option)) {
                  this.selected.remove(option);
               } else {
                  this.selected.add(option);
               }
            } else {
               this.selected.clear();
               this.selected.add(option);
            }
         }
      }

      public boolean isSelected(String option) {
         return this.selected.contains(option);
      }

      public String selectedOption(String fallback) {
         return this.selected.isEmpty() ? fallback : this.selected.iterator().next();
      }
   }

   public static final class Setting {
      public final String name;
      public final double min;
      public final double max;
      public final double step;
      public final boolean color;
      public final String description;
      public double value;

      public Setting(String name, double value, double min, double max, double step) {
         this(name, value, min, max, step, false, "");
      }

      public Setting(String name, double value, double min, double max, double step, boolean color) {
         this(name, value, min, max, step, color, "");
      }

      public Setting(String name, double value, double min, double max, double step, boolean color, String description) {
         this.name = name;
         this.min = min;
         this.max = max;
         this.step = step;
         this.color = color;
         this.description = description;
         this.set(value);
      }

      public void increment() {
         this.set(this.value + this.step);
      }

      public void decrement() {
         this.set(this.value - this.step);
      }

      public void set(double value) {
         this.value = Math.max(this.min, Math.min(this.max, value));
      }

      public String displayValue() {
         if (this.color) {
            return "Color";
         } else {
            return Math.abs(this.value - Math.rint(this.value)) < 0.001
               ? Integer.toString((int)Math.rint(this.value))
               : String.format(Locale.ROOT, "%.1f", this.value);
         }
      }
   }
}

package net.monolith.ui;

import java.awt.Color;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.monolith.config.ConfigManager;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Mre2D;
import net.monolith.utils.RenderUtils;
import org.lwjgl.glfw.GLFW;

public class CsGuiScreen extends Screen {
   private final String[] categories = new String[]{"Combat", "Movement", "Visuals", "Player", "Misc", "Themes", "Configs"};
   private String currentCategory = "Visuals";
   private Module activeSettingsModule;
   private Module.Setting draggingSetting;
   private float openAnim;
   private float settingsAnim;
   private int scroll;
   private int settingsScroll;
   private int accent = -16718337;
   private static final int W = 448;
   private static final int H = 286;
   private static final int SIDE_W = 94;
   private static final int CONTENT_X = 108;
   private static final int SETTINGS_W = 142;

   public CsGuiScreen() {
      super(Text.literal("Monolith"));
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
      Mre2D.of(context).gradient(0.0F, 0.0F, (float)this.width, (float)this.height, -234881024, -133757175);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.openAnim = RenderUtils.lerp(this.openAnim, 1.0F, 0.18F * delta);
      this.settingsAnim = RenderUtils.lerp(this.settingsAnim, this.activeSettingsModule == null ? 0.0F : 1.0F, 0.18F * delta);
      Mre2D r = Mre2D.of(context);
      int x = (this.width - W) / 2;
      int y = (this.height - H) / 2;
      r.push();
      r.translate((float)this.width / 2.0F, (float)this.height / 2.0F, 0.0F);
      r.scale(0.94F + this.openAnim * 0.06F, 0.94F + this.openAnim * 0.06F, 1.0F);
      r.translate((float)(-this.width) / 2.0F, (float)(-this.height) / 2.0F, 0.0F);
      r.blur((float)x, (float)y, (float)W, (float)H, 10.0F, 12.0F, -133822968);
      r.roundedOutline((float)x, (float)y, (float)W, (float)H, 10.0F, 1.0F, 587202559);
      r.gradient((float)x, (float)y, (float)W, 46.0F, 1510673166, 134217728);
      r.text(this.textRenderer, "MONOLITH", x + 16, y + 13, this.accent, true);
      r.text(this.textRenderer, "visual client", x + 16, y + 27, -9867139, false);
      int catY = y + 56;

      for (String category : this.categories) {
         boolean selected = this.currentCategory.equals(category);
         boolean hovered = this.inside((double)mouseX, (double)mouseY, x + 12, catY, SIDE_W - 24, 23);
         int bg = selected ? this.withAlpha(this.accent, 70) : (hovered ? 419430399 : 0);
         r.roundedRect((float)(x + 12), (float)catY, (float)(SIDE_W - 24), 23.0F, 6.0F, bg);
         if (selected) {
            r.roundedRect((float)(x + 12), (float)(catY + 5), 3.0F, 13.0F, 2.0F, this.accent);
         }

         r.text(this.textRenderer, category, x + 24, catY + 7, selected ? -1 : (hovered ? -3353116 : -8879727), false);
         catY += 27;
      }

      int contentX = x + CONTENT_X;
      int contentY = y + 56;
      int contentH = H - 72;
      int contentW = W - CONTENT_X - 14;
      int settingsX = x + W - SETTINGS_W - 10;
      boolean settingsOpen = this.activeSettingsModule != null && !this.currentCategory.equals("Themes") && !this.currentCategory.equals("Configs");
      int moduleW = settingsOpen ? settingsX - contentX - 8 : contentW;
      r.roundedRect((float)(contentX - 6), (float)(contentY - 8), (float)(contentW + 12), (float)(contentH + 14), 9.0F, -1610481148);
      r.text(this.textRenderer, this.currentCategory, contentX, y + 30, -1, true);
      if (this.currentCategory.equals("Themes")) {
         this.renderThemes(r, mouseX, mouseY, contentX, contentY);
      } else if (this.currentCategory.equals("Configs")) {
         this.renderConfigs(r, mouseX, mouseY, contentX, contentY);
      } else {
         this.renderModules(r, mouseX, mouseY, contentX, contentY, moduleW, contentH, delta);
      }

      if (settingsOpen) {
         this.renderSettings(r, mouseX, mouseY, settingsX, contentY, SETTINGS_W, contentH, delta);
      }
      r.pop();
   }

   private void renderModules(Mre2D r, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
      List<Module> modules = ModuleManager.getModulesByCategory(this.currentCategory);
      int cardH = this.activeSettingsModule == null ? 40 : 32;
      int gap = this.activeSettingsModule == null ? 7 : 6;
      int my = y - this.scroll;
      r.scissor(x - 4, y - 4, x + width + 4, y + height + 4);

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         int mx = x;
         module.toggleAnim = RenderUtils.lerp(module.toggleAnim, module.enabled ? 1.0F : 0.0F, 0.2F * delta);
         boolean selected = this.activeSettingsModule == module;
         boolean hovered = this.inside((double)mouseX, (double)mouseY, mx, my, width, cardH);
         int color = RenderUtils.lerpColor(-16316405, this.withAlpha(this.accent, 62), module.toggleAnim);
         if (hovered) {
            color = RenderUtils.lerpColor(color, -15658216, 0.55F);
         }

         r.roundedRect((float)mx, (float)my, (float)width, (float)cardH, 8.0F, color);
         r.roundedOutline((float)mx, (float)my, (float)width, (float)cardH, 8.0F, 0.7F, selected ? this.accent : (module.enabled ? this.withAlpha(this.accent, 115) : 318767103));
         r.roundedRect((float)(mx + 7), (float)(my + 7), 3.0F, (float)(cardH - 14), 2.0F, module.enabled ? this.accent : -14407630);
         r.text(this.textRenderer, this.trim(module.name, this.activeSettingsModule == null ? 20 : 10), mx + 17, my + 7, module.enabled ? -1 : -4932664, false);
         String state = module.enabled ? "enabled" : "disabled";
         int stateColor = module.enabled ? this.accent : -10590604;
         if (this.activeSettingsModule == null) {
            r.text(this.textRenderer, state, mx + width - r.textWidth(this.textRenderer, state) - 16, my + 7, stateColor, false);
            this.marquee(r, module.description, mx + 17, my + 22, width - 34, -9867139, delta, x - 4, y - 4, x + width + 4, y + height + 4);
         }
         if (this.hasSettings(module)) {
            r.text(this.textRenderer, ">", mx + width - 13, my + (this.activeSettingsModule == null ? 22 : 12), selected ? this.accent : -10590604, false);
         }

         my += cardH + gap;
      }

      r.disableScissor();
   }

   private void renderThemes(Mre2D r, int mouseX, int mouseY, int x, int y) {
      int[] colors = new int[]{-16718337, -6595329, -45096, -11684, -11141238, -41892};
      String[] names = new String[]{"Cyan", "Purple", "Pink", "Gold", "Lime", "Red"};

      for (int i = 0; i < colors.length; i++) {
         int bx = x + i % 3 * 95;
         int by = y + i / 3 * 52;
         boolean selected = this.accent == colors[i];
         boolean hovered = this.inside((double)mouseX, (double)mouseY, bx, by, 84, 40);
         r.roundedRect((float)bx, (float)by, 84.0F, 40.0F, 8.0F, hovered ? -15658216 : -16316405);
         r.roundedOutline((float)bx, (float)by, 84.0F, 40.0F, 8.0F, 0.8F, selected ? colors[i] : 419430399);
         r.circle((float)(bx + 17), (float)(by + 20), 8.0F, colors[i]);
         r.text(this.textRenderer, names[i], bx + 32, by + 16, selected ? -1 : -4932664, false);
      }
   }

   private void renderConfigs(Mre2D r, int mouseX, int mouseY, int x, int y) {
      List<String> configs = ConfigManager.safeList();
      if (configs.isEmpty()) {
         r.text(this.textRenderer, "No configs", x + 8, y + 8, -7695712, false);
      } else {
         int cy = y;

         for (String config : configs) {
            boolean hovered = this.inside((double)mouseX, (double)mouseY, x, cy, 300, 29);
            r.roundedRect((float)x, (float)cy, 300.0F, 29.0F, 7.0F, hovered ? -15658216 : -16316405);
            r.roundedOutline((float)x, (float)cy, 300.0F, 29.0F, 7.0F, 0.7F, 419430399);
            r.text(this.textRenderer, this.trim(config, 28), x + 10, cy + 10, -1, false);
            r.text(this.textRenderer, "load", x + 264, cy + 10, hovered ? this.accent : -9867139, false);
            cy += 34;
         }
      }
   }

   private void renderSettings(Mre2D r, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
      r.roundedRect((float)x, (float)y, (float)width, (float)height, 10.0F, -16250612);
      r.roundedOutline((float)x, (float)y, (float)width, (float)height, 10.0F, 0.8F, this.activeSettingsModule == null ? 285212671 : 520093695);
      if (this.activeSettingsModule == null) {
         r.text(this.textRenderer, "Settings", x + 12, y + 12, this.accent, true);
         r.text(this.textRenderer, "Right click", x + 12, y + 31, -9867139, false);
         r.text(this.textRenderer, "module", x + 12, y + 43, -9867139, false);
      } else if (!(this.settingsAnim < 0.03F)) {
         r.scissor(x, y, x + (int)((float)width * this.settingsAnim), y + height);
         r.text(this.textRenderer, this.trim(this.activeSettingsModule.name, 16), x + 12, y + 11, this.accent, true);
         this.marquee(r, this.activeSettingsModule.description, x + 12, y + 27, width - 24, -9867139, delta, x, y, x + (int)((float)width * this.settingsAnim), y + height);
         int sy = y + 50 - this.settingsScroll;

         if (!this.activeSettingsModule.modes.isEmpty()) {
            r.roundedRect((float)(x + 8), (float)(sy - 5), (float)(width - 16), 36.0F, 7.0F, -15263453);
            r.roundedOutline((float)(x + 8), (float)(sy - 5), (float)(width - 16), 36.0F, 7.0F, 0.6F, 285212671);
            r.text(this.textRenderer, "Mode", x + 14, sy, -3353116, false);
            r.text(this.textRenderer, this.trim(this.activeSettingsModule.currentMode, 15), x + 14, sy + 15, this.accent, false);
            sy += 42;
         }

         for (Module.Setting setting : this.activeSettingsModule.settings.values()) {
            int rowH = setting.color ? 44 : 50;
            r.roundedRect((float)(x + 8), (float)(sy - 5), (float)(width - 16), (float)rowH, 7.0F, -15263453);
            r.roundedOutline((float)(x + 8), (float)(sy - 5), (float)(width - 16), (float)rowH, 7.0F, 0.6F, 285212671);
            r.text(this.textRenderer, this.trim(setting.name, 13), x + 14, sy, -3353116, false);
            r.text(this.textRenderer, setting.displayValue(), x + width - 14 - r.textWidth(this.textRenderer, setting.displayValue()), sy, this.accent, false);
            this.marquee(r, setting.description, x + 14, sy + 13, width - 28, -10525069, delta, x, y, x + (int)((float)width * this.settingsAnim), y + height);
            if (setting.color) {
               this.drawPalette(r, x + 14, sy + 29, width - 38, 7);
               int color = 0xFF000000 | (int)Math.rint(setting.value) & 16777215;
               r.roundedRect((float)(x + width - 22), (float)(sy + 28), 10.0F, 10.0F, 3.0F, color);
            } else {
               double progress = (setting.value - setting.min) / (setting.max - setting.min);
               r.roundedRect((float)(x + 14), (float)(sy + 33), (float)(width - 28), 5.0F, 3.0F, -13421773);
               r.roundedRect((float)(x + 14), (float)(sy + 33), (float)((int)((double)(width - 28) * progress)), 5.0F, 3.0F, this.accent);
            }
            sy += rowH + 8;
         }

         for (Module.OptionSetting option : this.activeSettingsModule.optionSettings) {
            int rowH = 36 + option.options.size() * 20;
            r.roundedRect((float)(x + 8), (float)(sy - 5), (float)(width - 16), (float)rowH, 7.0F, -15263453);
            r.roundedOutline((float)(x + 8), (float)(sy - 5), (float)(width - 16), (float)rowH, 7.0F, 0.6F, 285212671);
            r.text(this.textRenderer, this.trim(option.name, 13), x + 14, sy, -3353116, false);
            this.marquee(r, option.description, x + 14, sy + 13, width - 28, -10525069, delta, x, y, x + (int)((float)width * this.settingsAnim), y + height);
            sy += 30;

            for (String value : option.options) {
               boolean selected = option.isSelected(value);
               boolean hovered = this.inside((double)mouseX, (double)mouseY, x + 12, sy, width - 24, 17);
               r.roundedRect(
                  (float)(x + 12), (float)sy, (float)(width - 24), 17.0F, 5.0F, selected ? this.withAlpha(this.accent, 78) : (hovered ? 419430399 : 0)
               );
               r.roundedRect((float)(x + 16), (float)(sy + 5), 3.0F, 7.0F, 2.0F, selected ? this.accent : -14012872);
               r.text(this.textRenderer, this.trim(value, 15), x + 24, sy + 5, selected ? -1 : -8550504, false);
               sy += 20;
            }

            sy += 8;
         }

         String bind = this.activeSettingsModule.binding ? "Press key..." : "Bind: " + this.bindName(this.activeSettingsModule.keyCode);
         r.roundedRect(
            (float)(x + 8), (float)sy, (float)(width - 16), 24.0F, 7.0F, this.activeSettingsModule.binding ? this.withAlpha(this.accent, 72) : -15263453
         );
         r.roundedOutline((float)(x + 8), (float)sy, (float)(width - 16), 24.0F, 7.0F, 0.6F, 285212671);
         r.text(this.textRenderer, this.trim(bind, 17), x + 14, sy + 8, this.activeSettingsModule.binding ? -1 : -4932664, false);
         sy += 30;
         if (this.activeSettingsModule.name.equals("HUD")) {
            String hudBind = this.activeSettingsModule.hudBinding ? "Press HUD key..." : "HUD: " + this.bindName(this.activeSettingsModule.hudKeyCode);
            r.roundedRect(
               (float)(x + 8), (float)sy, (float)(width - 16), 24.0F, 7.0F, this.activeSettingsModule.hudBinding ? this.withAlpha(this.accent, 72) : -15263453
            );
            r.text(this.textRenderer, this.trim(hudBind, 17), x + 14, sy + 8, this.activeSettingsModule.hudBinding ? -1 : -4932664, false);
         }

         r.disableScissor();
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = (this.width - W) / 2;
      int y = (this.height - H) / 2;
      int catY = y + 56;

      for (String category : this.categories) {
         if (this.inside(mouseX, mouseY, x + 12, catY, SIDE_W - 24, 23) && button == 0) {
            this.currentCategory = category;
            this.activeSettingsModule = null;
            this.scroll = 0;
            this.settingsScroll = 0;
            return true;
         }

         catY += 27;
      }

      if (this.currentCategory.equals("Themes")) {
         return this.clickThemes(mouseX, mouseY, button, x + CONTENT_X, y + 56);
      } else if (this.currentCategory.equals("Configs")) {
         return this.clickConfigs(mouseX, mouseY, button, x + CONTENT_X, y + 56);
      } else {
         int contentX = x + CONTENT_X;
         int contentY = y + 56;
         int settingsX = x + W - SETTINGS_W - 14;
         int contentW = this.activeSettingsModule == null ? W - CONTENT_X - 14 : settingsX - contentX - 8;
         int contentH = H - 76;
         return this.clickSettings(mouseX, mouseY, button, settingsX, contentY, SETTINGS_W, contentH)
            ? true
            : this.clickModules(mouseX, mouseY, button, contentX, contentY, contentW);
      }
   }

   private boolean clickModules(double mouseX, double mouseY, int button, int x, int y, int width) {
      List<Module> modules = ModuleManager.getModulesByCategory(this.currentCategory);
      int cardH = this.activeSettingsModule == null ? 40 : 32;
      int my = y - this.scroll;

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         if (this.inside(mouseX, mouseY, x, my, width, cardH)) {
            if (button == 0) {
               module.toggle();
            } else if (button == 1 && this.hasSettings(module)) {
               this.activeSettingsModule = this.activeSettingsModule == module ? null : module;
               this.settingsScroll = 0;
            } else if (button == 2) {
               this.activeSettingsModule = module;
               module.binding = true;
               this.settingsScroll = 0;
            }

            return true;
         }

         my += cardH + 7;
      }

      return false;
   }

   private boolean clickSettings(double mouseX, double mouseY, int button, int x, int y, int width, int height) {
      if (this.activeSettingsModule != null && !(this.settingsAnim < 0.85F) && this.inside(mouseX, mouseY, x, y, width, height)) {
         int sy = y + 50 - this.settingsScroll;

         if (!this.activeSettingsModule.modes.isEmpty()) {
            if (this.inside(mouseX, mouseY, x + 8, sy - 5, width - 16, 36) && button == 0) {
               this.activeSettingsModule.cycleMode();
               return true;
            }

            sy += 42;
         }

         for (Module.Setting setting : this.activeSettingsModule.settings.values()) {
            int rowH = setting.color ? 44 : 50;
            if (this.inside(mouseX, mouseY, x + 8, sy - 5, width - 16, rowH) && button == 0) {
               this.draggingSetting = setting;
               this.updateDraggingSetting(mouseX, x, width, setting);
               return true;
            }

            sy += rowH + 8;
         }

         for (Module.OptionSetting option : this.activeSettingsModule.optionSettings) {
            sy += 30;

            for (String value : option.options) {
               if (this.inside(mouseX, mouseY, x + 12, sy, width - 24, 17) && button == 0) {
                  option.click(value);
                  return true;
               }

               sy += 20;
            }

            sy += 8;
         }

         if (this.inside(mouseX, mouseY, x + 8, sy, width - 16, 24) && button == 0) {
            this.activeSettingsModule.binding = true;
            return true;
         } else {
            sy += 30;
            if (this.activeSettingsModule.name.equals("HUD") && this.inside(mouseX, mouseY, x + 8, sy, width - 16, 24) && button == 0) {
               this.activeSettingsModule.hudBinding = true;
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private boolean clickThemes(double mouseX, double mouseY, int button, int x, int y) {
      if (button != 0) {
         return false;
      } else {
         int[] colors = new int[]{-16718337, -6595329, -45096, -11684, -11141238, -41892};

         for (int i = 0; i < colors.length; i++) {
            int bx = x + i % 3 * 95;
            int by = y + i / 3 * 52;
            if (this.inside(mouseX, mouseY, bx, by, 84, 40)) {
               this.accent = colors[i];
               return true;
            }
         }

         return false;
      }
   }

   private boolean clickConfigs(double mouseX, double mouseY, int button, int x, int y) {
      if (button != 0) {
         return false;
      } else {
         int cy = y;

         for (String config : ConfigManager.safeList()) {
            if (this.inside(mouseX, mouseY, x, cy, 300, 29)) {
               ConfigManager.safeLoad(config);
               return true;
            }

            cy += 34;
         }

         return false;
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.draggingSetting != null && this.activeSettingsModule != null) {
         int x = (this.width - W) / 2 + W - SETTINGS_W - 14;
         this.updateDraggingSetting(mouseX, x, SETTINGS_W, this.draggingSetting);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.draggingSetting = null;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      int x = (this.width - W) / 2;
      int y = (this.height - H) / 2;
      int contentX = x + CONTENT_X;
      int contentY = y + 56;
      int settingsX = x + W - SETTINGS_W - 14;
      int contentW = this.activeSettingsModule == null ? W - CONTENT_X - 14 : settingsX - contentX - 8;
      int contentH = H - 76;
      if (this.activeSettingsModule != null && this.inside(mouseX, mouseY, settingsX, contentY, SETTINGS_W, contentH)) {
         this.settingsScroll = Math.max(0, this.settingsScroll - (int)(verticalAmount * 20.0));
         return true;
      } else if (this.inside(mouseX, mouseY, contentX, contentY, contentW, contentH)) {
         this.scroll = Math.max(0, this.scroll - (int)(verticalAmount * 24.0));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.activeSettingsModule != null && this.activeSettingsModule.binding) {
         this.activeSettingsModule.keyCode = keyCode == 256 ? -1 : keyCode;
         this.activeSettingsModule.binding = false;
         return true;
      } else if (this.activeSettingsModule != null && this.activeSettingsModule.hudBinding) {
         this.activeSettingsModule.hudKeyCode = keyCode == 256 ? -1 : keyCode;
         this.activeSettingsModule.hudBinding = false;
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private boolean hasSettings(Module module) {
      return !module.modes.isEmpty() || !module.settings.isEmpty() || !module.optionSettings.isEmpty();
   }

   private void updateDraggingSetting(double mouseX, int x, int width, Module.Setting setting) {
      double progress = Math.max(0.0, Math.min(1.0, (mouseX - (double)(x + 14)) / (double)(width - 28)));
      if (setting.color) {
         setting.set((double)this.paletteColor(progress));
      } else {
         double raw = setting.min + (setting.max - setting.min) * progress;
         setting.set((double)Math.round(raw / setting.step) * setting.step);
      }
   }

   private void drawPalette(Mre2D r, int x, int y, int width, int height) {
      int segments = 24;

      for (int i = 0; i < segments; i++) {
         double start = (double)i / (double)segments;
         double end = (double)(i + 1) / (double)segments;
         int x1 = x + (int)Math.round((double)width * start);
         int x2 = x + (int)Math.round((double)width * end);
         r.rect((float)x1, (float)y, (float)Math.max(1, x2 - x1), (float)height, 0xFF000000 | this.paletteColor(start));
      }
   }

   private void marquee(Mre2D r, String text, int x, int y, int width, int color, float delta) {
      this.marquee(r, text, x, y, width, color, delta, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
   }

   private void marquee(Mre2D r, String text, int x, int y, int width, int color, float delta, int sx1, int sy1, int sx2, int sy2) {
      if (text != null && !text.isEmpty() && width > 0) {
         int textWidth = r.textWidth(this.textRenderer, text);
         if (textWidth <= width) {
            r.text(this.textRenderer, text, x, y, color, false);
         } else {
            int overflow = textWidth - width;
            float time = (float)(System.currentTimeMillis() % 5000L) / 5000.0F;
            int offset = (int)((Math.sin((double)(time * (float)Math.PI * 2.0F)) * 0.5 + 0.5) * (double)overflow);
            r.scissor(x, y - 1, x + width, y + 10);
            r.text(this.textRenderer, text, x - offset, y, color, false);
            r.disableScissor();
         }

         if (sx1 != Integer.MIN_VALUE) {
            r.scissor(sx1, sy1, sx2, sy2);
         }
      }
   }

   private int paletteColor(double progress) {
      return Color.HSBtoRGB((float)progress, 0.86F, 1.0F) & 16777215;
   }

   private int withAlpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
      return mouseX >= (double)x && mouseX <= (double)(x + w) && mouseY >= (double)y && mouseY <= (double)(y + h);
   }

   private String trim(String text, int max) {
      return text != null && text.length() > max ? text.substring(0, Math.max(0, max - 1)) + "…" : text;
   }

   private String bindName(int keyCode) {
      if (keyCode < 0) {
         return "None";
      } else {
         String name = GLFW.glfwGetKeyName(keyCode, 0);
         return name == null ? "KEY " + keyCode : name.toUpperCase();
      }
   }

   public boolean shouldPause() {
      return false;
   }
}

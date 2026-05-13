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
   private static final int SIDE_W = 90;
   private static final int CONTENT_X = 112;
   private static final int SETTINGS_W = 132;

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
      int x = (this.width - 448) / 2;
      int y = (this.height - 286) / 2;
      r.push();
      r.translate((float)this.width / 2.0F, (float)this.height / 2.0F, 0.0F);
      r.scale(0.94F + this.openAnim * 0.06F, 0.94F + this.openAnim * 0.06F, 1.0F);
      r.translate((float)(-this.width) / 2.0F, (float)(-this.height) / 2.0F, 0.0F);
      r.blur((float)x, (float)y, 448.0F, 286.0F, 10.0F, 12.0F, -133822968);
      r.roundedOutline((float)x, (float)y, 448.0F, 286.0F, 10.0F, 1.0F, 587202559);
      r.gradient((float)x, (float)y, 448.0F, 46.0F, 1510673166, 134217728);
      r.text(this.textRenderer, "MONOLITH", x + 16, y + 13, this.accent, true);
      r.text(this.textRenderer, "visual client", x + 16, y + 27, -9867139, false);
      int catY = y + 56;

      for (String category : this.categories) {
         boolean selected = this.currentCategory.equals(category);
         boolean hovered = this.inside((double)mouseX, (double)mouseY, x + 12, catY, 90, 23);
         int bg = selected ? this.withAlpha(this.accent, 70) : (hovered ? 419430399 : 0);
         r.roundedRect((float)(x + 12), (float)catY, 90.0F, 23.0F, 6.0F, bg);
         if (selected) {
            r.roundedRect((float)(x + 12), (float)(catY + 5), 3.0F, 13.0F, 2.0F, this.accent);
         }

         r.text(this.textRenderer, category, x + 24, catY + 7, selected ? -1 : (hovered ? -3353116 : -8879727), false);
         catY += 27;
      }

      int contentX = x + 112;
      int contentY = y + 56;
      int contentW = 322;
      int contentH = 214;
      r.roundedRect((float)(contentX - 6), (float)(contentY - 8), (float)(contentW + 12), (float)(contentH + 14), 9.0F, -1610481148);
      r.text(this.textRenderer, this.currentCategory, contentX, y + 30, -1, true);
      if (this.currentCategory.equals("Themes")) {
         this.renderThemes(r, mouseX, mouseY, contentX, contentY);
      } else if (this.currentCategory.equals("Configs")) {
         this.renderConfigs(r, mouseX, mouseY, contentX, contentY);
      } else {
         this.renderModules(r, mouseX, mouseY, contentX, contentY, contentW, contentH, delta);
      }

      this.renderSettings(r, mouseX, mouseY, x + 448 + 6, y, delta);
      r.pop();
   }

   private void renderModules(Mre2D r, int mouseX, int mouseY, int x, int y, int width, int height, float delta) {
      List<Module> modules = ModuleManager.getModulesByCategory(this.currentCategory);
      int colW = (width - 10) / 2;
      int y1 = y - this.scroll;
      int y2 = y - this.scroll;
      r.scissor(x - 4, y - 4, x + width + 4, y + height + 6);

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         boolean left = i % 2 == 0;
         int mx = left ? x : x + colW + 10;
         int my = left ? y1 : y2;
         module.toggleAnim = RenderUtils.lerp(module.toggleAnim, module.enabled ? 1.0F : 0.0F, 0.2F * delta);
         boolean hovered = this.inside((double)mouseX, (double)mouseY, mx, my, colW, 40);
         int color = RenderUtils.lerpColor(-16316405, this.withAlpha(this.accent, 62), module.toggleAnim);
         if (hovered) {
            color = RenderUtils.lerpColor(color, -15658216, 0.55F);
         }

         r.roundedRect((float)mx, (float)my, (float)colW, 44.0F, 8.0F, color);
         r.roundedOutline((float)mx, (float)my, (float)colW, 44.0F, 8.0F, 0.7F, module.enabled ? this.withAlpha(this.accent, 115) : 318767103);
         r.roundedRect((float)(mx + 8), (float)(my + 9), 3.0F, 26.0F, 2.0F, module.enabled ? this.accent : -14407630);
         r.text(this.textRenderer, this.trim(module.name, 14), mx + 17, my + 8, module.enabled ? -1 : -4932664, false);
         this.marquee(r, module.description, mx + 17, my + 24, colW - 25, -9867139, delta);
         r.scissor(x - 4, y - 4, x + width + 4, y + height + 6);
         if (this.hasSettings(module)) {
            r.text(this.textRenderer, ">", mx + colW - 14, my + 15, this.activeSettingsModule == module ? this.accent : -10590604, false);
         }

         if (left) {
            y1 += 51;
         } else {
            y2 += 51;
         }
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

   private void renderSettings(Mre2D r, int mouseX, int mouseY, int x, int y, float delta) {
      if (!(this.settingsAnim < 0.03F) && this.activeSettingsModule != null) {
         int panelW = 132;
         r.scissor(x, y, x + (int)((float)panelW * this.settingsAnim), y + 286);
         r.blur((float)x, (float)y, (float)panelW, 286.0F, 10.0F, 12.0F, -200931832);
         r.roundedOutline((float)x, (float)y, (float)panelW, 286.0F, 10.0F, 1.0F, 520093695);
         r.text(this.textRenderer, this.trim(this.activeSettingsModule.name, 15), x + 10, y + 11, this.accent, true);
         r.text(this.textRenderer, "settings", x + 10, y + 25, -10525069, false);
         int sy = y + 43 - this.settingsScroll;

         if (!this.activeSettingsModule.modes.isEmpty()) {
            r.roundedRect((float)(x + 7), (float)(sy - 6), (float)(panelW - 14), 34.0F, 7.0F, -16250612);
            r.roundedOutline((float)(x + 7), (float)(sy - 6), (float)(panelW - 14), 34.0F, 7.0F, 0.6F, 285212671);
            r.text(this.textRenderer, "Mode", x + 12, sy, -3353116, false);
            this.marquee(r, this.activeSettingsModule.currentMode, x + 12, sy + 13, panelW - 25, this.accent, delta);
            r.scissor(x, y, x + (int)((float)panelW * this.settingsAnim), y + 286);
            sy += 40;
         }

         for (Module.Setting setting : this.activeSettingsModule.settings.values()) {
            r.roundedRect((float)(x + 7), (float)(sy - 6), (float)(panelW - 14), setting.color ? 40.0F : 44.0F, 7.0F, -16250612);
            r.roundedOutline((float)(x + 7), (float)(sy - 6), (float)(panelW - 14), setting.color ? 40.0F : 44.0F, 7.0F, 0.6F, 285212671);
            r.text(this.textRenderer, this.trim(setting.name, 13), x + 12, sy, -3353116, false);
            this.marquee(r, setting.description, x + 12, sy + 12, panelW - 25, -10525069, delta);
            r.scissor(x, y, x + (int)((float)panelW * this.settingsAnim), y + 286);
            if (setting.color) {
               this.drawPalette(r, x + 12, sy + 23, panelW - 34, 7);
               int color = 0xFF000000 | (int)Math.rint(setting.value) & 16777215;
               r.roundedRect((float)(x + panelW - 23), (float)sy, 10.0F, 10.0F, 3.0F, color);
               sy += 46;
            } else {
               r.text(this.textRenderer, setting.displayValue(), x + panelW - 32, sy, this.accent, false);
               double progress = (setting.value - setting.min) / (setting.max - setting.min);
               r.roundedRect((float)(x + 12), (float)(sy + 26), (float)(panelW - 26), 5.0F, 3.0F, -15263453);
               r.roundedRect((float)(x + 12), (float)(sy + 26), (float)((int)((double)(panelW - 26) * progress)), 5.0F, 3.0F, this.accent);
               sy += 50;
            }
         }

         for (Module.OptionSetting option : this.activeSettingsModule.optionSettings) {
            r.roundedRect((float)(x + 7), (float)(sy - 6), (float)(panelW - 14), (float)(30 + option.options.size() * 21), 7.0F, -16250612);
            r.roundedOutline((float)(x + 7), (float)(sy - 6), (float)(panelW - 14), (float)(30 + option.options.size() * 21), 7.0F, 0.6F, 285212671);
            r.text(this.textRenderer, this.trim(option.name, 11), x + 12, sy, -3353116, false);
            this.marquee(r, option.description, x + 12, sy + 12, panelW - 25, -10525069, delta);
            r.scissor(x, y, x + (int)((float)panelW * this.settingsAnim), y + 286);
            sy += 27;

            for (String value : option.options) {
               boolean selected = option.isSelected(value);
               boolean hovered = this.inside((double)mouseX, (double)mouseY, x + 10, sy, panelW - 20, 18);
               r.roundedRect(
                  (float)(x + 10), (float)sy, (float)(panelW - 20), 18.0F, 5.0F, selected ? this.withAlpha(this.accent, 78) : (hovered ? 419430399 : 0)
               );
               r.roundedRect((float)(x + 14), (float)(sy + 5), 3.0F, 8.0F, 2.0F, selected ? this.accent : -14012872);
               r.text(this.textRenderer, this.trim(value, 14), x + 22, sy + 6, selected ? -1 : -8550504, false);
               sy += 21;
            }

            sy += 8;
         }

         String bind = this.activeSettingsModule.binding ? "Press key..." : "Bind: " + this.bindName(this.activeSettingsModule.keyCode);
         r.roundedRect(
            (float)(x + 7), (float)sy, (float)(panelW - 14), 22.0F, 7.0F, this.activeSettingsModule.binding ? this.withAlpha(this.accent, 72) : -16250612
         );
         r.roundedOutline((float)(x + 7), (float)sy, (float)(panelW - 14), 22.0F, 7.0F, 0.6F, 285212671);
         r.text(this.textRenderer, this.trim(bind, 16), x + 14, sy + 7, this.activeSettingsModule.binding ? -1 : -4932664, false);
         sy += 26;
         if (this.activeSettingsModule.name.equals("HUD")) {
            String hudBind = this.activeSettingsModule.hudBinding ? "Press HUD key..." : "HUD: " + this.bindName(this.activeSettingsModule.hudKeyCode);
            r.roundedRect(
               (float)(x + 7), (float)sy, (float)(panelW - 14), 22.0F, 7.0F, this.activeSettingsModule.hudBinding ? this.withAlpha(this.accent, 72) : -16250612
            );
            r.text(this.textRenderer, this.trim(hudBind, 16), x + 14, sy + 7, this.activeSettingsModule.hudBinding ? -1 : -4932664, false);
         }

         r.disableScissor();
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = (this.width - 448) / 2;
      int y = (this.height - 286) / 2;
      int catY = y + 56;

      for (String category : this.categories) {
         if (this.inside(mouseX, mouseY, x + 12, catY, 90, 23) && button == 0) {
            this.currentCategory = category;
            this.activeSettingsModule = null;
            this.scroll = 0;
            return true;
         }

         catY += 27;
      }

      if (this.currentCategory.equals("Themes")) {
         return this.clickThemes(mouseX, mouseY, button, x + 112, y + 56);
      } else if (this.currentCategory.equals("Configs")) {
         return this.clickConfigs(mouseX, mouseY, button, x + 112, y + 56);
      } else {
         return this.clickSettings(mouseX, mouseY, button, x + 448 + 6, y) ? true : this.clickModules(mouseX, mouseY, button, x + 112, y + 56, 322);
      }
   }

   private boolean clickModules(double mouseX, double mouseY, int button, int x, int y, int width) {
      List<Module> modules = ModuleManager.getModulesByCategory(this.currentCategory);
      int colW = (width - 10) / 2;
      int y1 = y - this.scroll;
      int y2 = y - this.scroll;

      for (int i = 0; i < modules.size(); i++) {
         Module module = modules.get(i);
         boolean left = i % 2 == 0;
         int mx = left ? x : x + colW + 10;
         int my = left ? y1 : y2;
         if (this.inside(mouseX, mouseY, mx, my, colW, 44)) {
            if (button == 0) {
               module.toggle();
            } else if (button == 1 && this.hasSettings(module)) {
               this.activeSettingsModule = this.activeSettingsModule == module ? null : module;
            } else if (button == 2) {
               this.activeSettingsModule = module;
               module.binding = true;
            }

            return true;
         }

         if (left) {
            y1 += 51;
         } else {
            y2 += 51;
         }
      }

      return false;
   }

   private boolean clickSettings(double mouseX, double mouseY, int button, int x, int y) {
      if (this.activeSettingsModule != null && !(this.settingsAnim < 0.85F) && this.inside(mouseX, mouseY, x, y, 132, 286)) {
         int sy = y + 43 - this.settingsScroll;

         if (!this.activeSettingsModule.modes.isEmpty()) {
            if (this.inside(mouseX, mouseY, x + 7, sy - 6, 118, 34) && button == 0) {
               this.activeSettingsModule.cycleMode();
               return true;
            }

            sy += 40;
         }

         for (Module.Setting setting : this.activeSettingsModule.settings.values()) {
            int rowH = setting.color ? 40 : 44;
            if (this.inside(mouseX, mouseY, x + 7, sy - 6, 118, rowH) && button == 0) {
               this.draggingSetting = setting;
               this.updateDraggingSetting(mouseX, x, setting);
               return true;
            }

            sy += setting.color ? 46 : 50;
         }

         for (Module.OptionSetting option : this.activeSettingsModule.optionSettings) {
            sy += 27;

            for (String value : option.options) {
               if (this.inside(mouseX, mouseY, x + 10, sy, 112, 18) && button == 0) {
                  option.click(value);
                  return true;
               }

               sy += 21;
            }

            sy += 8;
         }

         if (this.inside(mouseX, mouseY, x + 7, sy, 118, 22) && button == 0) {
            this.activeSettingsModule.binding = true;
            return true;
         } else {
            sy += 26;
            if (this.activeSettingsModule.name.equals("HUD") && this.inside(mouseX, mouseY, x + 7, sy, 118, 22) && button == 0) {
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
         int x = (this.width - 448) / 2 + 448 + 6;
         this.updateDraggingSetting(mouseX, x, this.draggingSetting);
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
      int x = (this.width - 448) / 2;
      int y = (this.height - 286) / 2;
      if (this.activeSettingsModule != null && this.inside(mouseX, mouseY, x + 448 + 6, y, 132, 286)) {
         this.settingsScroll = Math.max(0, this.settingsScroll - (int)(verticalAmount * 20.0));
         return true;
      } else if (this.inside(mouseX, mouseY, x + 112, y + 56, 322, 214)) {
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

   private void updateDraggingSetting(double mouseX, int x, Module.Setting setting) {
      double progress = Math.max(0.0, Math.min(1.0, (mouseX - (double)(x + 12)) / 106.0));
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

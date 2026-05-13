package net.monolith.ui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.monolith.render.Mre2D;
import net.monolith.utils.RenderUtils;

public class MonolithMainMenu extends Screen {
   private static final Identifier LOGO_TEXTURE = Identifier.of("monolith", "textures/ui/logo.png");
   private final List<MonolithMainMenu.MenuButton> buttons = new ArrayList<>();
   private float openAnim = 0.0F;

   public MonolithMainMenu() {
      super(Text.literal("Monolith Main Menu"));
   }

   protected void init() {
      super.init();
      this.buttons.clear();
      int btnWidth = 200;
      int btnHeight = 35;
      int startX = this.width - btnWidth - 25;
      int startY = (this.height - 5 * (btnHeight + 15)) / 2;
      this.buttons
         .add(new MonolithMainMenu.MenuButton("Singleplayer", startX, startY, btnWidth, btnHeight, () -> this.client.setScreen(new SelectWorldScreen(this))));
      this.buttons
         .add(new MonolithMainMenu.MenuButton("Multiplayer", startX, startY + 50, btnWidth, btnHeight, () -> this.client.setScreen(new MultiplayerScreen(this))));
      this.buttons
         .add(
            new MonolithMainMenu.MenuButton(
               "Alt Manager", startX, startY + 100, btnWidth, btnHeight, () -> this.client.setScreen(new AltManagerScreen(this))
            )
         );
      this.buttons
         .add(
            new MonolithMainMenu.MenuButton(
               "Settings", startX, startY + 150, btnWidth, btnHeight, () -> this.client.setScreen(new OptionsScreen(this, this.client.options))
            )
         );
      this.buttons.add(new MonolithMainMenu.MenuButton("Quit", startX, startY + 200, btnWidth, btnHeight, () -> this.client.scheduleStop()));
   }

   public void close() {
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
      Mre2D.of(context).gradient(0.0F, 0.0F, (float)this.width, (float)this.height, -16448248, -16777216);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.openAnim = RenderUtils.lerp(this.openAnim, 1.0F, 0.1F * delta);
      Mre2D renderer = Mre2D.of(context);
      int logoSize = (int)(320.0F * this.openAnim);
      int logoX = this.width / 2 - 200 - logoSize / 2;
      int logoY = this.height / 2 - logoSize / 2;
      renderer.push();
      float breathe = (float)Math.sin((double)System.currentTimeMillis() / 1000.0) * 8.0F;
      renderer.texture(LOGO_TEXTURE, logoX, (int)((float)logoY + breathe), logoSize, logoSize);
      int panelWidth = 260;
      int panelX = this.width - panelWidth;
      renderer.rect((float)panelX, 0.0F, (float)panelWidth, (float)this.height, -2012607988);

      for (MonolithMainMenu.MenuButton btn : this.buttons) {
         btn.render(context, mouseX, mouseY, delta);
      }

      renderer.text(this.textRenderer, "Monolith Client v1.0", 10, this.height - 15, 872415231, false);
      renderer.pop();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      for (MonolithMainMenu.MenuButton btn : this.buttons) {
         if (btn.mouseClicked(mouseX, mouseY, button)) {
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private class MenuButton {
      String textString;
      int x;
      int y;
      int width;
      int height;
      Runnable action;
      float hoverAnim = 0.0F;

      public MenuButton(String text, int x, int y, int width, int height, Runnable action) {
         this.textString = text;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.action = action;
      }

      public void render(DrawContext context, int mouseX, int mouseY, float delta) {
         Mre2D renderer = Mre2D.of(context);
         boolean hovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
         this.hoverAnim = RenderUtils.lerp(this.hoverAnim, hovered ? 1.0F : 0.0F, 0.2F * delta);
         int bg = RenderUtils.lerpColor(-15395558, -14540248, this.hoverAnim);
         renderer.roundedRect((float)this.x, (float)this.y, (float)this.width, (float)this.height, 6.0F, bg);
         int textColor = RenderUtils.lerpColor(-5592406, -1, this.hoverAnim);
         int textOffset = (int)(8.0F * this.hoverAnim);
         renderer.text(MonolithMainMenu.this.textRenderer, this.textString, this.x + 20 + textOffset, this.y + (this.height - 8) / 2, textColor, false);
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (mouseX >= (double)this.x
            && mouseX <= (double)(this.x + this.width)
            && mouseY >= (double)this.y
            && mouseY <= (double)(this.y + this.height)
            && button == 0) {
            this.action.run();
            return true;
         } else {
            return false;
         }
      }
   }
}

package net.monolith.ui;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.Session.AccountType;
import net.monolith.mixin.MinecraftClientAccessor;
import net.monolith.render.Mre2D;
import net.monolith.utils.RenderUtils;

public class AltManagerScreen extends Screen {
   private final Screen parent;
   private final List<String> alts = new ArrayList<>();
   private final List<AltManagerScreen.AltButton> buttons = new ArrayList<>();
   private String input = "";
   private float openAnim;
   private float pulse;

   public AltManagerScreen(Screen parent) {
      super(Text.literal("Monolith Alt Manager"));
      this.parent = parent;
   }

   protected void init() {
      this.buttons.clear();
      int panelWidth = 320;
      int panelX = (this.width - panelWidth) / 2;
      int startY = this.height / 2 - 84;
      this.buttons.add(new AltManagerScreen.AltButton("Add", panelX + 20, startY + 58, 88, 26, this::addAlt));
      this.buttons.add(new AltManagerScreen.AltButton("Random", panelX + 116, startY + 58, 88, 26, this::randomAlt));
      this.buttons.add(new AltManagerScreen.AltButton("Back", panelX + 212, startY + 58, 88, 26, () -> this.client.setScreen(this.parent)));
   }

   public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
      Mre2D renderer = Mre2D.of(context);
      renderer.gradient(0.0F, 0.0F, (float)this.width, (float)this.height, -16579834, -16318442);
      this.pulse += delta * 0.025F;

      for (int i = 0; i < 18; i++) {
         float wave = (float)Math.sin((double)(this.pulse + (float)i * 0.75F));
         int x = (int)(((float)(i * 97) + this.pulse * 28.0F) % (float)(this.width + 80)) - 40;
         int y = (int)((float)this.height * (0.18F + (float)(i % 7) * 0.1F) + wave * 18.0F);
         int alpha = 18 + (int)(12.0F * Math.abs(wave));
         renderer.circle((float)x, (float)y, (float)(2 + i % 3), alpha << 24 | 58879);
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      this.openAnim = RenderUtils.lerp(this.openAnim, 1.0F, 0.12F * Math.max(1.0F, delta));
      Mre2D renderer = Mre2D.of(context);
      int panelWidth = 320;
      int panelHeight = 245;
      int panelX = (this.width - panelWidth) / 2;
      int panelY = (this.height - panelHeight) / 2;
      renderer.push();
      renderer.translate((float)this.width / 2.0F, (float)this.height / 2.0F, 0.0F);
      renderer.scale(0.92F + 0.08F * this.openAnim, 0.92F + 0.08F * this.openAnim, 1.0F);
      renderer.translate((float)(-this.width) / 2.0F, (float)(-this.height) / 2.0F, 0.0F);
      renderer.blur((float)(panelX - 2), (float)(panelY - 2), (float)(panelWidth + 4), (float)(panelHeight + 4), 12.0F, 18.0F, -1874198304);
      renderer.roundedRect((float)panelX, (float)panelY, (float)panelWidth, (float)panelHeight, 12.0F, -435681267);
      renderer.roundedOutline((float)panelX, (float)panelY, (float)panelWidth, (float)panelHeight, 12.0F, 1.0F, 2001338592);
      renderer.text(this.textRenderer, "Alt Manager", panelX + 20, panelY + 18, -1, false);
      renderer.text(this.textRenderer, MinecraftClient.getInstance().getSession().getUsername(), panelX + 20, panelY + 34, -16718337, false);
      renderer.roundedRect((float)(panelX + 20), (float)(panelY + 78), (float)(panelWidth - 40), 28.0F, 7.0F, -15658728);
      renderer.text(
         this.textRenderer, this.input.isEmpty() ? "offline nickname" : this.input, panelX + 32, panelY + 88, this.input.isEmpty() ? -10066322 : -1052673, false
      );

      for (AltManagerScreen.AltButton button : this.buttons) {
         button.render(context, mouseX, mouseY, delta);
      }

      int listY = panelY + 118;
      int index = 0;

      for (String alt : this.alts) {
         if (index >= 5) {
            break;
         }

         boolean hovered = mouseX >= panelX + 20 && mouseX <= panelX + panelWidth - 20 && mouseY >= listY && mouseY <= listY + 22;
         renderer.roundedRect((float)(panelX + 20), (float)listY, (float)(panelWidth - 40), 22.0F, 5.0F, hovered ? -15132382 : -1727000554);
         renderer.text(this.textRenderer, alt, panelX + 30, listY + 7, -1052673, false);
         renderer.text(this.textRenderer, "use", panelX + panelWidth - 50, listY + 7, hovered ? -16718337 : -8947840, false);
         listY += 26;
         index++;
      }

      if (this.alts.isEmpty()) {
         renderer.centeredText(this.textRenderer, "Add nickname to switch offline profile", this.width / 2, panelY + 152, -8947840, false);
      }

      renderer.pop();
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      for (AltManagerScreen.AltButton altButton : this.buttons) {
         if (altButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
         }
      }

      int panelWidth = 320;
      int panelX = (this.width - panelWidth) / 2;
      int listY = (this.height - 245) / 2 + 118;

      for (String alt : this.alts) {
         if (mouseX >= (double)(panelX + 20)
            && mouseX <= (double)(panelX + panelWidth - 20)
            && mouseY >= (double)listY
            && mouseY <= (double)(listY + 22)
            && button == 0) {
            this.switchTo(alt);
            return true;
         }

         listY += 26;
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.input.length() < 16 && Character.toString(chr).matches("[A-Za-z0-9_]")) {
         this.input = this.input + chr;
         return true;
      } else {
         return super.charTyped(chr, modifiers);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 259 && !this.input.isEmpty()) {
         this.input = this.input.substring(0, this.input.length() - 1);
         return true;
      } else if (keyCode == 257) {
         this.addAlt();
         return true;
      } else if (keyCode == 256) {
         this.client.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   private void addAlt() {
      if (this.input.length() >= 3 && !this.alts.contains(this.input)) {
         this.alts.add(0, this.input);
         this.switchTo(this.input);
         this.input = "";
      }
   }

   private void randomAlt() {
      this.input = "Monolith" + (100 + (int)(Math.random() * 900.0));
      this.addAlt();
   }

   private void switchTo(String name) {
      UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
      Session session = new Session(name, uuid, "0", Optional.empty(), Optional.empty(), AccountType.LEGACY);
      ((MinecraftClientAccessor)MinecraftClient.getInstance()).monolith$setSession(session);
   }

   private class AltButton {
      private final String label;
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final Runnable action;
      private float hoverAnim;

      private AltButton(String label, int x, int y, int width, int height, Runnable action) {
         this.label = label;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.action = action;
      }

      private void render(DrawContext context, int mouseX, int mouseY, float delta) {
         Mre2D renderer = Mre2D.of(context);
         boolean hovered = mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
         this.hoverAnim = RenderUtils.lerp(this.hoverAnim, hovered ? 1.0F : 0.0F, 0.22F * Math.max(1.0F, delta));
         renderer.roundedRect(
            (float)this.x, (float)this.y, (float)this.width, (float)this.height, 7.0F, RenderUtils.lerpColor(-15395556, -14666678, this.hoverAnim)
         );
         renderer.roundedOutline(
            (float)this.x, (float)this.y, (float)this.width, (float)this.height, 7.0F, 1.0F, RenderUtils.lerpColor(860487904, -1442781697, this.hoverAnim)
         );
         renderer.centeredText(
            AltManagerScreen.this.textRenderer, this.label, this.x + this.width / 2, this.y + 9, RenderUtils.lerpColor(-5592406, -1, this.hoverAnim), false
         );
      }

      private boolean mouseClicked(double mouseX, double mouseY, int button) {
         if (button == 0
            && mouseX >= (double)this.x
            && mouseX <= (double)(this.x + this.width)
            && mouseY >= (double)this.y
            && mouseY <= (double)(this.y + this.height)) {
            this.action.run();
            return true;
         } else {
            return false;
         }
      }
   }
}

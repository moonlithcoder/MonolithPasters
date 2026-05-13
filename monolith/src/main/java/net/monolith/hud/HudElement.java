package net.monolith.hud;

import net.minecraft.client.gui.DrawContext;
import net.monolith.render.Mre2D;

public abstract class HudElement {
   public int x;
   public int y;
   public int width;
   public int height;
   public boolean dragging;
   private int dragX;
   private int dragY;

   public HudElement(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   public abstract void render(DrawContext var1, float var2);

   public void renderEditMode(DrawContext context, int mouseX, int mouseY) {
      if (this.dragging) {
         this.x = mouseX - this.dragX;
         this.y = mouseY - this.dragY;
      }

      Mre2D renderer = Mre2D.of(context);
      renderer.blur(
         (float)(this.x - 3), (float)(this.y - 3), (float)(this.width + 6), (float)(this.height + 6), 4.0F, 14.0F, this.dragging ? 1883898080 : 1291845631
      );
      renderer.roundedOutline((float)(this.x - 3), (float)(this.y - 3), (float)(this.width + 6), (float)(this.height + 6), 4.0F, 1.0F, -1442781697);
      this.render(context, 0.0F);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0
         && mouseX >= (double)this.x
         && mouseX <= (double)(this.x + this.width)
         && mouseY >= (double)this.y
         && mouseY <= (double)(this.y + this.height)) {
         this.dragging = true;
         this.dragX = (int)(mouseX - (double)this.x);
         this.dragY = (int)(mouseY - (double)this.y);
         return true;
      } else {
         return false;
      }
   }

   public void mouseReleased(int button) {
      if (button == 0) {
         this.dragging = false;
      }
   }
}

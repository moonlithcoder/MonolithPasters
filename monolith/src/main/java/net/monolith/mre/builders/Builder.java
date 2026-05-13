package net.monolith.mre.builders;

import net.monolith.mre.builders.impl.BlurBuilder;
import net.monolith.mre.builders.impl.BorderBuilder;
import net.monolith.mre.builders.impl.RectangleBuilder;
import net.monolith.mre.builders.impl.TextBuilder;
import net.monolith.mre.builders.impl.TextureBuilder;

public final class Builder {
   private static final RectangleBuilder RECTANGLE_BUILDER = new RectangleBuilder();
   private static final BorderBuilder BORDER_BUILDER = new BorderBuilder();
   private static final TextureBuilder TEXTURE_BUILDER = new TextureBuilder();
   private static final TextBuilder TEXT_BUILDER = new TextBuilder();
   private static final BlurBuilder BLUR_BUILDER = new BlurBuilder();

   public static RectangleBuilder rectangle() {
      return RECTANGLE_BUILDER;
   }

   public static BorderBuilder border() {
      return BORDER_BUILDER;
   }

   public static TextureBuilder texture() {
      return TEXTURE_BUILDER;
   }

   public static TextBuilder text() {
      return TEXT_BUILDER;
   }

   public static BlurBuilder blur() {
      return BLUR_BUILDER;
   }
}

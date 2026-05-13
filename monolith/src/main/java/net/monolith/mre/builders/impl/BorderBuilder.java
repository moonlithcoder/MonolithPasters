package net.monolith.mre.builders.impl;

import net.monolith.mre.builders.AbstractBuilder;
import net.monolith.mre.builders.states.QuadColorState;
import net.monolith.mre.builders.states.QuadRadiusState;
import net.monolith.mre.builders.states.SizeState;
import net.monolith.mre.renderers.impl.BuiltBorder;

public final class BorderBuilder extends AbstractBuilder<BuiltBorder> {
   private SizeState size;
   private QuadRadiusState radius;
   private QuadColorState color;
   private float thickness;
   private float internalSmoothness;
   private float externalSmoothness;

   public BorderBuilder size(SizeState size) {
      this.size = size;
      return this;
   }

   public BorderBuilder radius(QuadRadiusState radius) {
      this.radius = radius;
      return this;
   }

   public BorderBuilder color(QuadColorState color) {
      this.color = color;
      return this;
   }

   public BorderBuilder thickness(float thickness) {
      this.thickness = thickness;
      return this;
   }

   public BorderBuilder smoothness(float internalSmoothness, float externalSmoothness) {
      this.internalSmoothness = internalSmoothness;
      this.externalSmoothness = externalSmoothness;
      return this;
   }

   protected BuiltBorder _build() {
      return new BuiltBorder(this.size, this.radius, this.color, this.thickness, this.internalSmoothness, this.externalSmoothness);
   }

   @Override
   protected void reset() {
      this.size = SizeState.NONE;
      this.radius = QuadRadiusState.NO_ROUND;
      this.color = QuadColorState.TRANSPARENT;
      this.thickness = 0.0F;
      this.internalSmoothness = 1.0F;
      this.externalSmoothness = 1.0F;
   }
}

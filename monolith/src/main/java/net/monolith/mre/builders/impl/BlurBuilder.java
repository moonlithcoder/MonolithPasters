package net.monolith.mre.builders.impl;

import net.monolith.mre.builders.AbstractBuilder;
import net.monolith.mre.builders.states.QuadColorState;
import net.monolith.mre.builders.states.QuadRadiusState;
import net.monolith.mre.builders.states.SizeState;
import net.monolith.mre.renderers.impl.BuiltBlur;

public final class BlurBuilder extends AbstractBuilder<BuiltBlur> {
   private SizeState size;
   private QuadRadiusState radius;
   private QuadColorState color;
   private float smoothness;
   private float blurRadius;

   public BlurBuilder size(SizeState size) {
      this.size = size;
      return this;
   }

   public BlurBuilder radius(QuadRadiusState radius) {
      this.radius = radius;
      return this;
   }

   public BlurBuilder color(QuadColorState color) {
      this.color = color;
      return this;
   }

   public BlurBuilder smoothness(float smoothness) {
      this.smoothness = smoothness;
      return this;
   }

   public BlurBuilder blurRadius(float blurRadius) {
      this.blurRadius = blurRadius;
      return this;
   }

   protected BuiltBlur _build() {
      return new BuiltBlur(this.size, this.radius, this.color, this.smoothness, this.blurRadius);
   }

   @Override
   protected void reset() {
      this.size = SizeState.NONE;
      this.radius = QuadRadiusState.NO_ROUND;
      this.color = QuadColorState.WHITE;
      this.smoothness = 1.0F;
      this.blurRadius = 0.0F;
   }
}

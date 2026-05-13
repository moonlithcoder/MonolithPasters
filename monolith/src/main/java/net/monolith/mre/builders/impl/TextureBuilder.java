package net.monolith.mre.builders.impl;

import net.minecraft.client.texture.AbstractTexture;
import net.monolith.mre.builders.AbstractBuilder;
import net.monolith.mre.builders.states.QuadColorState;
import net.monolith.mre.builders.states.QuadRadiusState;
import net.monolith.mre.builders.states.SizeState;
import net.monolith.mre.renderers.impl.BuiltTexture;

public final class TextureBuilder extends AbstractBuilder<BuiltTexture> {
   private SizeState size;
   private QuadRadiusState radius;
   private QuadColorState color;
   private float smoothness;
   private float u;
   private float v;
   private float texWidth;
   private float texHeight;
   private AbstractTexture texture;

   public TextureBuilder size(SizeState size) {
      this.size = size;
      return this;
   }

   public TextureBuilder radius(QuadRadiusState radius) {
      this.radius = radius;
      return this;
   }

   public TextureBuilder color(QuadColorState color) {
      this.color = color;
      return this;
   }

   public TextureBuilder smoothness(float smoothness) {
      this.smoothness = smoothness;
      return this;
   }

   public TextureBuilder texture(float u, float v, float texWidth, float texHeight, AbstractTexture texture) {
      this.u = u;
      this.v = v;
      this.texWidth = texWidth;
      this.texHeight = texHeight;
      this.texture = texture;
      return this;
   }

   protected BuiltTexture _build() {
      return new BuiltTexture(this.size, this.radius, this.color, this.smoothness, this.u, this.v, this.texWidth, this.texHeight, this.texture);
   }

   @Override
   protected void reset() {
      this.size = SizeState.NONE;
      this.radius = QuadRadiusState.NO_ROUND;
      this.color = QuadColorState.WHITE;
      this.smoothness = 1.0F;
      this.u = 0.0F;
      this.v = 0.0F;
      this.texWidth = 0.0F;
      this.texHeight = 0.0F;
      this.texture = null;
   }
}

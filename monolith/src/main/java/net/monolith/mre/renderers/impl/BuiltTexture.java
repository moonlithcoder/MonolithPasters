package net.monolith.mre.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.monolith.mre.builders.states.QuadColorState;
import net.monolith.mre.builders.states.QuadRadiusState;
import net.monolith.mre.builders.states.SizeState;
import net.monolith.mre.renderers.IRenderer;
import org.joml.Matrix4f;

public record BuiltTexture(
   SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float u, float v, float texWidth, float texHeight, AbstractTexture texture
) implements IRenderer {
   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      float width = this.size.width();
      float height = this.size.height();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      if (this.texture != null) {
         RenderSystem.setShaderTexture(0, this.texture.getGlId());
      }

      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      builder.vertex(matrix, x, y, z).texture(this.u, this.v).color(this.color.color1());
      builder.vertex(matrix, x, y + height, z).texture(this.u, this.v + this.texHeight).color(this.color.color2());
      builder.vertex(matrix, x + width, y + height, z).texture(this.u + this.texWidth, this.v + this.texHeight).color(this.color.color3());
      builder.vertex(matrix, x + width, y, z).texture(this.u + this.texWidth, this.v).color(this.color.color4());
      BufferRenderer.drawWithGlobalProgram(builder.end());
      RenderSystem.enableCull();
   }
}

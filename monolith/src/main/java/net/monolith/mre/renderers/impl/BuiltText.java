package net.monolith.mre.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.monolith.mre.msdf.MsdfFont;
import net.monolith.mre.renderers.IRenderer;
import org.joml.Matrix4f;

public record BuiltText(
   MsdfFont font, String text, float size, float thickness, int color, float smoothness, float spacing, int outlineColor, float outlineThickness
) implements IRenderer {
   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      RenderSystem.setShaderTexture(0, this.font.getTexture().getGlId());
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      this.font
         .applyGlyphs(
            matrix,
            builder,
            this.text,
            this.size,
            (this.thickness + this.outlineThickness * 0.5F) * 0.5F * this.size,
            this.spacing,
            x,
            y + this.font.getMetrics().baselineHeight() * this.size,
            z,
            this.color
         );
      BufferRenderer.drawWithGlobalProgram(builder.end());
      RenderSystem.enableCull();
   }
}

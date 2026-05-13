package net.monolith.mre.renderers.impl;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.monolith.mre.builders.states.QuadColorState;
import net.monolith.mre.builders.states.QuadRadiusState;
import net.monolith.mre.builders.states.SizeState;
import net.monolith.mre.renderers.IRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;

public record BuiltBlur(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float blurRadius) implements IRenderer {
   private static int program;

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      float width = this.size.width();
      float height = this.size.height();
      if (!(width <= 0.0F) && !(height <= 0.0F)) {
         ensureProgram();
         if (program == 0) {
            this.fallback(matrix, x, y, z);
         } else {
            MinecraftClient client = MinecraftClient.getInstance();
            Framebuffer framebuffer = client.getFramebuffer();
            double scale = client.getWindow().getScaleFactor();
            float fbWidth = (float)framebuffer.textureWidth;
            float fbHeight = (float)framebuffer.textureHeight;
            float rx = (float)((double)x * scale);
            float ry = (float)((double)fbHeight - (double)(y + height) * scale);
            float rw = (float)((double)width * scale);
            float rh = (float)((double)height * scale);
            int overlay = this.color.color1();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            framebuffer.beginRead();
            GL20.glUseProgram(program);
            uniform1i("Sampler0", 0);
            uniform2f("ScreenSize", fbWidth, fbHeight);
            uniform4f("Rect", rx, ry, rw, rh);
            uniform1f("Radius", Math.max(0.0F, this.maxRadius()) * (float)scale);
            uniform1f("BlurRadius", Math.max(1.0F, this.blurRadius) * (float)scale);
            uniform4f(
               "OverlayColor", (float)red(overlay) / 255.0F, (float)green(overlay) / 255.0F, (float)blue(overlay) / 255.0F, (float)alpha(overlay) / 255.0F
            );
            uniformMatrix("ModelViewMat", RenderSystem.getModelViewMatrix());
            uniformMatrix("ProjMat", RenderSystem.getProjectionMatrix());
            BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION);
            builder.vertex(matrix, x, y, z);
            builder.vertex(matrix, x, y + height, z);
            builder.vertex(matrix, x + width, y + height, z);
            builder.vertex(matrix, x + width, y, z);
            BufferRenderer.draw(builder.end());
            GL20.glUseProgram(0);
            framebuffer.endRead();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
         }
      }
   }

   private void fallback(Matrix4f matrix, float x, float y, float z) {
      float spread = Math.max(2.0F, this.blurRadius * 0.75F);
      int base = this.color.color1();
      new BuiltRectangle(expand(this.size, spread * 2.0F), this.radius, faded(base, 20), this.smoothness).render(matrix, x - spread, y - spread, z);
      new BuiltRectangle(expand(this.size, spread * 1.45F), this.radius, faded(base, 28), this.smoothness)
         .render(matrix, x - spread * 0.72F, y - spread * 0.72F, z);
      new BuiltRectangle(expand(this.size, spread * 0.9F), this.radius, faded(-1, 18), this.smoothness)
         .render(matrix, x - spread * 0.45F, y - spread * 0.45F, z);
      new BuiltRectangle(this.size, this.radius, this.color, this.smoothness).render(matrix, x, y, z);
      new BuiltRectangle(this.size, this.radius, faded(-1, 16), this.smoothness).render(matrix, x, y, z);
   }

   private static void ensureProgram() {
      if (program == 0) {
         int vertex = compile(
            35633,
            "#version 150\nin vec3 Position;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);\n}\n"
         );
         int fragment = compile(
            35632,
            "#version 150\nuniform sampler2D Sampler0;\nuniform vec2 ScreenSize;\nuniform vec4 Rect;\nuniform float Radius;\nuniform float BlurRadius;\nuniform vec4 OverlayColor;\nout vec4 fragColor;\nfloat roundedBoxSdf(vec2 center, vec2 size, float radius) {\n    vec2 q = abs(center) - size + radius;\n    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;\n}\nvoid main() {\n    vec2 frag = gl_FragCoord.xy;\n    vec2 center = Rect.xy + Rect.zw * 0.5;\n    float dist = roundedBoxSdf(frag - center, Rect.zw * 0.5, Radius);\n    float mask = 1.0 - smoothstep(-1.0, 1.0, dist);\n    if (mask <= 0.0) discard;\n    vec2 uv = frag / ScreenSize;\n    vec2 px = 1.0 / ScreenSize;\n    float r = max(1.0, BlurRadius);\n    vec4 sum = vec4(0.0);\n    sum += texture(Sampler0, uv + px * vec2(-r, -r)) * 0.0625;\n    sum += texture(Sampler0, uv + px * vec2( 0, -r)) * 0.125;\n    sum += texture(Sampler0, uv + px * vec2( r, -r)) * 0.0625;\n    sum += texture(Sampler0, uv + px * vec2(-r,  0)) * 0.125;\n    sum += texture(Sampler0, uv) * 0.25;\n    sum += texture(Sampler0, uv + px * vec2( r,  0)) * 0.125;\n    sum += texture(Sampler0, uv + px * vec2(-r,  r)) * 0.0625;\n    sum += texture(Sampler0, uv + px * vec2( 0,  r)) * 0.125;\n    sum += texture(Sampler0, uv + px * vec2( r,  r)) * 0.0625;\n    vec4 color = mix(sum, OverlayColor, OverlayColor.a);\n    color.rgb += vec3(0.035);\n    color.a = mask * max(0.55, OverlayColor.a);\n    fragColor = color;\n}\n"
         );
         if (vertex != 0 && fragment != 0) {
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vertex);
            GL20.glAttachShader(program, fragment);
            GL20.glBindAttribLocation(program, 0, "Position");
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, 35714) == 0) {
               GL20.glDeleteProgram(program);
               program = 0;
            }

            GL20.glDeleteShader(vertex);
            GL20.glDeleteShader(fragment);
         }
      }
   }

   private static int compile(int type, String source) {
      int shader = GL20.glCreateShader(type);
      GL20.glShaderSource(shader, source);
      GL20.glCompileShader(shader);
      if (GL20.glGetShaderi(shader, 35713) == 0) {
         GL20.glDeleteShader(shader);
         return 0;
      } else {
         return shader;
      }
   }

   private static void uniform1i(String name, int value) {
      int loc = GL20.glGetUniformLocation(program, name);
      if (loc >= 0) {
         GL20.glUniform1i(loc, value);
      }
   }

   private static void uniform1f(String name, float value) {
      int loc = GL20.glGetUniformLocation(program, name);
      if (loc >= 0) {
         GL20.glUniform1f(loc, value);
      }
   }

   private static void uniform2f(String name, float v1, float v2) {
      int loc = GL20.glGetUniformLocation(program, name);
      if (loc >= 0) {
         GL20.glUniform2f(loc, v1, v2);
      }
   }

   private static void uniform4f(String name, float v1, float v2, float v3, float v4) {
      int loc = GL20.glGetUniformLocation(program, name);
      if (loc >= 0) {
         GL20.glUniform4f(loc, v1, v2, v3, v4);
      }
   }

   private static void uniformMatrix(String name, Matrix4f matrix) {
      int loc = GL20.glGetUniformLocation(program, name);
      if (loc >= 0) {
         GL20.glUniformMatrix4fv(loc, false, matrix.get(new float[16]));
      }
   }

   private float maxRadius() {
      return Math.max(Math.max(this.radius.radius1(), this.radius.radius2()), Math.max(this.radius.radius3(), this.radius.radius4()));
   }

   private static SizeState expand(SizeState size, float amount) {
      return new SizeState(size.width() + amount, size.height() + amount);
   }

   private static QuadColorState faded(int color, int alpha) {
      return new QuadColorState(Math.max(0, Math.min(255, alpha)) << 24 | color & 16777215);
   }

   private static int alpha(int c) {
      return c >>> 24 & 0xFF;
   }

   private static int red(int c) {
      return c >>> 16 & 0xFF;
   }

   private static int green(int c) {
      return c >>> 8 & 0xFF;
   }

   private static int blue(int c) {
      return c & 0xFF;
   }
}

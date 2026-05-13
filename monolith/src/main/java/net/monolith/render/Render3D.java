package net.monolith.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class Render3D {
   private final WorldRenderContext context;
   private final MatrixStack matrices;
   private final Camera cameraObject;
   private final Vec3d camera;

   private Render3D(WorldRenderContext context) {
      this.context = context;
      this.matrices = context.matrixStack();
      this.cameraObject = context.camera();
      this.camera = this.cameraObject.getPos();
   }

   public static Render3D of(WorldRenderContext context) {
      return new Render3D(context);
   }

   public MatrixStack matrices() {
      return this.matrices;
   }

   public void push() {
      this.matrices.push();
   }

   public void pop() {
      this.matrices.pop();
   }

   public void line(Vec3d from, Vec3d to, int color) {
      this.drawLines(color, builder -> {
         this.addLineVertex(builder, from.x, from.y, from.z, color);
         this.addLineVertex(builder, to.x, to.y, to.z, color);
      });
   }

   public void box(Box box, int color) {
      Box b = box.offset(-this.camera.x, -this.camera.y, -this.camera.z);
      this.drawLines(color, builder -> {
         this.line(builder, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, color);
         this.line(builder, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, color);
         this.line(builder, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, color);
         this.line(builder, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ, color);
         this.line(builder, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, color);
         this.line(builder, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, color);
         this.line(builder, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, color);
         this.line(builder, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, color);
         this.line(builder, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, color);
         this.line(builder, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, color);
         this.line(builder, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, color);
         this.line(builder, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, color);
      });
   }

   public void filledBox(Box box, int color) {
      if (alpha(color) > 0) {
         Box b = box.offset(-this.camera.x, -this.camera.y, -this.camera.z);
         this.drawQuads(
            color,
            builder -> {
               this.quad(
                  builder,
                  b.minX,
                  b.minY,
                  b.minZ,
                  b.maxX,
                  b.minY,
                  b.minZ,
                  b.maxX,
                  b.maxY,
                  b.minZ,
                  b.minX,
                  b.maxY,
                  b.minZ,
                  color
               );
               this.quad(
                  builder,
                  b.maxX,
                  b.minY,
                  b.maxZ,
                  b.minX,
                  b.minY,
                  b.maxZ,
                  b.minX,
                  b.maxY,
                  b.maxZ,
                  b.maxX,
                  b.maxY,
                  b.maxZ,
                  color
               );
               this.quad(
                  builder,
                  b.minX,
                  b.minY,
                  b.maxZ,
                  b.minX,
                  b.minY,
                  b.minZ,
                  b.minX,
                  b.maxY,
                  b.minZ,
                  b.minX,
                  b.maxY,
                  b.maxZ,
                  color
               );
               this.quad(
                  builder,
                  b.maxX,
                  b.minY,
                  b.minZ,
                  b.maxX,
                  b.minY,
                  b.maxZ,
                  b.maxX,
                  b.maxY,
                  b.maxZ,
                  b.maxX,
                  b.maxY,
                  b.minZ,
                  color
               );
               this.quad(
                  builder,
                  b.minX,
                  b.maxY,
                  b.minZ,
                  b.maxX,
                  b.maxY,
                  b.minZ,
                  b.maxX,
                  b.maxY,
                  b.maxZ,
                  b.minX,
                  b.maxY,
                  b.maxZ,
                  color
               );
               this.quad(
                  builder,
                  b.minX,
                  b.minY,
                  b.maxZ,
                  b.maxX,
                  b.minY,
                  b.maxZ,
                  b.maxX,
                  b.minY,
                  b.minZ,
                  b.minX,
                  b.minY,
                  b.minZ,
                  color
               );
            }
         );
      }
   }

   public void ribbon(Vec3d left1, Vec3d right1, Vec3d right2, Vec3d left2, int color1, int color2) {
      if (alpha(color1) > 0 || alpha(color2) > 0) {
         this.drawQuads(-1, builder -> {
            this.ribbonVertex(builder, left1, color1);
            this.ribbonVertex(builder, right1, color1);
            this.ribbonVertex(builder, right2, color2);
            this.ribbonVertex(builder, left2, color2);
         });
      }
   }

   public void star(Vec3d center, float size, int color) {
      double x = center.x - this.camera.x;
      double y = center.y - this.camera.y;
      double z = center.z - this.camera.z;
      this.drawLines(color, builder -> {
         this.line(builder, x - (double)size, y, z, x + (double)size, y, z, color);
         this.line(builder, x, y - (double)size, z, x, y + (double)size, z, color);
         this.line(builder, x, y, z - (double)size, x, y, z + (double)size, color);
         this.line(builder, x - (double)size * 0.55, y - (double)size * 0.55, z, x + (double)size * 0.55, y + (double)size * 0.55, z, color);
         this.line(builder, x - (double)size * 0.55, y + (double)size * 0.55, z, x + (double)size * 0.55, y - (double)size * 0.55, z, color);
      });
   }

   public void fallingStar(Vec3d head, Vec3d velocity, float size, int color, int tailColor) {
      this.star(head, size, color);
      Vec3d tail = head.subtract(velocity.normalize().multiply((double)(size * 9.0F)));
      this.line(head, tail, tailColor);
   }

   public void meteor(Vec3d head, Vec3d velocity, float size, float age, int coreColor, int glowColor, int tailColor, int smokeColor) {
      Vec3d direction = velocity.normalize();
      Vec3d tail = head.subtract(direction.multiply((double)(size * 26.0F)));
      Vec3d farTail = head.subtract(direction.multiply((double)(size * 48.0F)));
      this.line(head, farTail, withAlpha(tailColor, Math.max(18, alpha(tailColor) / 2)));
      this.line(head.add(0.0, (double)size * 0.18, 0.0), tail, tailColor);
      this.line(head.add(0.0, (double)(-size) * 0.12, 0.0), tail, withAlpha(tailColor, Math.max(20, alpha(tailColor) * 2 / 3)));
      this.star(head, size * 1.7F, glowColor);
      this.star(head, size * 0.72F, coreColor);
      if (((int)age & 1) == 0) {
         Vec3d spark = head.subtract(direction.multiply((double)(size * (8.0F + age % 6.0F))));
         this.star(spark, size * 0.36F, withAlpha(coreColor, Math.max(35, alpha(coreColor) / 2)));
      }

      Vec3d smoke = head.subtract(direction.multiply((double)(size * 18.0F)));
      this.star(smoke, size * 0.55F, smokeColor);
   }

   public void billboard(Identifier texture, Vec3d center, float size, float rotation, int color) {
      if (alpha(color) > 0) {
         Vec3d relative = center.subtract(this.camera);
         Vector3f right = new Vector3f(this.cameraObject.getHorizontalPlane());
         Vector3f up = new Vector3f(this.cameraObject.getVerticalPlane());
         float sin = (float)Math.sin((double)rotation);
         float cos = (float)Math.cos((double)rotation);
         Vector3f rightRot = new Vector3f(right).mul(cos).add(new Vector3f(up).mul(sin));
         Vector3f upRot = new Vector3f(up).mul(cos).sub(new Vector3f(right).mul(sin));
         float half = size / 2.0F;
         Vector3f r = rightRot.mul(half);
         Vector3f u = upRot.mul(half);
         this.drawTexturedQuads(texture, color, builder -> {
            this.texturedVertex(builder, relative, (double)(-r.x - u.x), (double)(-r.y - u.y), (double)(-r.z - u.z), 0.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x - u.x), (double)(r.y - u.y), (double)(r.z - u.z), 1.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x + u.x), (double)(r.y + u.y), (double)(r.z + u.z), 1.0F, 0.0F, color);
            this.texturedVertex(builder, relative, (double)(-r.x + u.x), (double)(-r.y + u.y), (double)(-r.z + u.z), 0.0F, 0.0F, color);
         });
      }
   }

   public void billboardAdditive(Identifier texture, Vec3d center, float size, float rotation, int color) {
      if (alpha(color) > 0) {
         Vec3d relative = center.subtract(this.camera);
         Vector3f right = new Vector3f(this.cameraObject.getHorizontalPlane());
         Vector3f up = new Vector3f(this.cameraObject.getVerticalPlane());
         float sin = (float)Math.sin((double)rotation);
         float cos = (float)Math.cos((double)rotation);
         Vector3f rightRot = new Vector3f(right).mul(cos).add(new Vector3f(up).mul(sin));
         Vector3f upRot = new Vector3f(up).mul(cos).sub(new Vector3f(right).mul(sin));
         float half = size / 2.0F;
         Vector3f r = rightRot.mul(half);
         Vector3f u = upRot.mul(half);
         this.drawTexturedQuadsAdditive(texture, color, builder -> {
            this.texturedVertex(builder, relative, (double)(-r.x - u.x), (double)(-r.y - u.y), (double)(-r.z - u.z), 0.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x - u.x), (double)(r.y - u.y), (double)(r.z - u.z), 1.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x + u.x), (double)(r.y + u.y), (double)(r.z + u.z), 1.0F, 0.0F, color);
            this.texturedVertex(builder, relative, (double)(-r.x + u.x), (double)(-r.y + u.y), (double)(-r.z + u.z), 0.0F, 0.0F, color);
         });
      }
   }

   public void billboardDepth(Identifier texture, Vec3d center, float size, float rotation, int color) {
      if (alpha(color) > 0) {
         Vec3d relative = center.subtract(this.camera);
         Vector3f right = new Vector3f(this.cameraObject.getHorizontalPlane());
         Vector3f up = new Vector3f(this.cameraObject.getVerticalPlane());
         float sin = (float)Math.sin((double)rotation);
         float cos = (float)Math.cos((double)rotation);
         Vector3f rightRot = new Vector3f(right).mul(cos).add(new Vector3f(up).mul(sin));
         Vector3f upRot = new Vector3f(up).mul(cos).sub(new Vector3f(right).mul(sin));
         float half = size / 2.0F;
         Vector3f r = rightRot.mul(half);
         Vector3f u = upRot.mul(half);
         this.drawTexturedQuadsDepth(texture, color, builder -> {
            this.texturedVertex(builder, relative, (double)(-r.x - u.x), (double)(-r.y - u.y), (double)(-r.z - u.z), 0.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x - u.x), (double)(r.y - u.y), (double)(r.z - u.z), 1.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x + u.x), (double)(r.y + u.y), (double)(r.z + u.z), 1.0F, 0.0F, color);
            this.texturedVertex(builder, relative, (double)(-r.x + u.x), (double)(-r.y + u.y), (double)(-r.z + u.z), 0.0F, 0.0F, color);
         });
      }
   }

   public void billboardDepthAdditive(Identifier texture, Vec3d center, float size, float rotation, int color) {
      if (alpha(color) > 0) {
         Vec3d relative = center.subtract(this.camera);
         Vector3f right = new Vector3f(this.cameraObject.getHorizontalPlane());
         Vector3f up = new Vector3f(this.cameraObject.getVerticalPlane());
         float sin = (float)Math.sin((double)rotation);
         float cos = (float)Math.cos((double)rotation);
         Vector3f rightRot = new Vector3f(right).mul(cos).add(new Vector3f(up).mul(sin));
         Vector3f upRot = new Vector3f(up).mul(cos).sub(new Vector3f(right).mul(sin));
         float half = size / 2.0F;
         Vector3f r = rightRot.mul(half);
         Vector3f u = upRot.mul(half);
         this.drawTexturedQuadsDepthAdditive(texture, color, builder -> {
            this.texturedVertex(builder, relative, (double)(-r.x - u.x), (double)(-r.y - u.y), (double)(-r.z - u.z), 0.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x - u.x), (double)(r.y - u.y), (double)(r.z - u.z), 1.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x + u.x), (double)(r.y + u.y), (double)(r.z + u.z), 1.0F, 0.0F, color);
            this.texturedVertex(builder, relative, (double)(-r.x + u.x), (double)(-r.y + u.y), (double)(-r.z + u.z), 0.0F, 0.0F, color);
         });
      }
   }

   public void billboardTrail(Identifier texture, Vec3d head, Vec3d velocity, float width, float length, int color) {
      if (alpha(color) > 0) {
         Vec3d direction = velocity.normalize();
         Vec3d center = head.subtract(direction.multiply((double)length * 0.5));
         Vec3d relative = center.subtract(this.camera);
         Vec3d side = direction.crossProduct(head.subtract(this.camera).normalize());
         if (side.lengthSquared() < 1.0E-4) {
            side = new Vec3d(1.0, 0.0, 0.0);
         }

         Vec3d trailSide = side.normalize().multiply((double)width * 0.5);
         Vec3d along = direction.multiply((double)length * 0.5);
         this.drawTexturedQuads(
            texture,
            color,
            builder -> {
               this.texturedVertex(
                  builder,
                  relative,
                  -trailSide.x - along.x,
                  -trailSide.y - along.y,
                  -trailSide.z - along.z,
                  0.0F,
                  1.0F,
                  color
               );
               this.texturedVertex(
                  builder,
                  relative,
                  trailSide.x - along.x,
                  trailSide.y - along.y,
                  trailSide.z - along.z,
                  1.0F,
                  1.0F,
                  color
               );
               this.texturedVertex(
                  builder,
                  relative,
                  trailSide.x + along.x,
                  trailSide.y + along.y,
                  trailSide.z + along.z,
                  1.0F,
                  0.0F,
                  color
               );
               this.texturedVertex(
                  builder,
                  relative,
                  -trailSide.x + along.x,
                  -trailSide.y + along.y,
                  -trailSide.z + along.z,
                  0.0F,
                  0.0F,
                  color
               );
            }
         );
      }
   }

   public void verticalBillboard(Identifier texture, Vec3d center, float width, float height, float rotation, int color) {
      if (alpha(color) > 0) {
         Vec3d relative = center.subtract(this.camera);
         Vector3f right = new Vector3f(this.cameraObject.getHorizontalPlane());
         right.y = 0.0F;
         if (right.lengthSquared() < 1.0E-4F) {
            right.set(1.0F, 0.0F, 0.0F);
         }

         right.normalize();
         Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
         float sin = (float)Math.sin((double)rotation);
         float cos = (float)Math.cos((double)rotation);
         Vector3f rightRot = new Vector3f(right).mul(cos).add(new Vector3f(up).mul(sin));
         Vector3f upRot = new Vector3f(up).mul(cos).sub(new Vector3f(right).mul(sin));
         Vector3f r = rightRot.mul(width / 2.0F);
         Vector3f u = upRot.mul(height / 2.0F);
         this.drawTexturedQuads(texture, color, builder -> {
            this.texturedVertex(builder, relative, (double)(-r.x - u.x), (double)(-r.y - u.y), (double)(-r.z - u.z), 0.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x - u.x), (double)(r.y - u.y), (double)(r.z - u.z), 1.0F, 1.0F, color);
            this.texturedVertex(builder, relative, (double)(r.x + u.x), (double)(r.y + u.y), (double)(r.z + u.z), 1.0F, 0.0F, color);
            this.texturedVertex(builder, relative, (double)(-r.x + u.x), (double)(-r.y + u.y), (double)(-r.z + u.z), 0.0F, 0.0F, color);
         });
      }
   }

   public void ring(Vec3d center, float radius, float yOffset, int color) {
      Vec3d c = center.add(0.0, (double)yOffset, 0.0);
      this.drawLines(
         color,
         builder -> {
            int segments = 80;

            for (int i = 0; i < segments; i++) {
               double a1 = (Math.PI * 2) * (double)i / (double)segments;
               double a2 = (Math.PI * 2) * (double)(i + 1) / (double)segments;
               this.line(
                  builder,
                  c.x - this.camera.x + Math.cos(a1) * (double)radius,
                  c.y - this.camera.y,
                  c.z - this.camera.z + Math.sin(a1) * (double)radius,
                  c.x - this.camera.x + Math.cos(a2) * (double)radius,
                  c.y - this.camera.y,
                  c.z - this.camera.z + Math.sin(a2) * (double)radius,
                  color
               );
            }
         }
      );
   }

   public void diamondLines(Vec3d center, float width, float height, float rotation, int color) {
      Vec3d relative = center.subtract(this.camera);
      Vector3f right = new Vector3f(this.cameraObject.getHorizontalPlane()).normalize();
      Vector3f up = new Vector3f(this.cameraObject.getVerticalPlane()).normalize();
      float sin = (float)Math.sin((double)rotation);
      float cos = (float)Math.cos((double)rotation);
      Vector3f rightRot = new Vector3f(right).mul(cos).add(new Vector3f(up).mul(sin));
      Vector3f upRot = new Vector3f(up).mul(cos).sub(new Vector3f(right).mul(sin));
      Vector3f r = rightRot.mul(width / 2.0F);
      Vector3f u = upRot.mul(height / 2.0F);
      this.drawLines(
         color,
         builder -> {
            this.line(
               builder,
               relative.x + (double)u.x,
               relative.y + (double)u.y,
               relative.z + (double)u.z,
               relative.x + (double)r.x,
               relative.y + (double)r.y,
               relative.z + (double)r.z,
               color
            );
            this.line(
               builder,
               relative.x + (double)r.x,
               relative.y + (double)r.y,
               relative.z + (double)r.z,
               relative.x - (double)u.x,
               relative.y - (double)u.y,
               relative.z - (double)u.z,
               color
            );
            this.line(
               builder,
               relative.x - (double)u.x,
               relative.y - (double)u.y,
               relative.z - (double)u.z,
               relative.x - (double)r.x,
               relative.y - (double)r.y,
               relative.z - (double)r.z,
               color
            );
            this.line(
               builder,
               relative.x - (double)r.x,
               relative.y - (double)r.y,
               relative.z - (double)r.z,
               relative.x + (double)u.x,
               relative.y + (double)u.y,
               relative.z + (double)u.z,
               color
            );
         }
      );
   }

   public void nametag(Vec3d center, String line, float scale, int textColor, int backgroundColor) {
      if (line != null && !line.isEmpty()) {
         MinecraftClient client = MinecraftClient.getInstance();
         TextRenderer textRenderer = client.textRenderer;
         Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
         this.matrices.push();
         this.matrices
            .translate(center.x - this.camera.x, center.y - this.camera.y, center.z - this.camera.z);
         this.matrices.multiply(new Quaternionf(this.cameraObject.getRotation()));
         this.matrices.scale(-scale, -scale, scale);
         Matrix4f matrix = this.matrices.peek().getPositionMatrix();
         Text styled = Text.literal(line);
         float width = (float)textRenderer.getWidth(styled);
         int left = (int)(-width / 2.0F) - 5;
         this.roundedQuad2d(matrix, (float)left, -5.0F, width + 10.0F, 14.0F, 2.0F, backgroundColor);
         textRenderer.draw(styled, -width / 2.0F, -1.0F, textColor, false, matrix, consumers, TextLayerType.SEE_THROUGH, 0, 15728880);
         consumers.draw();
         this.matrices.pop();
      }
   }

   public WorldRenderContext context() {
      return this.context;
   }

   private void drawLines(int color, Render3D.Drawer drawer) {
      if (alpha(color) > 0) {
         this.setup();
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         drawer.draw(builder);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         this.teardown();
      }
   }

   private void drawQuads(int color, Render3D.Drawer drawer) {
      if (alpha(color) > 0) {
         this.setup();
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         drawer.draw(builder);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         this.teardown();
      }
   }

   private void drawTexturedQuads(Identifier texture, int color, Render3D.Drawer drawer) {
      if (alpha(color) > 0) {
         this.setup();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, texture);
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         drawer.draw(builder);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         this.teardown();
      }
   }

   private void drawTexturedQuadsAdditive(Identifier texture, int color, Render3D.Drawer drawer) {
      if (alpha(color) > 0) {
         this.setup();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, texture);
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         drawer.draw(builder);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         this.teardown();
      }
   }

   private void drawTexturedQuadsDepth(Identifier texture, int color, Render3D.Drawer drawer) {
      if (alpha(color) > 0) {
         this.setup();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, texture);
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         drawer.draw(builder);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.depthMask(true);
         this.teardown();
      }
   }

   private void drawTexturedQuadsDepthAdditive(Identifier texture, int color, Render3D.Drawer drawer) {
      if (alpha(color) > 0) {
         this.setup();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, texture);
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         drawer.draw(builder);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         this.teardown();
      }
   }

   private void setup() {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
   }

   private void teardown() {
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableCull();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void addLineVertex(BufferBuilder builder, double x, double y, double z, int color) {
      Vec3d translated = new Vec3d(x - this.camera.x, y - this.camera.y, z - this.camera.z);
      this.addVertex(builder, translated.x, translated.y, translated.z, color);
   }

   private void line(BufferBuilder builder, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      this.addVertex(builder, x1, y1, z1, color);
      this.addVertex(builder, x2, y2, z2, color);
   }

   private void quad(
      BufferBuilder builder,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      int color
   ) {
      this.addVertex(builder, x1, y1, z1, color);
      this.addVertex(builder, x2, y2, z2, color);
      this.addVertex(builder, x3, y3, z3, color);
      this.addVertex(builder, x4, y4, z4, color);
   }

   private void roundedQuad2d(Matrix4f matrix, float x, float y, float width, float height, float radius, int color) {
      if (alpha(color) > 0) {
         this.setup();
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
         builder.vertex(matrix, x + width / 2.0F, y + height / 2.0F, 0.0F).color(color);
         this.arc2d(builder, matrix, x + width - radius, y + radius, radius, -90.0F, 0.0F, color);
         this.arc2d(builder, matrix, x + width - radius, y + height - radius, radius, 0.0F, 90.0F, color);
         this.arc2d(builder, matrix, x + radius, y + height - radius, radius, 90.0F, 180.0F, color);
         this.arc2d(builder, matrix, x + radius, y + radius, radius, 180.0F, 270.0F, color);
         builder.vertex(matrix, x + width - radius, y, 0.0F).color(color);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         this.teardown();
      }
   }

   private void arc2d(BufferBuilder builder, Matrix4f matrix, float centerX, float centerY, float radius, float start, float end, int color) {
      for (int i = 0; i <= 6; i++) {
         float angle = (float)Math.toRadians((double)(start + (end - start) * (float)i / 6.0F));
         builder.vertex(matrix, centerX + (float)Math.cos((double)angle) * radius, centerY + (float)Math.sin((double)angle) * radius, 0.0F).color(color);
      }
   }

   private void ribbonVertex(BufferBuilder builder, Vec3d pos, int color) {
      this.addVertex(builder, pos.x - this.camera.x, pos.y - this.camera.y, pos.z - this.camera.z, color);
   }

   private void texturedVertex(BufferBuilder builder, Vec3d origin, double x, double y, double z, float u, float v, int color) {
      Matrix4f matrix = this.matrices.peek().getPositionMatrix();
      builder.vertex(matrix, (float)(origin.x + x), (float)(origin.y + y), (float)(origin.z + z))
         .texture(u, v)
         .color(color);
   }

   private void addVertex(BufferBuilder builder, double x, double y, double z, int color) {
      Matrix4f matrix = this.matrices.peek().getPositionMatrix();
      builder.vertex(matrix, (float)x, (float)y, (float)z).color(color);
   }

   private static int alpha(int color) {
      return color >>> 24 & 0xFF;
   }

   private static int withAlpha(int color, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | color & 16777215;
   }

   private interface Drawer {
      void draw(BufferBuilder var1);
   }
}

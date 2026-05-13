package net.monolith.visual;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.monolith.combat.AttackAura;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import org.joml.Matrix4f;

public final class TargetEsp {
   private static final Identifier TARGET_MARKER = Identifier.of("monolith", "textures/target.png");
   private static final Identifier GHOST_MARKER = Identifier.of("monolith", "textures/ghost.png");
   private static LivingEntity lastTarget;
   private static float animation;
   private static float rotationAngle;
   private static float rotationSpeed;
   private static float ghostTime;
   private static boolean reversing;

   private TargetEsp() {
   }

   public static void tick() {
      if (animation > 0.0F && lastTarget == null) {
         animation = Math.max(0.0F, animation - 0.08F);
      }
   }

   public static void render(WorldRenderContext context) {
      Module module = ModuleManager.getModule("TargetESP");
      LivingEntity target = AttackAura.target();
      if (module != null && module.enabled) {
         ClientPlayerEntity player = MinecraftClient.getInstance().player;
         if (target == null || target == player) {
            animation = Math.max(0.0F, animation - 0.12F);
         } else if (player != null) {
            lastTarget = target;
            animation = Math.min(1.0F, animation + 0.12F);
         } else {
            animation = Math.max(0.0F, animation - 0.12F);
         }

         if (animation <= 0.01F) {
            lastTarget = null;
         } else if (lastTarget != null) {
            float tickDelta = context.tickCounter().getTickDelta(true);
            if ("Призраки".equals(module.currentMode)) {
               drawGhosts(context.matrixStack(), context.camera(), tickDelta);
            } else {
               drawMarker(context.matrixStack(), context.camera(), tickDelta);
            }
         }
      }
   }

   private static void drawMarker(MatrixStack matrices, Camera camera, float tickDelta) {
      double x = interpolate(lastTarget.getX(), lastTarget.lastRenderX, (double)tickDelta);
      double y = interpolate(lastTarget.getY(), lastTarget.lastRenderY, (double)tickDelta) + (double)lastTarget.getHeight() / 2.0;
      double z = interpolate(lastTarget.getZ(), lastTarget.lastRenderZ, (double)tickDelta);
      Vec3d camPos = camera.getPos();
      matrices.push();
      matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      float hurt = lastTarget.hurtTime > 0 ? MathHelper.clamp((float)lastTarget.hurtTime / 10.0F, 0.0F, 1.0F) : 0.0F;
      float scale = 0.125F * animation * (1.0F + 0.25F * hurt);
      matrices.scale(-scale, -scale, scale);
      updateRotation();
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle));
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      RenderSystem.setShaderTexture(0, TARGET_MARKER);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      float size = 6.5F;
      int color = withAlpha(mix(-16718337, -43691, hurt), (int)(animation * 255.0F));
      Matrix4f mat = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(mat, -size, size, 0.0F).texture(0.0F, 1.0F).color(color);
      buffer.vertex(mat, size, size, 0.0F).texture(1.0F, 1.0F).color(color);
      buffer.vertex(mat, size, -size, 0.0F).texture(1.0F, 0.0F).color(color);
      buffer.vertex(mat, -size, -size, 0.0F).texture(0.0F, 0.0F).color(color);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableCull();
      matrices.pop();
   }

   private static void drawGhosts(MatrixStack matrices, Camera camera, float tickDelta) {
      double x = interpolate(lastTarget.getX(), lastTarget.lastRenderX, (double)tickDelta);
      double y = interpolate(lastTarget.getY(), lastTarget.lastRenderY, (double)tickDelta);
      double z = interpolate(lastTarget.getZ(), lastTarget.lastRenderZ, (double)tickDelta);
      Vec3d camPos = camera.getPos();
      float hurt = lastTarget.hurtTime > 0 ? MathHelper.clamp((float)lastTarget.hurtTime / 10.0F, 0.0F, 1.0F) : 0.0F;
      ghostTime += 0.004F + tickDelta * 0.00125F;
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      RenderSystem.setShaderTexture(0, GHOST_MARKER);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      float c = ghostTime + (float)Math.sin((double)(ghostTime * 0.86F)) * 0.1F;
      boolean forward = true;

      for (int layer = 0; layer < 3; layer++) {
         float direction = layer % 2 == 0 ? 1.0F : -1.0F;
         float start = c * 360.0F * direction + (float)layer * 120.0F;
         float end = start + 78.0F;
         float layerY = 0.28F + (float)layer * 0.5F;

         for (float degrees = start; degrees < end; degrees += 4.0F) {
            float progress = MathHelper.clamp((degrees - start) / 78.0F, 0.0F, 1.0F);
            float wave = forward ? progress : 1.0F - progress;
            double radians = Math.toRadians((double)degrees);
            double yWave = (double)layerY + Math.sin(radians * 1.2F + (double)layer) * 0.06F;
            float radius = 0.44F + (float)layer * 0.08F + progress * 0.04F;
            float q = 0.15F * (Math.max(0.15F, wave) + 0.45F);
            float size = q * 1.6F * animation * (1.0F + 0.25F * hurt);
            double gx = x + Math.cos(radians) * (double)radius;
            double gy = y + yWave + 0.48F;
            double gz = z + Math.sin(radians) * (double)radius;
            int color = withAlpha(mix(-1048675, -34953, hurt), (int)(animation * (55.0F + wave * 120.0F)));
            drawBillboard(matrices, camera, camPos, gx, gy, gz, size, color);
         }

         c *= -1.025F;
         forward = !forward;
      }

      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.enableCull();
   }

   private static void drawBillboard(MatrixStack matrices, Camera camera, Vec3d camPos, double x, double y, double z, float size, int color) {
      matrices.push();
      matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      Matrix4f mat = matrices.peek().getPositionMatrix();
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(mat, -size, size, 0.0F).texture(0.0F, 1.0F).color(color);
      buffer.vertex(mat, size, size, 0.0F).texture(1.0F, 1.0F).color(color);
      buffer.vertex(mat, size, -size, 0.0F).texture(1.0F, 0.0F).color(color);
      buffer.vertex(mat, -size, -size, 0.0F).texture(0.0F, 0.0F).color(color);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      matrices.pop();
   }

   private static void updateRotation() {
      if (!reversing) {
         rotationSpeed += 0.01F;
         if (rotationSpeed > 2.3F) {
            rotationSpeed = 2.3F;
            reversing = true;
         }
      } else {
         rotationSpeed -= 0.01F;
         if (rotationSpeed < -2.3F) {
            rotationSpeed = -2.3F;
            reversing = false;
         }
      }

      rotationAngle = (rotationAngle + rotationSpeed) % 360.0F;
   }

   private static double interpolate(double current, double old, double scale) {
      return old + (current - old) * scale;
   }

   private static int mix(int first, int second, float factor) {
      if (factor <= 0.0F) {
         return first;
      } else if (factor >= 1.0F) {
         return second;
      } else {
         int a = (int)((float)(first >>> 24 & 0xFF) + (float)((second >>> 24 & 0xFF) - (first >>> 24 & 0xFF)) * factor);
         int r = (int)((float)(first >>> 16 & 0xFF) + (float)((second >>> 16 & 0xFF) - (first >>> 16 & 0xFF)) * factor);
         int g = (int)((float)(first >>> 8 & 0xFF) + (float)((second >>> 8 & 0xFF) - (first >>> 8 & 0xFF)) * factor);
         int b = (int)((float)(first & 0xFF) + (float)((second & 0xFF) - (first & 0xFF)) * factor);
         return a << 24 | r << 16 | g << 8 | b;
      }
   }

   private static int withAlpha(int color, int alpha) {
      return MathHelper.clamp(alpha, 0, 255) << 24 | color & 16777215;
   }
}

package net.monolith.visual;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Render3D;

public final class WorldParticles {
   private static final Identifier GHOST = Identifier.of("monolith", "textures/ghost.png");
   private static final List<WorldParticles.Particle> PARTICLES = new CopyOnWriteArrayList<>();
   private static long tickTime;

   private WorldParticles() {
   }

   public static void spawn(LivingEntity entity) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("World particles");
      if (module != null && module.enabled && client.player != null && client.world != null && entity != client.player) {
         Vec3d center = entity.getPos().add(0.0, (double)entity.getHeight() * 0.55, 0.0);
         int count = (int)Math.round(module.getSettingValue("Кол-во за удар", 20.0));

         for (int i = 0; i < count; i++) {
            PARTICLES.add(new WorldParticles.Particle(center));
         }
      }
   }

   public static void tick() {
      tickTime++;
      long now = System.currentTimeMillis();
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null || client.world == null) {
         PARTICLES.clear();
      } else {
         for (WorldParticles.Particle particle : PARTICLES) {
            if (now - particle.time > 7000L || particle.alpha <= 0.0F || client.player.getPos().distanceTo(particle.pos) > 30.0) {
               PARTICLES.remove(particle);
            } else {
               particle.update();
            }
         }
      }
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("World particles");
      if (module != null && module.enabled && client.player != null && client.world != null) {
         Render3D renderer = Render3D.of(context);
         String mode = module.selectedOption("Вид", "Сердечки");
         int index = 0;

         for (WorldParticles.Particle particle : PARTICLES) {
            if (isInView(context.camera().getPos(), particle.pos)) {
               float age = (float)(System.currentTimeMillis() - particle.time) / 7000.0F;
               float size = Math.max(0.02F, (1.0F - age) * particle.alpha);
               int color = color(index++, size);
               if ("Орбизы".equals(mode)) {
                  renderer.billboardAdditive(GHOST, particle.pos, 0.16F * size, particle.spin + (float)tickTime * 0.05F, color);
               } else if ("Молния".equals(mode)) {
                  renderer.fallingStar(particle.pos, particle.velocity, 0.045F * size, color, withAlpha(color, 90));
               } else if ("Снежинки".equals(mode)) {
                  renderer.star(particle.pos, 0.075F * size, color);
                  renderer.star(particle.pos.add(0.0, 0.025 * (double)size, 0.0), 0.045F * size, withAlpha(color, 120));
               } else {
                  renderer.billboardAdditive(GHOST, particle.pos, 0.2F * size, particle.spin, withAlpha(0xFFFF5C8A, color >>> 24));
                  renderer.star(particle.pos, 0.04F * size, withAlpha(0xFFFFD6E3, color >>> 24));
               }
            }
         }
      }
   }

   private static boolean isInView(Vec3d camera, Vec3d pos) {
      return camera.squaredDistanceTo(pos) <= 900.0 && new Box(pos.add(-0.2, -0.2, -0.2), pos.add(0.2, 0.2, 0.2)).getAverageSideLength() > 0.0;
   }

   private static int color(int index, float alpha) {
      float hue = (float)((System.currentTimeMillis() / 22L + (long)index * 18L) % 360L) / 360.0F;
      int rgb = java.awt.Color.HSBtoRGB(hue, 0.75F, 1.0F);
      return withAlpha(rgb, (int)(200.0F * alpha));
   }

   private static int withAlpha(int color, int alpha) {
      return Math.max(0, Math.min(255, alpha)) << 24 | color & 16777215;
   }

   private static final class Particle {
      private Vec3d pos;
      private Vec3d velocity;
      private final long time;
      private final float spin;
      private float alpha = 1.0F;

      private Particle(Vec3d pos) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         this.pos = pos;
         this.velocity = new Vec3d(random.nextDouble(-0.035, 0.035), random.nextDouble(-0.01, 0.045), random.nextDouble(-0.035, 0.035));
         this.time = System.currentTimeMillis();
         this.spin = random.nextFloat() * 6.28F;
      }

      private void update() {
         this.pos = this.pos.add(this.velocity);
         this.velocity = this.velocity.add(0.0, -0.0015, 0.0).multiply(0.992, 0.985, 0.992);
         this.alpha = Math.max(0.0F, 1.0F - (float)(System.currentTimeMillis() - this.time) / 7000.0F);
      }
   }
}

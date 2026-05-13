package net.monolith.visual;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
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
   private static long lastSpawnTime;

   private WorldParticles() {
   }

   public static void tick() {
      tickTime++;
      long now = System.currentTimeMillis();
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("World particles");
      if (module == null || !module.enabled || client.player == null || client.world == null) {
         PARTICLES.clear();
      } else {
         spawnWorldParticles(client, module, now);
         int maxParticles = (int)Math.round(module.getSettingValue("Макс количество", 50.0));
         for (WorldParticles.Particle particle : PARTICLES) {
            if (now - particle.time > particle.lifeTime || particle.alpha <= 0.0F || client.player.getPos().distanceTo(particle.pos) > 64.0) {
               PARTICLES.remove(particle);
            } else {
               particle.update(module);
            }
         }
         while (PARTICLES.size() > maxParticles) {
            PARTICLES.remove(0);
         }
      }
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("World particles");
      if (module != null && module.enabled && client.player != null && client.world != null) {
         Render3D renderer = Render3D.of(context);
         String mode = module.selectedOption("Вид", "Звезды");
         int index = 0;

         for (WorldParticles.Particle particle : PARTICLES) {
            if (isInView(context.camera().getPos(), particle.pos)) {
               float baseSize = (float)module.getSettingValue("Размер", 1.0);
               float size = Math.max(0.02F, 0.12F * baseSize * particle.fade());
               int color = color(index++, size);
               if ("Блум".equals(mode)) {
                  renderer.billboardAdditive(GHOST, particle.pos, size, particle.spin + (float)tickTime * 0.05F, color);
               } else if ("Молния".equals(mode)) {
                  renderer.fallingStar(particle.pos, particle.velocity, size * 0.45F, color, withAlpha(color, 80));
               } else if ("Сердечки".equals(mode)) {
                  renderer.billboardAdditive(GHOST, particle.pos, size * 1.25F, particle.spin, withAlpha(0xFFFF5C8A, color >>> 24));
                  renderer.star(particle.pos, size * 0.28F, withAlpha(0xFFFFD6E3, color >>> 24));
               } else if ("Снег".equals(mode)) {
                  renderer.star(particle.pos, size * 0.65F, withAlpha(0xFFFFFFFF, color >>> 24));
                  renderer.star(particle.pos.add(0.0, 0.025 * (double)size, 0.0), size * 0.35F, withAlpha(color, 120));
               } else {
                  renderer.star(particle.pos, size, color);
               }
            }
         }
      }
   }

   private static void spawnWorldParticles(MinecraftClient client, Module module, long now) {
      double spawnRate = Math.max(1.0, module.getSettingValue("Спавн/сек", 15.0));
      long interval = (long)Math.max(10.0, 1000.0 / spawnRate);
      int maxParticles = (int)Math.round(module.getSettingValue("Макс количество", 50.0));
      if (now - lastSpawnTime < interval || PARTICLES.size() >= maxParticles) {
         return;
      }

      lastSpawnTime = now;
      ThreadLocalRandom random = ThreadLocalRandom.current();
      double range = module.getSettingValue("Радиус спавна", 25.0);
      double height = module.getSettingValue("Высота спавна", 10.0);
      Vec3d playerPos = client.player.getPos();
      Vec3d pos = playerPos.add(random.nextDouble(-range, range), height + random.nextDouble(0.0, 3.0), random.nextDouble(-range, range));
      PARTICLES.add(new WorldParticles.Particle(pos, (long)Math.round(module.getSettingValue("Время жизни", 1800.0))));
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
      private final long lifeTime;
      private final float spin;
      private float alpha = 1.0F;

      private Particle(Vec3d pos, long lifeTime) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         this.pos = pos;
         this.velocity = new Vec3d(random.nextDouble(-0.01, 0.01), random.nextDouble(-0.035, -0.015), random.nextDouble(-0.01, 0.01));
         this.time = System.currentTimeMillis();
         this.lifeTime = Math.max(500L, lifeTime);
         this.spin = random.nextFloat() * 6.28F;
      }

      private void update(Module module) {
         this.pos = this.pos.add(this.velocity);
         double gravity = module.getSettingValue("Гравитация", 1.0) * 0.0008;
         this.velocity = this.velocity.add(0.0, -gravity, 0.0).multiply(0.996, 0.995, 0.996);
         this.alpha = this.fade();
      }

      private float fade() {
         float progress = Math.min(1.0F, (float)(System.currentTimeMillis() - this.time) / (float)this.lifeTime);
         if (progress < 0.2F) {
            return progress / 0.2F;
         } else {
            return Math.max(0.0F, 1.0F - (progress - 0.2F) / 0.8F);
         }
      }
   }
}

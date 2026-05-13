package net.monolith.optimization;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.ChunkBuilderMode;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;

public final class OptimizationManager {
   private static boolean applied;
   private static GraphicsMode graphicsMode;
   private static CloudRenderMode cloudMode;
   private static ParticlesMode particlesMode;
   private static ChunkBuilderMode chunkBuilderMode;
   private static boolean entityShadows;
   private static boolean ambientOcclusion;
   private static int viewDistance;
   private static int simulationDistance;
   private static double entityDistance;
   private static int biomeBlend;
   private static int mipmapLevels;

   private OptimizationManager() {
   }

   public static void tick(MinecraftClient client) {
      if (client != null) {
         Module module = ModuleManager.getModule("Optimization");
         boolean enabled = module != null && module.enabled;
         if (enabled) {
            apply(client);
         } else {
            restore(client);
         }
      }
   }

   private static void apply(MinecraftClient client) {
      if (!applied) {
         GameOptions options = client.options;
         graphicsMode = (GraphicsMode)options.getGraphicsMode().getValue();
         cloudMode = (CloudRenderMode)options.getCloudRenderMode().getValue();
         particlesMode = (ParticlesMode)options.getParticles().getValue();
         chunkBuilderMode = (ChunkBuilderMode)options.getChunkBuilderMode().getValue();
         entityShadows = (Boolean)options.getEntityShadows().getValue();
         ambientOcclusion = (Boolean)options.getAo().getValue();
         viewDistance = (Integer)options.getViewDistance().getValue();
         simulationDistance = (Integer)options.getSimulationDistance().getValue();
         entityDistance = (Double)options.getEntityDistanceScaling().getValue();
         biomeBlend = (Integer)options.getBiomeBlendRadius().getValue();
         mipmapLevels = (Integer)options.getMipmapLevels().getValue();
         options.getGraphicsMode().setValue(GraphicsMode.FAST);
         options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
         options.getParticles().setValue(ParticlesMode.MINIMAL);
         options.getChunkBuilderMode().setValue(ChunkBuilderMode.NEARBY);
         options.getEntityShadows().setValue(false);
         options.getAo().setValue(false);
         options.getViewDistance().setValue(Math.min(viewDistance, 8));
         options.getSimulationDistance().setValue(Math.min(simulationDistance, 5));
         options.getEntityDistanceScaling().setValue(Math.min(entityDistance, 0.75));
         options.getBiomeBlendRadius().setValue(0);
         options.getMipmapLevels().setValue(0);
         client.chunkCullingEnabled = true;
         applied = true;
      }
   }

   private static void restore(MinecraftClient client) {
      if (applied) {
         GameOptions options = client.options;
         options.getGraphicsMode().setValue(graphicsMode);
         options.getCloudRenderMode().setValue(cloudMode);
         options.getParticles().setValue(particlesMode);
         options.getChunkBuilderMode().setValue(chunkBuilderMode);
         options.getEntityShadows().setValue(entityShadows);
         options.getAo().setValue(ambientOcclusion);
         options.getViewDistance().setValue(viewDistance);
         options.getSimulationDistance().setValue(simulationDistance);
         options.getEntityDistanceScaling().setValue(entityDistance);
         options.getBiomeBlendRadius().setValue(biomeBlend);
         options.getMipmapLevels().setValue(mipmapLevels);
         applied = false;
      }
   }
}

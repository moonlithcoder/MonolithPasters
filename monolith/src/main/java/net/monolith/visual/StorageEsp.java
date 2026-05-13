package net.monolith.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.client.MinecraftClient;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.render.Render3D;

public final class StorageEsp {
   private StorageEsp() {
   }

   public static void render(WorldRenderContext context) {
      MinecraftClient client = MinecraftClient.getInstance();
      Module module = ModuleManager.getModule("StorageESP");
      if (module != null && module.enabled && client.player != null && client.world != null) {
         Render3D renderer = Render3D.of(context);
         int range = (int)Math.round(module.getSettingValue("Range", 48.0));
         int chunkRange = Math.max(1, range / 16 + 1);
         int playerChunkX = client.player.getBlockPos().getX() >> 4;
         int playerChunkZ = client.player.getBlockPos().getZ() >> 4;

         for (int cx = playerChunkX - chunkRange; cx <= playerChunkX + chunkRange; cx++) {
            for (int cz = playerChunkZ - chunkRange; cz <= playerChunkZ + chunkRange; cz++) {
               if (client.world.isChunkLoaded(cx, cz)) {
                  WorldChunk chunk = client.world.getChunk(cx, cz);

                  for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                     if (isStorage(blockEntity)) {
                        BlockPos pos = blockEntity.getPos();
                        if (!(client.player.getBlockPos().getSquaredDistance(pos) > (double)(range * range))) {
                           Box box = new Box(pos).expand(0.01);
                           int color = color(blockEntity);
                           renderer.filledBox(box, withAlpha(color, 45));
                           renderer.box(box, withAlpha(color, 220));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isStorage(BlockEntity blockEntity) {
      BlockEntityType<?> type = blockEntity.getType();
      return type == BlockEntityType.CHEST
         || type == BlockEntityType.TRAPPED_CHEST
         || type == BlockEntityType.ENDER_CHEST
         || type == BlockEntityType.BARREL
         || type == BlockEntityType.SHULKER_BOX
         || type == BlockEntityType.HOPPER
         || type == BlockEntityType.DISPENSER
         || type == BlockEntityType.DROPPER
         || type == BlockEntityType.FURNACE
         || type == BlockEntityType.BLAST_FURNACE;
   }

   private static int color(BlockEntity blockEntity) {
      BlockEntityType<?> type = blockEntity.getType();
      if (type == BlockEntityType.ENDER_CHEST) {
         return 10181887;
      } else if (type == BlockEntityType.SHULKER_BOX) {
         return 16732120;
      } else {
         return type == BlockEntityType.BARREL ? 16765532 : 58879;
      }
   }

   private static int withAlpha(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }
}

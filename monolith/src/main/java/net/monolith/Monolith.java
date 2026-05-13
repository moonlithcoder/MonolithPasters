package net.monolith;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.AllowChat;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AllowMouseClick;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AllowMouseRelease;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil.Type;
import net.monolith.combat.AttackAura;
import net.monolith.combat.RotationManager;
import net.monolith.config.ConfigManager;
import net.monolith.hud.HudManager;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;
import net.monolith.optimization.OptimizationManager;
import net.monolith.ui.CsGuiScreen;
import net.monolith.visual.Arrows;
import net.monolith.visual.Esp;
import net.monolith.visual.Nametags;
import net.monolith.visual.Prediction;
import net.monolith.visual.StorageEsp;
import net.monolith.visual.TargetEsp;
import net.monolith.visual.TargetGlow;
import net.monolith.visual.Trails;
import net.monolith.visual.WorldRender;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monolith implements ClientModInitializer {
   public static final String MOD_ID = "monolith";
   public static final Logger LOGGER = LoggerFactory.getLogger("monolith");
   private static KeyBinding guiKeyBinding;

   public void onInitializeClient() {
      ModuleManager.init();
      HudManager.init();
      ConfigManager.init(MinecraftClient.getInstance());
      guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.monolith.opengui", Type.KEYSYM, 344, "category.monolith.main"));
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         OptimizationManager.tick(client);
         WorldRender.tick(client);
         TargetEsp.tick();
         TargetGlow.tick();
         AttackAura.tick(client);
         RotationManager.tick();
         ConfigManager.autoSaveTick();
         this.handleHudKeybind(client);
         this.handleModuleKeybinds(client);

         while (guiKeyBinding.wasPressed()) {
            if (client.currentScreen == null) {
               client.setScreen(new CsGuiScreen());
            }
         }
      });
      HudRenderCallback.EVENT.register((HudRenderCallback)(context, tickDelta) -> {
         HudManager.render(context, tickDelta.getTickDelta(true));
         Arrows.render(context, tickDelta.getTickDelta(true));
      });
      WorldRenderEvents.LAST.register((Last)context -> {
         Esp.render(context);
         TargetEsp.render(context);
         TargetGlow.render(context);
         Trails.render(context);
         Prediction.render(context);
         StorageEsp.render(context);
         Nametags.render(context);
      });
      ClientSendMessageEvents.ALLOW_CHAT.register((AllowChat)message -> !ConfigManager.handleChat(message));
      ScreenEvents.AFTER_INIT
         .register(
            (AfterInit)(client, screen, scaledWidth, scaledHeight) -> {
               if (screen instanceof ChatScreen) {
                  ScreenMouseEvents.allowMouseClick(screen)
                     .register((AllowMouseClick)(screen1, mouseX, mouseY, button) -> !HudManager.mouseClicked(mouseX, mouseY, button));
                  ScreenMouseEvents.allowMouseRelease(screen).register((AllowMouseRelease)(screen1, mouseX, mouseY, button) -> {
                     HudManager.mouseReleased(button);
                     return true;
                  });
               }
            }
         );
   }

   private void handleModuleKeybinds(MinecraftClient client) {
      if (client.currentScreen == null && client.getWindow() != null) {
         long handle = client.getWindow().getHandle();

         for (Module module : ModuleManager.modules) {
            if (!module.name.equals("HUD") || module.keyCode != module.hudKeyCode) {
               if (module.keyCode >= 0 && GLFW.glfwGetKey(handle, module.keyCode) == 1) {
                  module.toggle();
                  module.keyCode = -module.keyCode - 2;
               } else if (module.keyCode < -1) {
                  int original = -module.keyCode - 2;
                  if (GLFW.glfwGetKey(handle, original) == 0) {
                     module.keyCode = original;
                  }
               }
            }
         }
      }
   }

   private void handleHudKeybind(MinecraftClient client) {
      Module hud = ModuleManager.getModule("HUD");
      if (hud != null && client.currentScreen == null && client.getWindow() != null) {
         long handle = client.getWindow().getHandle();
         if (GLFW.glfwGetKey(handle, hud.hudKeyCode) == 1) {
            HudManager.visible = !HudManager.visible;
            hud.hudKeyCode = -hud.hudKeyCode - 2;
         } else if (hud.hudKeyCode < -1) {
            int original = -hud.hudKeyCode - 2;
            if (GLFW.glfwGetKey(handle, original) == 0) {
               hud.hudKeyCode = original;
            }
         }
      }
   }
}

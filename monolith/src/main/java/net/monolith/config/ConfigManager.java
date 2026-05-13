package net.monolith.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.monolith.friend.FriendManager;
import net.monolith.hud.HudManager;
import net.monolith.module.Module;
import net.monolith.module.ModuleManager;

public final class ConfigManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static Path configDir;
   private static int autoSaveTicks;

   private ConfigManager() {
   }

   public static void init(MinecraftClient client) {
      configDir = client.runDirectory.toPath().resolve("monolith").resolve("configs");

      try {
         Files.createDirectories(configDir);
         if (list().isEmpty() || !Files.exists(path("default"))) {
            save("default");
         }

         load("default");
      } catch (IOException var2) {
      }
   }

   public static void save(String name) throws IOException {
      Files.createDirectories(dir());

      try (Writer writer = Files.newBufferedWriter(path(name))) {
         GSON.toJson(createJson(), writer);
      }
   }

   public static void autoSaveTick() {
      if (++autoSaveTicks >= 200) {
         autoSaveTicks = 0;

         try {
            save("default");
         } catch (IOException var1) {
         }
      }
   }

   public static void load(String name) throws IOException {
      Path path = path(name);
      if (!Files.exists(path)) {
         throw new IOException("Config not found");
      } else {
         try (Reader reader = Files.newBufferedReader(path)) {
            applyJson((JsonObject)GSON.fromJson(reader, JsonObject.class));
         }
      }
   }

   public static List<String> list() throws IOException {
      Files.createDirectories(dir());
      List<String> result = new ArrayList<>();

      try (Stream<Path> stream = Files.list(dir())) {
         stream.filter(path -> path.getFileName().toString().endsWith(".json"))
            .map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
            .sorted()
            .forEach(result::add);
      }

      return result;
   }

   public static List<String> safeList() {
      try {
         return list();
      } catch (IOException var1) {
         return new ArrayList<>();
      }
   }

   public static boolean safeLoad(String name) {
      try {
         load(name);
         return true;
      } catch (IOException var2) {
         return false;
      }
   }

   public static boolean handleChat(String message) {
      if (message.startsWith(".friend")) {
         return handleFriend(message);
      } else if (!message.startsWith(".config") && !message.startsWith(".cfg")) {
         return false;
      } else {
         String[] args = message.trim().split("\\s+");

         try {
            if (args.length >= 3 && args[1].equalsIgnoreCase("save")) {
               save(args[2]);
               send("Saved config: " + args[2]);
            } else if (args.length >= 3 && args[1].equalsIgnoreCase("load")) {
               load(args[2]);
               send("Loaded config: " + args[2]);
            } else if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
               List<String> configs = list();
               send(configs.isEmpty() ? "Configs: none" : "Configs: " + String.join(", ", configs));
            } else {
               send("Usage: .config/.cfg save <name> | load <name> | list");
            }
         } catch (IOException var3) {
            send("Config error: " + var3.getMessage());
         }

         return true;
      }
   }

   private static boolean handleFriend(String message) {
      String[] args = message.trim().split("\\s+");
      if (args.length >= 3 && args[1].equalsIgnoreCase("add")) {
         FriendManager.add(args[2]);
         send("Friend added: " + args[2]);
      } else if (args.length >= 3 && args[1].equalsIgnoreCase("remove")) {
         FriendManager.remove(args[2]);
         send("Friend removed: " + args[2]);
      } else if (args.length >= 2 && args[1].equalsIgnoreCase("clear")) {
         FriendManager.clear();
         send("Friends cleared");
      } else if (args.length >= 2 && args[1].equalsIgnoreCase("list")) {
         send(FriendManager.all().isEmpty() ? "Friends: none" : "Friends: " + String.join(", ", FriendManager.all()));
      } else {
         send("Usage: .friend add <nick> | remove <nick> | list | clear");
      }

      try {
         save("default");
      } catch (IOException var3) {
      }

      return true;
   }

   public static String suggestion(String text) {
      if (text == null || !text.startsWith(".")) {
         return null;
      } else if (".config".startsWith(text)) {
         return ".config save <name> | load <name> | list";
      } else if (".cfg".startsWith(text)) {
         return ".cfg save <name> | load <name> | list";
      } else if (".friend".startsWith(text)) {
         return ".friend add <nick> | remove <nick> | list | clear";
      } else if (text.startsWith(".config") || text.startsWith(".cfg")) {
         return "save <name> | load <name> | list";
      } else {
         return text.startsWith(".friend") ? "add <nick> | remove <nick> | list | clear" : ".config  .cfg  .friend";
      }
   }

   private static JsonObject createJson() {
      JsonObject root = new JsonObject();
      JsonObject hud = new JsonObject();
      hud.addProperty("visible", HudManager.visible);
      root.add("hud", hud);
      JsonArray friends = new JsonArray();

      for (String friend : FriendManager.all()) {
         friends.add(friend);
      }

      root.add("friends", friends);
      JsonArray modules = new JsonArray();

      for (Module module : ModuleManager.modules) {
         JsonObject object = new JsonObject();
         object.addProperty("name", module.name);
         object.addProperty("enabled", module.enabled);
         object.addProperty("mode", module.currentMode);
         object.addProperty("keyCode", normalizeKey(module.keyCode));
         object.addProperty("hudKeyCode", normalizeKey(module.hudKeyCode));
         JsonObject settings = new JsonObject();

         for (Entry<String, Module.Setting> entry : module.settings.entrySet()) {
            settings.addProperty(entry.getKey(), entry.getValue().value);
         }

         object.add("settings", settings);
         JsonObject options = new JsonObject();

         for (Module.OptionSetting setting : module.optionSettings) {
            JsonArray selected = new JsonArray();

            for (String value : setting.selected) {
               selected.add(value);
            }

            options.add(setting.name, selected);
         }

         object.add("options", options);
         modules.add(object);
      }

      root.add("modules", modules);
      return root;
   }

   private static void applyJson(JsonObject root) {
      if (root != null) {
         JsonObject hud = root.getAsJsonObject("hud");
         if (hud != null && hud.has("visible")) {
            HudManager.visible = hud.get("visible").getAsBoolean();
         }

         JsonArray friends = root.getAsJsonArray("friends");
         FriendManager.clear();
         if (friends != null) {
            for (JsonElement friend : friends) {
               FriendManager.add(friend.getAsString());
            }
         }

         JsonArray modules = root.getAsJsonArray("modules");
         if (modules != null) {
            for (JsonElement element : modules) {
               JsonObject object = element.getAsJsonObject();
               Module module = ModuleManager.getModule(object.get("name").getAsString());
               if (module != null) {
                  if (object.has("enabled")) {
                     module.enabled = object.get("enabled").getAsBoolean();
                  }

                  if (object.has("mode")) {
                     module.currentMode = object.get("mode").getAsString();
                  }

                  if (object.has("keyCode")) {
                     module.keyCode = object.get("keyCode").getAsInt();
                  }

                  if (object.has("hudKeyCode")) {
                     module.hudKeyCode = object.get("hudKeyCode").getAsInt();
                  }

                  JsonObject settings = object.getAsJsonObject("settings");
                  if (settings != null) {
                     for (Entry<String, JsonElement> entry : settings.entrySet()) {
                        Module.Setting setting = module.getSetting(entry.getKey());
                        if (setting != null) {
                           setting.set(entry.getValue().getAsDouble());
                        }
                     }
                  }

                  JsonObject options = object.getAsJsonObject("options");
                  if (options != null) {
                     for (Entry<String, JsonElement> entryx : options.entrySet()) {
                        Module.OptionSetting setting = module.getOptionSetting(entryx.getKey());
                        if (setting != null) {
                           setting.selected.clear();

                           for (JsonElement selected : entryx.getValue().getAsJsonArray()) {
                              String value = selected.getAsString();
                              if (setting.options.contains(value)) {
                                 setting.selected.add(value);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static Path dir() {
      if (configDir == null) {
         configDir = MinecraftClient.getInstance().runDirectory.toPath().resolve("monolith").resolve("configs");
      }

      return configDir;
   }

   private static Path path(String name) {
      return dir().resolve(sanitize(name) + ".json");
   }

   private static String sanitize(String name) {
      return name.replaceAll("[^A-Za-z0-9_\\-]", "_");
   }

   private static int normalizeKey(int key) {
      return key < -1 ? -key - 2 : key;
   }

   private static void send(String text) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null) {
         client.player.sendMessage(Text.literal("[Monolith] " + text), false);
      }
   }
}

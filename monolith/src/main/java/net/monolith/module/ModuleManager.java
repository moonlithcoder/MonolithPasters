package net.monolith.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleManager {
   public static final List<Module> modules = new ArrayList<>();

   public static void init() {
      Module attackAura = new Module("AttackAura", "Combat", "Автоматически атакует выбранные цели в радиусе.");
      attackAura.addSetting("Range", 3.2, 2.0, 6.0, 0.1, "Дистанция, с которой модуль наносит удар.");
      attackAura.addSetting("Vision", 4.2, 1.5, 6.0, 0.1, "Радиус поиска целей вокруг игрока.");
      attackAura.addOptionSettingDescription("Rotations", false, "SpookyTime duels", "Режим наведения на цель.", "SpookyTime duels", "Polar", "Vulcan", "Grim", "Funtime");
      attackAura.addOptionSettingDescription("Targets", true, "Players", "Типы сущностей, которых можно атаковать.", "Players", "Friends", "Mobs", "Animals");
      attackAura.addOptionSettingDescription("Move correction", false, "Free", "Коррекция движения во время наведения.", "Free", "Targeted");
      attackAura.addOptionSettingDescription("Extra", true, "Don't attack while eating", "Дополнительные ограничения атаки.", "Don't attack while eating", "Don't attack through walls", "Only with weapon");
      attackAura.addOptionSettingDescription("Bypass features", true, "Randomize aim", "Дополнительное поведение для обхода проверок.", "Randomize aim", "Smart cooldown", "Sprint reset", "Target strafe");
      modules.add(attackAura);
      modules.add(new Module("Velocity", "Combat", "Уменьшает отбрасывание от ударов."));
      Module hudModule = new Module("HUD", "Visuals", "Показывает на экране элементы клиента.");
      hudModule.enabled = true;
      modules.add(hudModule);
      Module worldRender = new Module("World render", "Visuals", "Меняет отображение мира и неба.");
      worldRender.addOptionSettingDescription("Custom world", false, "Off", "Включает пользовательский цвет мира.", "Off", "On");
      worldRender.addColorSetting("Sky Color", 6728447, "Цвет неба при включенном Custom world.");
      modules.add(worldRender);
      Module fullbright = new Module("Fullbright", "Visuals", "Делает мир ярким без темных зон.");
      modules.add(fullbright);
      Module prediction = new Module("Prediction", "Visuals", "Рисует траектории полета предметов.");
      Module.OptionSetting predictionShow = prediction.addOptionSettingDescription("Show", true, "Pearl", "Какие траектории показывать.", "Pearl", "Bow", "Crossbow", "Trident");
      predictionShow.selected.addAll(Arrays.asList("Bow", "Crossbow", "Trident"));
      modules.add(prediction);
      Module removals = new Module("Removals", "Visuals", "Скрывает мешающие визуальные эффекты.");
      removals.addOptionSettingDescription("Remove", true, "Fire", "Какие эффекты убрать с экрана.", "Fire", "Shake");
      modules.add(removals);
      Module targetEsp = new Module("TargetESP", "Visuals", "Подсвечивает текущую цель AttackAura.");
      targetEsp.modes = Arrays.asList("Квадрат", "Призраки");
      targetEsp.currentMode = "Призраки";
      modules.add(targetEsp);
      Module esp = new Module("ESP", "Visuals", "Показывает игроков и мобов через окружение.");
      esp.addSetting("Opacity", 45.0, 10.0, 100.0, 5.0, "Прозрачность заливки ESP.");
      esp.addOptionSettingDescription("Targets", true, "Players", "Кого подсвечивать через стены.", "Players", "Mobs", "Animals");
      modules.add(esp);
      Module nametags = new Module("Nametags", "Visuals", "Показывает ник, роль и здоровье над игроками.");
      nametags.addOptionSettingDescription("Show", true, "Armor", "Дополнительная информация в nametag.", "Armor", "Left item", "Right item");
      modules.add(nametags);
      Module trails = new Module("Trails", "Visuals", "Оставляет след за движущимися игроками.");
      trails.addSetting("Length", 36.0, 10.0, 90.0, 2.0, "Длина визуального следа.");
      trails.addSetting("Height", 0.9, 0.55, 1.15, 0.02, "Высота следа относительно игрока.");
      trails.addSetting("Top Gap", 0.04, 0.0, 0.25, 0.01, "Отступ верхней части следа.");
      trails.addOptionSettingDescription("View", false, "Third person", "Когда отображать свой след.", "Third person", "Always");
      modules.add(trails);
      Module arrows = new Module("Arrows", "Visuals", "Стрелками показывает игроков вне центра экрана.");
      arrows.addSetting("Range", 80.0, 20.0, 180.0, 5.0, "Дальность поиска игроков для стрелок.");
      arrows.addSetting("Distance", 62.0, 35.0, 110.0, 1.0, "Расстояние стрелок от центра экрана.");
      arrows.addSetting("Size", 18.0, 10.0, 32.0, 1.0, "Размер стрелок.");
      arrows.addOptionSettingDescription("Design", false, "Client", "Стиль окраски стрелок.", "Client", "Celestial", "Nursultan");
      modules.add(arrows);
      Module particles = new Module("World particles", "Visuals", "Создает частицы вокруг цели после удара.");
      particles.addOptionSettingDescription("Вид", false, "Сердечки", "Форма частиц после удара.", "Сердечки", "Орбизы", "Молния", "Снежинки");
      particles.addSetting("Кол-во за удар", 20.0, 1.0, 50.0, 1.0, "Сколько частиц создавать после каждого удара.");
      modules.add(particles);
      Module storageEsp = new Module("StorageESP", "Visuals", "Подсвечивает сундуки и другие контейнеры.");
      storageEsp.addSetting("Range", 48.0, 12.0, 128.0, 4.0, "Дистанция поиска контейнеров.");
      modules.add(storageEsp);
      modules.add(new Module("Sprint", "Movement", "Автоматически включает бег."));
      modules.add(new Module("NoSlow", "Movement", "Убирает замедление при использовании предметов."));
      modules.add(new Module("Optimization", "Misc", "Отключает лишние эффекты для повышения FPS."));
   }

   public static List<Module> getModulesByCategory(String category) {
      List<Module> result = new ArrayList<>();

      for (Module m : modules) {
         if (m.category.equals(category)) {
            result.add(m);
         }
      }

      return result;
   }

   public static Module getModule(String name) {
      for (Module m : modules) {
         if (m.name.equalsIgnoreCase(name)) {
            return m;
         }
      }

      return null;
   }
}

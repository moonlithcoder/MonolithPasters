package net.monolith.friend;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class FriendManager {
   private static final Set<String> FRIENDS = new LinkedHashSet<>();

   private FriendManager() {
   }

   public static void add(String name) {
      if (name != null && !name.isBlank()) {
         FRIENDS.add(name.toLowerCase(Locale.ROOT));
      }
   }

   public static void remove(String name) {
      if (name != null) {
         FRIENDS.remove(name.toLowerCase(Locale.ROOT));
      }
   }

   public static void clear() {
      FRIENDS.clear();
   }

   public static boolean isFriend(String name) {
      return name != null && FRIENDS.contains(name.toLowerCase(Locale.ROOT));
   }

   public static Set<String> all() {
      return FRIENDS;
   }
}

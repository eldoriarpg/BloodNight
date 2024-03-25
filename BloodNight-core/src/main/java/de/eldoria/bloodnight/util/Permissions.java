package de.eldoria.bloodnight.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Permissions {
    public static final String BASE = "bloodnight";

    @UtilityClass
    public static class Admin {
        public static final String ADMIN = BASE + ".admin";
        public static final String SPAWN_MOB = ADMIN + ".spawnmob";
        public static final String RELOAD = ADMIN + ".reload";
        public static final String MANAGE_DEATH_ACTION = ADMIN + ".managedeathactions";
        public static final String MANAGE_WORLDS = ADMIN + ".manageworlds";
        public static final String MANAGE_NIGHT = ADMIN + ".managenight";
        public static final String MANAGE_MOBS = ADMIN + ".managemobs";
        public static final String MANAGE_MOB = ADMIN + ".managemob";
        public static final String FORCE_NIGHT = ADMIN + ".forcenight";
        public static final String CANCEL_NIGHT = ADMIN + ".cancelnight";
    }

    @UtilityClass
    public static class Bypass {
        public static final String BYPASS = BASE + ".bypass";
        public static final String COMMAND_BLOCK = BYPASS + ".blockedcommands";
    }
}

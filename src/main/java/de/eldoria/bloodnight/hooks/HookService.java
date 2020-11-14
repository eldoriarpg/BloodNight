package de.eldoria.bloodnight.hooks;

import de.eldoria.bloodnight.hooks.mythicmobs.MythicMobsHook;
import lombok.Getter;

@Getter
public class HookService {
    private final MythicMobsHook mythicMobsHook;

    public HookService() {
        mythicMobsHook = new MythicMobsHook();
        mythicMobsHook.setup();
    }
}
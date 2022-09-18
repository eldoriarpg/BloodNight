package de.eldoria.bloodnight.hooks.mythicmobs;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.hooks.AbstractHookService;
import io.lumine.mythic.bukkit.MythicBukkit;

public class MythicMobsHook extends AbstractHookService<MythicBukkit> {
    public MythicMobsHook() {
        super("MythicMobs");
    }

    @Override
    public MythicBukkit getHook() throws ClassNotFoundException {
        return MythicBukkit.inst();
    }

    @Override
    public void setup() {
        BloodNight.getInstance().registerListener(new MythicMobTagger());
    }

    @Override
    public void shutdown() {

    }
}

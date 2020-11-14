package de.eldoria.bloodnight.hooks.mythicmobs;

import de.eldoria.bloodnight.core.BloodNight;
import de.eldoria.bloodnight.hooks.AbstractHookService;
import io.lumine.xikage.mythicmobs.MythicMobs;

public class MythicMobsHook extends AbstractHookService<MythicMobs> {
    public MythicMobsHook() {
        super("MythicMobs");
    }

    @Override
    public MythicMobs getHook() throws ClassNotFoundException {
        return MythicMobs.inst();
    }

    @Override
    public void setup() {
        BloodNight.getInstance().registerListener();
    }

    @Override
    public void shutdown() {

    }
}

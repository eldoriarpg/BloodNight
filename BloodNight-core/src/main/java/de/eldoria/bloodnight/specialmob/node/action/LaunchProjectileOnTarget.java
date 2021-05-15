package de.eldoria.bloodnight.specialmob.node.action;

import de.eldoria.bloodnight.specialmob.ISpecialMob;
import de.eldoria.bloodnight.specialmob.node.Node;
import de.eldoria.bloodnight.specialmob.node.context.IActionContext;
import de.eldoria.bloodnight.specialmobs.SpecialMobUtil;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LaunchProjectileOnTarget implements Node {
    ProjectileType projectileType;
    double speed;

    @Override
    public void handle(ISpecialMob mob, IActionContext context) {
        SpecialMobUtil.launchProjectileOnTarget(mob.getBase(), projectileType.projectileClazz(), speed);
    }

    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    public enum ProjectileType {
        ARROW(Arrow.class),
        DRAGON_FIREBALL(DragonFireball.class),
        EGG(Egg.class),
        ENDER_PEARL(EnderPearl.class),
        FIREBALL(Fireball.class),
        FISH_HOOK(FishHook.class),
        LARGE_FIREBALL(LargeFireball.class),
        LLAMA_SPIT(LlamaSpit.class),
        SHULKER_BULLET(ShulkerBullet.class),
        SIZED_FIREBALL(SizedFireball.class),
        SMALL_FIREBALL(SmallFireball.class),
        SNOWBALL(Snowball.class),
        WITHER_SKULL(WitherSkull.class);

        private final Class<? extends Projectile> projectileClazz;

        ProjectileType(Class<? extends Projectile> projectileClazz) {
            this.projectileClazz = projectileClazz;
        }

        public Class<? extends Projectile> projectileClazz() {
            return projectileClazz;
        }
    }
}

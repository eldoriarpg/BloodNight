package de.eldoria.bloodnight.bloodmob.nodeimpl.action;

import de.eldoria.bloodnight.bloodmob.IBloodMob;
import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.bloodmob.node.annotations.RequiresContext;
import de.eldoria.bloodnight.bloodmob.node.contextcontainer.ContextContainer;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumericProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.bloodnight.bloodmob.utils.BloodMobUtil;
import lombok.NoArgsConstructor;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@NoArgsConstructor
public class LaunchProjectileOnTarget implements Node {

    @Property(name = "", descr = "")
    ProjectileType projectileType;
    @NumericProperty(name = "", descr = "", max = 64)
    float speed;

    public LaunchProjectileOnTarget(ProjectileType projectileType, float speed) {
        this.projectileType = projectileType;
        this.speed = speed;
    }

    @Override
    public void handle(IBloodMob mob, ContextContainer context) {
        BloodMobUtil.launchProjectileOnTarget(mob.getBase(), projectileType.projectileClazz(), speed);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LaunchProjectileOnTarget that = (LaunchProjectileOnTarget) o;

        if (Float.compare(that.speed, speed) != 0) return false;
        return projectileType == that.projectileType;
    }

    @Override
    public int hashCode() {
        int result = projectileType.hashCode();
        result = 31 * result + (speed != +0.0f ? Float.floatToIntBits(speed) : 0);
        return result;
    }
}

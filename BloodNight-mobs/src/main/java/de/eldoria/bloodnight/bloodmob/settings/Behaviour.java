package de.eldoria.bloodnight.bloodmob.settings;

import de.eldoria.bloodnight.bloodmob.node.Node;
import de.eldoria.bloodnight.config.ConfigCheck;
import de.eldoria.bloodnight.config.ConfigException;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Behaviour implements ConfigCheck<MobConfiguration>, ConfigurationSerializable {
    private List<Node> tick = new ArrayList<>();
    private List<Node> onEnd = new ArrayList<>();
    private List<Node> onTeleport = new ArrayList<>();
    private List<Node> onProjectileShoot = new ArrayList<>();
    private List<Node> onProjectileHit = new ArrayList<>();
    private List<Node> onDeath = new ArrayList<>();
    private List<Node> onKill = new ArrayList<>();
    private List<Node> onExplosionPrime = new ArrayList<>();
    private List<Node> onExplosion = new ArrayList<>();
    private List<Node> onTarget = new ArrayList<>();
    private List<Node> onDamage = new ArrayList<>();
    private List<Node> onDamageByEntity = new ArrayList<>();
    private List<Node> onHit = new ArrayList<>();

    public Behaviour() {
    }

    public void addNode(BehaviourNode type, Node node) {
        getBehaviour(type).add(node);
    }

    private List<Node> getBehaviour(BehaviourNode type) {
        switch (type) {
            case TICK:
                return tick;
            case ON_END:
                return onEnd;
            case ON_TELEPORT:
                return onTeleport;
            case ON_PROJECTILE_SHOOT:
                return onProjectileShoot;
            case ON_PROJECTILE_HIT:
                return onProjectileHit;
            case ON_DEATH:
                return onDeath;
            case ON_KILL:
                return onKill;
            case ON_EXPLOSION_PRIME:
                return onExplosionPrime;
            case ON_EXPLOSION:
                return onExplosion;
            case ON_TARGET:
                return onTarget;
            case ON_DAMAGE:
                return onDamage;
            case ON_DAMAGE_BY_ENTITY:
                return onDamageByEntity;
            case ON_HIT:
                return onHit;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }


    @Override
    public void check(MobConfiguration data) throws ConfigException {
    }

    public Behaviour(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        tick = map.getValueOrDefault("tick", tick);
        onEnd = map.getValueOrDefault("onEnd", onEnd);
        onTeleport = map.getValueOrDefault("onTeleport", onTeleport);
        onProjectileShoot = map.getValueOrDefault("onProjectileShoot", onProjectileShoot);
        onProjectileHit = map.getValueOrDefault("onProjectileHit", onProjectileHit);
        onDeath = map.getValueOrDefault("onDeath", onDeath);
        onKill = map.getValueOrDefault("onKill", onKill);
        onExplosionPrime = map.getValueOrDefault("onExplosionPrime", onExplosionPrime);
        onExplosion = map.getValueOrDefault("onExplosion", onExplosion);
        onTarget = map.getValueOrDefault("onTarget", onTarget);
        onDamage = map.getValueOrDefault("onDamage", onDamage);
        onDamageByEntity = map.getValueOrDefault("onDamageByEntity", onDamageByEntity);
        onHit = map.getValueOrDefault("onHit", onHit);
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("tick", tick)
                .add("onEnd", onEnd)
                .add("onTeleport", onTeleport)
                .add("onProjectileShoot", onProjectileShoot)
                .add("onProjectileHit", onProjectileHit)
                .add("onDeath", onDeath)
                .add("onKill", onKill)
                .add("onExplosionPrime", onExplosionPrime)
                .add("onExplosion", onExplosion)
                .add("onTarget", onTarget)
                .add("onDamage", onDamage)
                .add("onDamageByEntity", onDamageByEntity)
                .add("onHit", onHit)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Behaviour behaviour = (Behaviour) o;

        if (tick != null ? !tick.equals(behaviour.tick) : behaviour.tick != null) return false;
        if (onEnd != null ? !onEnd.equals(behaviour.onEnd) : behaviour.onEnd != null) return false;
        if (onTeleport != null ? !onTeleport.equals(behaviour.onTeleport) : behaviour.onTeleport != null) return false;
        if (onProjectileShoot != null ? !onProjectileShoot.equals(behaviour.onProjectileShoot) : behaviour.onProjectileShoot != null)
            return false;
        if (onProjectileHit != null ? !onProjectileHit.equals(behaviour.onProjectileHit) : behaviour.onProjectileHit != null)
            return false;
        if (onDeath != null ? !onDeath.equals(behaviour.onDeath) : behaviour.onDeath != null) return false;
        if (onKill != null ? !onKill.equals(behaviour.onKill) : behaviour.onKill != null) return false;
        if (onExplosionPrime != null ? !onExplosionPrime.equals(behaviour.onExplosionPrime) : behaviour.onExplosionPrime != null)
            return false;
        if (onExplosion != null ? !onExplosion.equals(behaviour.onExplosion) : behaviour.onExplosion != null)
            return false;
        if (onTarget != null ? !onTarget.equals(behaviour.onTarget) : behaviour.onTarget != null) return false;
        if (onDamage != null ? !onDamage.equals(behaviour.onDamage) : behaviour.onDamage != null) return false;
        if (onDamageByEntity != null ? !onDamageByEntity.equals(behaviour.onDamageByEntity) : behaviour.onDamageByEntity != null)
            return false;
        return onHit != null ? onHit.equals(behaviour.onHit) : behaviour.onHit == null;
    }

    @Override
    public int hashCode() {
        int result = tick != null ? tick.hashCode() : 0;
        result = 31 * result + (onEnd != null ? onEnd.hashCode() : 0);
        result = 31 * result + (onTeleport != null ? onTeleport.hashCode() : 0);
        result = 31 * result + (onProjectileShoot != null ? onProjectileShoot.hashCode() : 0);
        result = 31 * result + (onProjectileHit != null ? onProjectileHit.hashCode() : 0);
        result = 31 * result + (onDeath != null ? onDeath.hashCode() : 0);
        result = 31 * result + (onKill != null ? onKill.hashCode() : 0);
        result = 31 * result + (onExplosionPrime != null ? onExplosionPrime.hashCode() : 0);
        result = 31 * result + (onExplosion != null ? onExplosion.hashCode() : 0);
        result = 31 * result + (onTarget != null ? onTarget.hashCode() : 0);
        result = 31 * result + (onDamage != null ? onDamage.hashCode() : 0);
        result = 31 * result + (onDamageByEntity != null ? onDamageByEntity.hashCode() : 0);
        result = 31 * result + (onHit != null ? onHit.hashCode() : 0);
        return result;
    }
}

package de.eldoria.bloodnight.listener.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class ProjectileSender {
    private Entity entity;
    private Block block;

    /**
     * Create a new projectile send with a entity
     *
     * @param entity entity which is the sender
     */
    public ProjectileSender(Entity entity) {
        this.entity = entity;
    }

    /**
     * Create a new projectile send with a block
     *
     * @param block block which send the projectile
     */
    public ProjectileSender(Block block) {
        this.block = block;
    }

    /**
     * Empty projectile sender
     */
    public ProjectileSender() {
    }

    /**
     * Checks if a entity is present.
     *
     * @return true if the sender is a entity
     */
    public boolean isEntity() {
        return entity != null;
    }

    /**
     * Check if a block is present.
     *
     * @return true if the sender is a block
     */
    public boolean isBlock() {
        return block != null;
    }

    /**
     * Check if the sender is empty.
     *
     * @return false if {@link #isBlock()} and {@link #isEntity()} is false. otherwise true
     */
    public boolean isEmpty() {
        return block == null && entity == null;
    }

    /**
     * Get the sender as entity.
     *
     * @return Will be null if {@link #isEntity()} is false.
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Get the sender as block.
     *
     * @return Will be null if {@link #isBlock()} ()} is false.
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Get the sender as {@link EntityType}.
     *
     * @return entity type of sender
     * @throws NullPointerException When {@link #isEntity()} is false
     */
    public EntityType getEntityType() throws NullPointerException {
        return entity.getType();
    }

    /**
     * Get the sender as {@link Material}.
     *
     * @return material of sender
     * @throws NullPointerException When {@link #isEntity()} is false
     */
    public Material getBlockType() throws NullPointerException {
        return block.getType();
    }
}

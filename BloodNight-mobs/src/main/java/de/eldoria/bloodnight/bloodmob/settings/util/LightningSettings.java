package de.eldoria.bloodnight.bloodmob.settings.util;

import de.eldoria.bloodnight.config.ILightningSettings;
import de.eldoria.bloodnight.utils.InvMenuUtil;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Setter
@Getter
@SerializableAs("bloodNightLightningSettings")
public class LightningSettings implements ILightningSettings {
    /**
     * Activate Lighting.
     */
    protected boolean doLightning = true;

    /**
     * Probability of a lighting should be spawned above the death location.
     */
    protected int lightning = 100;

    /**
     * Activate thunder.
     */
    protected boolean doThunder = true;

    /**
     * If no lighting is send a thunder sound can be played optionally.
     */
    protected int thunder = 100;

    public LightningSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        doLightning = map.getValueOrDefault("doLightning", doLightning);
        lightning = map.getValueOrDefault("lightning", lightning);
        doThunder = map.getValueOrDefault("doThunder", doThunder);
        thunder = map.getValueOrDefault("thunder", thunder);
    }

    public LightningSettings() {
    }

    @Override
    public @NotNull
    Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("doLightning", doLightning)
                .add("lightning", lightning)
                .add("doThunder", doThunder)
                .add("thunder", thunder)
                .build();
    }

    public Inventory getInventoryRepresentation(Player inventoryHolder) {
        Inventory inventory = Bukkit.createInventory(inventoryHolder, 9);
        Material type;
        ItemStack lightingState = new ItemStack(InvMenuUtil.getBooleanMaterial(doLightning));
        return null;
    }
}

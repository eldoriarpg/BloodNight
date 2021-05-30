package de.eldoria.bloodnight.bloodmob.drop;

import de.eldoria.bloodnight.bloodmob.serialization.annotation.ItemProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.NumberProperty;
import de.eldoria.bloodnight.bloodmob.serialization.annotation.Property;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@SerializableAs("bloodNightDrop")
public class Drop implements ConfigurationSerializable, IDrop {
    @ItemProperty(name = "", descr = "")
    private int itemId;
    @NumberProperty(name = "", descr = "", max = 64)
    private int amount;
    @NumberProperty(name = "", descr = "", max = 100)
    private int weight;

    public Drop(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        itemId = map.getValue("itemId");
        weight = map.getValue("weight");
    }

    public Drop(int itemId, int weight) {
        this.itemId = itemId;
        this.weight = weight;
    }

    @Override
    public @NotNull
    Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("item", itemId)
                .add("weight", weight)
                .build();
    }


    public int itemId() {
        return itemId;
    }

    public int weight() {
        return weight;
    }
}

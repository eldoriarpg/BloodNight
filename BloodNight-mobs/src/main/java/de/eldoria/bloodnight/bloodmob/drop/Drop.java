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

@SerializableAs("bloodNightDrop")
public class Drop implements ConfigurationSerializable {
    @ItemProperty(name = "", descr = "")
    private int itemId;
    @NumberProperty(name = "", descr = "", max = 64)
    private int amount;
    @NumberProperty(name = "", descr = "", max = 100)
    private int weight;

    public Drop(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        itemId = map.getValue("itemId");
        amount = map.getValue("amount");
        weight = map.getValue("weight");
    }

    public Drop(int itemId, int amount, int weight) {
        this.itemId = itemId;
        this.amount = amount;
        this.weight = weight;
    }

    public Drop() {
    }

    @Override
    public @NotNull
    Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("item", itemId)
                .add("amount", amount)
                .add("weight", weight)
                .build();
    }

    public int amount() {
        return amount;
    }

    public int itemId() {
        return itemId;
    }

    public int weight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Drop drop = (Drop) o;

        if (itemId != drop.itemId) return false;
        if (amount != drop.amount) return false;
        return weight == drop.weight;
    }

    @Override
    public int hashCode() {
        int result = itemId;
        result = 31 * result + amount;
        result = 31 * result + weight;
        return result;
    }
}

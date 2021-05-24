package de.eldoria.bloodnight.bloodmob.serialization.value;

import java.util.List;

public class MapValueEntry {
    protected final ValueType type;

    public MapValueEntry(ValueType type) {
        this.type = type;
    }

    public static MapValueEntry withValues(List<String> values) {
        return new ListValue(values);
    }

    public static class ListValue extends MapValueEntry {
        private final List<String> values;

        public ListValue(List<String> values) {
            super(ValueType.LIST);
            this.values = values;
        }
    }
}

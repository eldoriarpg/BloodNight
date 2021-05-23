package de.eldoria.bloodnight.bloodmob.serialization.value;

public final class ValueProperties {
    public static class Floats {
        float min, max;

        private Floats(float min, float max) {
            this.min = min;
            this.max = max;
        }
    }

    public static class Ints {
        int min, max;

        private Ints(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }

    public static class Strings {
        private final String pattern;
        private final int min;
        private final int max;

        private Strings(String pattern, int min, int max) {
            this.pattern = pattern;
            this.min = min;
            this.max = max;
        }
    }

    public static Floats ofFloats(float min, float max) {
        return new Floats(min, max);
    }

    public static Ints ofInts(int min, int max) {
        return new Ints(min, max);
    }

    public static Strings ofString(String pattern, int min, int max) {
        return new Strings(pattern, min, max);
    }
}

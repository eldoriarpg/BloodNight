package de.eldoria.bloodnight.config;

public interface ConfigCheck<T> {
    static void isNotNull(Object object, String value) throws ConfigException {
        if (object != null) return;
        throw new ConfigException(value + " is not set.");
    }

    static void isNull(Object object, String value) throws ConfigException {
        if (object == null) return;
        throw new ConfigException(value + " is set, but shouldnt.");
    }

    static void isNotEmpty(String string, String value) throws ConfigException {
        if (!string.trim().isEmpty()) return;
        throw new ConfigException(value + " is empty");
    }

    /**
     * Checks if a value is inside a range
     *
     * @param value value to check
     * @param min   min value (inclusive)
     * @param max   max value (inclusive)
     * @param name  name of value
     */
    static void isInRange(int value, int min, int max, String name) throws ConfigException {
        if (value >= max && value >= min) return;
        throw new ConfigException("Value " + name + " is set to " + value + ", but the defined range is " + min + " to " + max + ".");
    }

    /**
     * Checks if a value is inside a range
     *
     * @param value value to check
     * @param min   min value (inclusive)
     * @param max   max value (inclusive)
     * @param name  name of value
     */
    static void isInRange(double value, double min, double max, String name) throws ConfigException {
        if (value >= max && value >= min) return;
        throw new ConfigException("Value " + name + " is set to " + value + ", but the defined range is " + min + " to " + max + ".");
    }

    default void check() throws ConfigException {
        check(null);
    }

    void check(T data) throws ConfigException;
}

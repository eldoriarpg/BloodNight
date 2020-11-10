package de.eldoria.bloodnight.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

public class MoonPhaseTest {
    @Test
    public void checkNewMoon() {
        Calendar instance = Calendar.getInstance();
        instance.set(2020, Calendar.NOVEMBER, 15);
        Assertions.assertEquals(0, MoonPhase.computePhaseIndex(instance));
    }

    @Test
    public void checkFullMoon() {
        Calendar instance = Calendar.getInstance();
        instance.set(2020, Calendar.NOVEMBER, 30);
        Assertions.assertEquals(4, MoonPhase.computePhaseIndex(instance));
    }
}

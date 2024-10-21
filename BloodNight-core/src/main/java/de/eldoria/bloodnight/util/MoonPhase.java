package de.eldoria.bloodnight.util;

import lombok.experimental.UtilityClass;

import java.util.Calendar;

// Proudly stolen from https://github.com/sighrobot/Moon/blob/master/src/com/abe/moon/MoonPhase.java
@UtilityClass
public class MoonPhase {

    /**
     * Computes the moon phase index as a value from 0 to 7 Used to display the phase name and the moon image for the
     * current phase.
     * <p>
     * 0 is a New Moon. 4 is a Full Moon
     *
     * @param cal Calendar calendar object for today's date
     * @return moon index 0..7
     */
    public static int computePhaseIndex(Calendar cal) {

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // 0 = Jan, 1 = Feb, etc.
        int day = cal.get(Calendar.DATE);
        int hour = cal.get(Calendar.HOUR);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);

        double dayExact = day + hour / 24.0 + min / 1440.0 + sec / 86400.0;

        if (month > 12) {
            month = 0;
        }

        int[] dayYear = {-1, -1, 30, 58, 89, 119,
                150, 180, 211, 241, 272,
                303, 333};
        // Day in the year
        double diy = dayExact + dayYear[month];                // Day in the year

        if ((month > 2) && isLeapYearP(year)) {
            diy++;
        }                                  // Leapyear fixup
        // Century number (1979 = 20)
        int cent = (year / 100) + 1;                    // Century number
        // Moon's golden number
        int golden = (year % 19) + 1;                   // Golden number
        // Age of the moon on Jan. 1
        int epact = ((11 * golden) + 20                 // Golden number
                     + (((8 * cent) + 5) / 25) - 5       // 400 year cycle
                     - (((3 * cent) / 4) - 12)) % 30;    //Leap year correction
        if (epact <= 0) {
            epact += 30;
        }                        // Age range is 1 .. 30
        if ((epact == 25 && golden > 11) ||
            epact == 24) {
            epact++;

            // Calculate the phase, using the magic numbers defined above.
            // Note that (phase and 7) is equivalent to (phase mod 8) and
            // is needed on two days per year (when the algorithm yields 8).
        }

        // Calculate the phase, using the magic numbers defined above.
        // Note that (phase and 7) is equivalent to (phase mod 8) and
        // is needed on two days per year (when the algorithm yields 8).
        // this.factor = ((((diy + (double)epact) * 6) + 11) % 100 );
        // Moon phase

        return ((((((int) diy + epact) * 6 + 11) % 177) / 22) & 7);
    }

    /**
     * isLeapYearP Return true if the year is a leapyear
     */
    private static boolean isLeapYearP(int year) {
        return ((year % 4 == 0) &&
                ((year % 400 == 0) || (year % 100 != 0)));
    }
}

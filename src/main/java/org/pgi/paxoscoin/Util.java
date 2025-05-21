package org.pgi.paxoscoin;

import java.text.DecimalFormat;

public class Util {
    public static final int EXERCISE_NUMBER = 4;

    private static DecimalFormat df = new DecimalFormat("€ ###,###,###.##");

    public static String formatCurrency(double amount) {
        return df.format(amount);
    }
}

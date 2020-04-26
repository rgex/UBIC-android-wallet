package network.ubic.ubic.AsyncTasks;

import android.icu.util.TimeZone;
import android.os.Build;

public class APIServerSelector {
    public static String getBestServer() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TimeZone tz = TimeZone.getDefault();
            String tzShort = tz.getDisplayName(false, TimeZone.SHORT);

            if(tzShort.equals("GMT+5") || tzShort.equals("GMT+6") || tzShort.equals("GMT+7") || tzShort.equals("GMT+8")
                    || tzShort.equals("GMT+9") || tzShort.equals("GMT+10") || tzShort.equals("GMT+11") || tzShort.equals("GMT+12")) {
                System.out.println("Using the https://ubic.asia API");
                return "https://ubic.asia";
            }
        }

        System.out.println("Using the https://ubic.network API");
        return "https://ubic.network";
    }
}

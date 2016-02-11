package eu.principalmedia.androidplayer.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Ovidiu on 2/8/2016.
 */
public class TimeUtils {

    public static final int MILLIS_IN_SECOND = 1000;

    public static String millisToTimeString(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

    }

}

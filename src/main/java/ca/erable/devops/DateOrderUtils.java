package ca.erable.devops;

import java.util.Date;

public class DateOrderUtils {

    /**
     * This method will return the latest of two dates. It will support nulls. If
     * both parameters are null, then it will return null.
     * 
     * @return Latest date or null
     */
    public static Date returnLatest(Date first, Date second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        }

        if (first.after(second)) {
            return first;
        }

        return second;
    }

}

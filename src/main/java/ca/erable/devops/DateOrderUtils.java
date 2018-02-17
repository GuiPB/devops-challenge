package ca.erable.devops;

import java.util.Date;

/**
 * Classe utilitaire de date
 * 
 * @author guillaume
 *
 */
public class DateOrderUtils {

    /**
     * Fonction qui retourne la date la plus récente. La fonction supporte les
     * valeurs nulles. Si les deux paramètres sont nulls, la methode va retourner
     * null.
     * 
     * @return Date la plus récente, retourne null si les deux dates sont nulles
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

package miage.fr.gestionprojet.outils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Audrey on 27/02/2017.
 */

public class Outils {

    private static final long CONST_DURATION_OF_DAY = 1000l * 60 * 60 * 24;

    private Outils() {
        // private constructor
    }

    public static int calculerPourcentage(double valeurRelevee, double valeurCible){
        return (int) ((valeurRelevee / valeurCible) * 100);
    }

    public static Date weekOfYearToDate(int year, int week){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.WEEK_OF_YEAR,week);
        c.set(Calendar.YEAR,year);
        return c.getTime();
    }

    public static long dureeEntreDeuxDates(Date dateInf, Date datePost){
        return (datePost.getTime() - dateInf.getTime()) / CONST_DURATION_OF_DAY;
    }
}

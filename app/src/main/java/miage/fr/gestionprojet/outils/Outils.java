package miage.fr.gestionprojet.outils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Audrey on 27/02/2017.
 */

public class Outils {

    private static final long CONST_DURATION_OF_DAY = 1000l * 60 * 60 * 24;

    public static int calculerPourcentage(double valeurReleve, double valeurCible){
        int result = (int) ((valeurReleve/valeurCible)*100);
        return result;
    }

    public static Date weekOfYearToDate(int year, int week){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.WEEK_OF_YEAR,week);
        c.set(Calendar.YEAR,year);
        return c.getTime();
    }

    public static long dureeEntreDeuxDates(Date dateInf, Date datePost){
        long duree = datePost.getTime() - dateInf.getTime();
        return duree / CONST_DURATION_OF_DAY;
    }
}

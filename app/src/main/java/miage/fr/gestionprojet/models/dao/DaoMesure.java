package miage.fr.gestionprojet.models.dao;

import com.activeandroid.query.Select;

import java.util.List;

import miage.fr.gestionprojet.models.Mesure;

public class DaoMesure {

    private DaoMesure() {
        // private constructor for static class
    }

    public static Mesure getLastMesureBySaisieCharge(long idSaisieCharge){
        List<Mesure> mesures =
                new Select()
                .from(Mesure.class)
                .where("action=?", idSaisieCharge)
                .orderBy("dt_mesure DESC")
                .execute();
        if (!mesures.isEmpty()) {
            return mesures.get(0);
        } else {
            return new Mesure();
        }
    }

    public static List<Mesure> getMesureByAction(long idSaisieCharge) {
        return new Select()
                .from(Mesure.class)
                .where("action=?", idSaisieCharge)
                .execute();
    }

    public static List<Mesure> loadAll() {
        return new Select().from(Mesure.class).execute();
    }
}

package miage.fr.gestionprojet.models.dao;

import com.activeandroid.query.Select;

import java.util.List;

import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.models.Formation;

/**
 * Created by utilisateur on 06/04/2018.
 */

public class DaoEtapeFormation {

    private DaoEtapeFormation() {
        // private constructor for static class
    }

    public static List<EtapeFormation> getEtapeFormationByFormation(Formation formation) {
        return new Select()
                .from(EtapeFormation.class)
                .where("formation = ?", String.valueOf(formation.getId()))
                .execute();
    }

}

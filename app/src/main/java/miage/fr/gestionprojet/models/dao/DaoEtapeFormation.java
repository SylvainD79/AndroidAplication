package miage.fr.gestionprojet.models.dao;

import com.activeandroid.query.Select;

import java.util.List;

import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.models.Formation;

/**
 * Created by utilisateur on 06/04/2018.
 */

public class DaoEtapeFormation {

    public static List<EtapeFormation> getEtapeFormationByFormation(Formation formation) {
        List<EtapeFormation> listeEtapeFormation = new Select()
                .from(EtapeFormation.class)
                .where("formation = ?", String.valueOf(formation.getId()))
                .execute();

        return listeEtapeFormation;
    }

    public static List<EtapeFormation> allEtapeFormation() {
        List<EtapeFormation> listeEtapeFormation = new Select()
                .from(EtapeFormation.class)
                .execute();

        return listeEtapeFormation;
    }
}

package miage.fr.gestionprojet.models.dao;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

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

    public static EtapeFormation getEtapeFormationById(Long id) {
        return new Select()
                .from(EtapeFormation.class)
                .where("id = ?", id)
                .executeSingle();
    }

    public static void modificationDonnee(long id,String commentaire, boolean check) {
        new Update(EtapeFormation.class)
                .set("commentaire = ?", commentaire)
                .where("id = ?", id)
                .execute();

        new Update(EtapeFormation.class)
                .set("objectif_atteint = ?", check)
                .where("id = ?", id)
                .execute();

    }

}

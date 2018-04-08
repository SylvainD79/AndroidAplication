package miage.fr.gestionprojet.models.dao;

import com.activeandroid.Model;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.Mesure;
import miage.fr.gestionprojet.models.Projet;
import miage.fr.gestionprojet.models.SaisieCharge;

public class DaoSaisieCharge {

    private DaoSaisieCharge() {
        // private constructor for static class
    }

    private static final String DOMAINE_FILTER = "domaine=?";
    private static final String ACTION_FILTER = "action=?";
    private static final String SAISIE_FILTER = "Saisie";

    public static List<SaisieCharge> loadSaisiesByAction(Action action) {
        return new Select().from(SaisieCharge.class).where(ACTION_FILTER,action.getId()).execute();

    }
    public static List<SaisieCharge> loadAll() {
        return new Select().from(SaisieCharge.class).execute();

    }

    public static List<SaisieCharge> loadSaisiesChargesByDomaine(int idDomaine){
        List<SaisieCharge> saisiesCharge = new ArrayList<>();
        List<Action> actions = new Select()
                .from(Action.class)
                .where(DOMAINE_FILTER,idDomaine)
                .execute();
        for(Action action : actions) {
            if (action.getTypeTravail().equalsIgnoreCase(SAISIE_FILTER)
                    || action.getTypeTravail().equalsIgnoreCase("Test")) {
                   SaisieCharge saisieCharge = (SaisieCharge) new Select()
                        .from(SaisieCharge.class)
                        .where(ACTION_FILTER, String.valueOf(action.getId()))
                        .execute().get(0);
                saisiesCharge.add(saisieCharge);
            }
        }
        return saisiesCharge;
    }

    public static List<SaisieCharge> loadSaisiesChargeByUtilisateur(int idUser){
        List<SaisieCharge> saisiesCharges = new ArrayList<>();
        List<Action> actions = new Select()
                .from(Action.class)
                .where("resp_ouv=? or resp_oeu=?",idUser,idUser)
                .execute();
        for(Action action : actions) {
            if (action.getTypeTravail().equalsIgnoreCase(SAISIE_FILTER)
                    || action.getTypeTravail().equalsIgnoreCase("Test")) {
                SaisieCharge saisieCharge = (SaisieCharge) new Select()
                        .from(SaisieCharge.class)
                        .where(ACTION_FILTER, action.getId())
                        .execute().get(0);
                saisiesCharges.add(saisieCharge);
            }
        }
        return saisiesCharges;
    }

    public static SaisieCharge loadSaisiesChargeByAction(long idAction){
        List<SaisieCharge> saisiesCharge = new Select()
                .from(SaisieCharge.class)
                .where(ACTION_FILTER, idAction)
                .execute();
        if (!saisiesCharge.isEmpty()) {
            return saisiesCharge.get(0);
        } else {
            return null;
        }
    }

    public static int getNbUnitesSaisies(long idProjet){
        Projet projet = Model.load(Projet.class, idProjet);
        List<Domaine> domaines = projet.getLstDomaines();
        ArrayList<Action> actions = new ArrayList<>();
        for (Domaine domaine : domaines) {
            actions.addAll(domaine.getActions());
        }
        int nbUnitesSaisies = 0;
        for(Action action : actions){
            if (action.getTypeTravail().equalsIgnoreCase(SAISIE_FILTER)
                    || action.getTypeTravail().equalsIgnoreCase("Test")){
                SaisieCharge saisieCharge = DaoSaisieCharge.loadSaisiesChargeByAction(action.getId());
                if (saisieCharge != null) {
                    Mesure mesure = DaoMesure.getLastMesureBySaisieCharge(saisieCharge.getId());
                    nbUnitesSaisies += mesure.getNbUnitesMesures();
                }
            }
        }
        return nbUnitesSaisies;
    }

    public static int getNbUnitesCibles(long idProjet){
        Projet projet = Model.load(Projet.class, idProjet);
        List<Domaine> domaines = projet.getLstDomaines();
        ArrayList<Action> actions = new ArrayList<>();
        for(Domaine domaine : domaines){
            actions.addAll(domaine.getActions());
        }
        int nbUnitesCibles = 0;
        for(Action action : actions){
            if(action.getTypeTravail().equalsIgnoreCase(SAISIE_FILTER)
                    || action.getTypeTravail().equalsIgnoreCase("Test")){
                SaisieCharge saisieCharge = DaoSaisieCharge.loadSaisiesChargeByAction(action.getId());
                if (saisieCharge != null) {
                    nbUnitesCibles += saisieCharge.getNbUnitesCibles();
                }
            }
        }
        return nbUnitesCibles;
    }
}

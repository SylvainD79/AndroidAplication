package miage.fr.gestionprojet.models.dao;

import com.activeandroid.Model;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.Formation;
import miage.fr.gestionprojet.models.Projet;

public class DaoFormation {

    private DaoFormation() {
        // private constructor for static class
    }

    public static List<Formation> getFormations() {
        return new Select().from(Formation.class).execute();
    }

    public static Formation getFormation(long id) {
        return new Select().from(Formation.class).where("Id = ?", String.valueOf(id)).executeSingle();
    }

    public static Formation getFormationByAction(Action action) {
        return new Select()
                .from(Formation.class)
                .where("action = ?", action.getId())
                .executeSingle();
    }

    public static float getAvancementTotal (long idProjet){
        Projet projet = Model.load(Projet.class,idProjet);
        List<Domaine> domaines = projet.getLstDomaines();
        ArrayList<Action> actions = new ArrayList<>();
        for (Domaine domaine: domaines) {
            actions.addAll(domaine.getActions());
        }
        ArrayList<Formation> formations = new ArrayList<>();
        float avancementTotal = 0;
        for (Action action : actions) {
            if (action.getTypeTravail().equalsIgnoreCase("Formation")) {
                Formation form = new Select().from(Formation.class).where("action = ?",action.getId()).executeSingle();
                formations.add(form);
                if (form!=null) {
                    avancementTotal += form.getAvancementTotal();
                }
            }
        }
        if (!formations.isEmpty()) {
            avancementTotal /= formations.size();
        }
        return avancementTotal;

    }
    public static List<Formation> loadAll() {
        return new Select().from(Formation.class).execute();
    }
}

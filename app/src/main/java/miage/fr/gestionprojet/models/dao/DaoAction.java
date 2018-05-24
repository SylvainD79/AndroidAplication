package miage.fr.gestionprojet.models.dao;

import android.database.Cursor;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.Projet;

/**
 * Created by Audrey on 07/04/2017.
 */

public class DaoAction {

    private DaoAction() {
        // private constructor for static class
    }

    public static List<Action> loadActionsByCode(String code) {
        return new Select()
                .from(Action.class)
                .where("code=?",code)
                .execute();
    }

    public static Action loadActionByCodeSingle(String code) {
        return (Action) new Select()
                .from(Action.class)
                .where("code=?",code)
                .execute().get(0);
    }

    public static List<Action> loadActionsByType(String type, long idProjet) {
        Projet proj = Model.load(Projet.class, idProjet);
        ArrayList<Action> lstActions = new ArrayList<>();
        for(Domaine d : proj.getLstDomaines()) {
            List<Action> actions = new Select().from(Action.class)
                    .where("typeTravail = ? and domaine=?", type, d.getId())
                    .execute();
            lstActions.addAll(actions);
        }
        return lstActions;
    }

    public static List<Action> loadActionsByPhaseAndDate(String phase,Date d, long idProjet) {
        Projet proj = Model.load(Projet.class, idProjet);
        ArrayList<Action> actions = new ArrayList<>();

        /* Création des date du début et de la fin de semaine */
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date debut = cal.getTime();
        cal.add(Calendar.DATE, 6);
        Date fin = cal.getTime();

        /* Recherche des action selon la date de début et
           de fin de semaine, de numéro de la phase, et des domaines
         */
        for(Domaine domaine : proj.getLstDomaines()) {
            List<Action> domaineActions = new Select()
                    .from(Action.class)
                    .where("phase=? and ((dt_debut <= ? and dt_debut >= ?) or " +
                                    "(dt_fin_prevue <= ? and dt_fin_prevue >= ?) or " +
                                    "(dt_debut <= ? and dt_fin_prevue >= ?) or " +
                                    "(dt_debut >= ? and dt_fin_prevue <= ?)) and domaine = ?",
                            phase,
                            fin.getTime(), debut.getTime(),
                            fin.getTime(), debut.getTime(),
                            debut.getTime(), debut.getTime(),
                            debut.getTime(), fin.getTime(), domaine.getId())
                    .execute();
            actions.addAll(domaineActions);
        }
        return actions;
    }

    public static List<Action> loadActionsByDate(Date date, long idProjet) {
        Projet projet = Model.load(Projet.class, idProjet);
        ArrayList<Action> actions = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date debut = cal.getTime();
        cal.add(Calendar.DATE, 6);
        Date fin = cal.getTime();

        for(Domaine domaine : projet.getLstDomaines()) {
            List<Action> domaineActions = new Select()
                    .from(Action.class)
                    .where("((dt_debut <= ? and dt_debut >= ?) or " +
                            "(dt_fin_prevue <= ? and dt_fin_prevue >= ?) or " +
                            "(dt_debut <= ? and dt_fin_prevue >= ?) or " +
                            "(dt_debut >= ? and dt_fin_prevue <= ?)) and domaine = ?",
                            fin.getTime(), debut.getTime(),
                            fin.getTime(), debut.getTime(),
                            debut.getTime(), debut.getTime(),
                            debut.getTime(), fin.getTime(), domaine.getId())
                    .execute();
            actions.addAll(domaineActions);
        }
        return actions;
    }

    public static List<Action> loadAll(){
        return new Select().from(Action.class).execute();
    }

    public static List<Action> loadActionsOrderByNomAndDate(Date d, long idProjet){
        Projet proj = Model.load(Projet.class, idProjet);
        ArrayList<Action> lstActions = new ArrayList<>();
        for(Domaine dom : proj.getLstDomaines()) {
            List<Action> actions = new Select()
                    .from(Action.class)
                    .where("dt_fin_prevue>=? and dt_debut<=? and domaine=?", d.getTime(), d.getTime(), dom.getId())
                    .orderBy("code ASC")
                    .execute();
            lstActions.addAll(actions);
        }
        return lstActions;
    }

    public static List<Action> loadActionsByDomaineAndDate(int idDomaine, Date d, long idProjet){


        /* Création des date du début et de la fin de semaine */
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date debut = cal.getTime();
        cal.add(Calendar.DATE, 6);
        Date fin = cal.getTime();

        List<Action> result = new Select()
                .from(Action.class)
                .where("domaine=? and dt_debut>=? " +
                        "and dt_fin_prevue<=?",
                        idDomaine, debut.getTime(),
                        fin.getTime())
                .execute();
        return result;
    }
    public static List<Action> getActionbyCode(String id) {
        return new Select()
                .from(Action.class)
                .where("code = ?", id)
                .execute();
    }


    public static Map<String,Integer> getNbActionRealiseeGroupByDomaine(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total, domaine FROM " + tableName + " WHERE reste_a_faire=0 GROUP BY domaine", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
               lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }

    public static Map<String,Integer> getNbActionTotalGroupByDomaine(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total, domaine FROM " + tableName + " GROUP BY domaine", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
                lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }


    public static Map<String,Integer> getNbActionRealiseeGroupByTypeTravail(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total,typeTravail FROM " + tableName + " WHERE reste_a_faire=0 GROUP BY typeTravail", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
                lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }

    public static Map<String,Integer> getNbActionTotalGroupByTypeTravail(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total, typeTravail FROM " + tableName + " GROUP BY typeTravail", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
                lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }

    public static List<String> getLstTypeTravail(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT DISTINCT(typeTravail) FROM " + tableName, null);
        List<String> lstResults = new ArrayList<>();

        try {
            while (c.moveToNext()) {
                lstResults.add(c.getString(0));
            }
        } finally {
            c.close();
        }

        return lstResults;
    }


    public static Map<String,Integer> getNbActionRealiseeGroupByUtilisateurOeu(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total,resp_oeu FROM " + tableName + " WHERE reste_a_faire=0 GROUP BY resp_oeu", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
                lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }

    public static Map<String,Integer> getNbActionRealiseeGroupByUtilisateurOuv(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total,resp_ouv FROM " + tableName + " WHERE reste_a_faire=0 GROUP BY resp_ouv", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
                lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }

    public static Map<String,Integer> getNbActionTotalGroupByUtilisateurOeu(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total, resp_oeu FROM " + tableName + " GROUP BY resp_oeu", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
                lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }

    public static Map<String,Integer> getNbActionTotalGroupByUtilisateurOuv(){
        String tableName = Cache.getTableInfo(Action.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT COUNT(*) as total, resp_ouv FROM " + tableName + " GROUP BY resp_ouv", null);
        HashMap<String,Integer> lstResult = new HashMap<>();

        try {
            while (c.moveToNext()) {
                lstResult.put(c.getString(1),c.getInt(0));
            }
        } finally {
            c.close();
        }

        return lstResult;
    }

    public static List<Action> getActionsRealiseesByProjet(long idProjet){
        Projet proj = Model.load(Projet.class,idProjet);
        List<Domaine> lstDomaines = proj.getLstDomaines();
        List<Action> lstActionRealisees = new ArrayList<>();
        for(Domaine d: lstDomaines){
            List<Action> lstActionRecuperees = new Select()
                    .from(Action.class)
                    .where("reste_a_faire=0 and domaine=?",d.getId())
                    .execute();
            lstActionRealisees.addAll(lstActionRecuperees);
        }
        return lstActionRealisees;
    }

    public static List<Action> getAllActionsByProjet(long idProjet){
        Projet proj = Model.load(Projet.class,idProjet);
        List<Domaine> lstDomaines = proj.getLstDomaines();
        List<Action> lstAction = new ArrayList<>();
        for(Domaine d: lstDomaines){
            List<Action> lstActionRecuperees = new Select()
                    .from(Action.class)
                    .where("domaine=?",d.getId())
                    .execute();
            lstAction.addAll(lstActionRecuperees);
        }
        return lstAction;
    }

    public static List<Action> loadActionsByDateAndType(Date dateSaisie, String type, long idProjet) {
        Projet proj = Model.load(Projet.class, idProjet);
        ArrayList<Action> actions = new ArrayList<>();

        /* Création des date du début et de la fin de semaine */
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateSaisie);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date debut = cal.getTime();
        cal.add(Calendar.DATE, 6);
        Date fin = cal.getTime();

        /* Recherche des action selon la date de début et
           de fin de semaine, de numéro de la phase, et des domaines
         */
        for(Domaine domaine : proj.getLstDomaines()) {
            List<Action> domaineActions = new Select()
                    .from(Action.class)
                    .where("typeTravail = ? and ((dt_debut <= ? and dt_debut >= ?) or " +
                                    "(dt_fin_prevue <= ? and dt_fin_prevue >= ?) or " +
                                    "(dt_debut <= ? and dt_fin_prevue >= ?) or " +
                                    "(dt_debut >= ? and dt_fin_prevue <= ?)) and domaine = ?",
                            type,
                            fin.getTime(), debut.getTime(),
                            fin.getTime(), debut.getTime(),
                            debut.getTime(), debut.getTime(),
                            debut.getTime(), fin.getTime(), domaine.getId())
                    .execute();
            actions.addAll(domaineActions);
        }
        return actions;


    }
}

package miage.fr.gestionprojet.models.dao;

import android.database.Cursor;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Select;

import java.util.ArrayList;
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
        ArrayList<Action> lstActions = new ArrayList<>();
        for(Domaine dom : proj.getLstDomaines()) {
            List<Action> actions = new Select().from(Action.class)
                    .where("phase = ? and dt_fin_prevue>=? and dt_debut<=? and domaine=?", phase, d.getTime(), d.getTime(), dom.getId())
                    .execute();
            lstActions.addAll(actions);
        }
        return lstActions;
    }

    public static List<Action> loadActionsByDate(Date d, long idProjet) {
        Projet proj = Model.load(Projet.class, idProjet);
        ArrayList<Action> lstActions = new ArrayList<>();
        for(Domaine dom : proj.getLstDomaines()) {
            List<Action> actions = new Select()
                    .from(Action.class)
                    .where("dt_fin_prevue>=? and dt_debut<=? and domaine = ?", d.getTime(), d.getTime(), dom.getId())
                    .execute();
            lstActions.addAll(actions);
        }
        return lstActions;
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

    public static List<Action> loadActionsByDomaineAndDate(int idDomaine,Date d, long idProjet){
        Projet proj = Model.load(Projet.class, idProjet);
        List<Action> lstActions = new ArrayList<>();
        for(Domaine dom : proj.getLstDomaines()) {
            List<Action> result = new Select()
                    .from(Action.class)
                    .where("domaine=? and dt_fin_prevue>=? and dt_debut<=? and domaine=?", idDomaine, d.getTime(), d.getTime(),dom.getId())
                    .execute();
            lstActions.addAll(result);
        }
        return lstActions;
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
}

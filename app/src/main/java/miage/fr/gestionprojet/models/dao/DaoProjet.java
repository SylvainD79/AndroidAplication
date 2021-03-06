package miage.fr.gestionprojet.models.dao;

import android.database.Cursor;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.query.Select;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.Projet;

public class DaoProjet {

    private DaoProjet() {
        // private constructor for static class
    }

    public static List<Projet> getProjetsEnCours(Date dateDuJour){
        return new Select()
                .from(Projet.class)
                .where("date_fin_initiale>? or date_fin_reelle>?", dateDuJour.getTime(),dateDuJour.getTime())
                .execute();
    }
    public static List<Projet> loadAll(){
        return new Select().from(Projet.class).execute();
    }

    public static Date getDateFin(long idProjet){
        String tableNameAction = Cache.getTableInfo(Action.class).getTableName();
        String tableNameDomaine = Cache.getTableInfo(Domaine.class).getTableName();
        String tableNameProjet = Cache.getTableInfo(Projet.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT max(a.dt_fin_prevue) FROM " + tableNameAction
                        + " a INNER JOIN "+ tableNameDomaine + " d ON a.domaine = d.id INNER JOIN "
                        + tableNameProjet +" p ON d.projet = p.id WHERE p.id = "+idProjet, null);
        Date dateFinPrevu;
        if (c.moveToFirst()) {
            Calendar.getInstance().setTimeInMillis(c.getLong(0));
            dateFinPrevu = Calendar.getInstance().getTime();
            return dateFinPrevu;
        }
        return null;
    }

    public static Date getDateDebut(long idProjet){
        String tableNameAction = Cache.getTableInfo(Action.class).getTableName();
        String tableNameDomaine = Cache.getTableInfo(Domaine.class).getTableName();
        String tableNameProjet = Cache.getTableInfo(Projet.class).getTableName();
        Cursor c = ActiveAndroid
                .getDatabase()
                .rawQuery("SELECT min(a.dt_debut) FROM " + tableNameAction
                        + " a INNER JOIN "+ tableNameDomaine + " d ON a.domaine = d.id INNER JOIN "
                        +tableNameProjet +" p ON d.projet = p.id WHERE p.id = "+idProjet, null);
        Date dateDebut;
        if (c.moveToFirst()) {
            Calendar.getInstance().setTimeInMillis(c.getLong(0));
            dateDebut = Calendar.getInstance().getTime();
            return dateDebut;
        }
        return null;
    }
}

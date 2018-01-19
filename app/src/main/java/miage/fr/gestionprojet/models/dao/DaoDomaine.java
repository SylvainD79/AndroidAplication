package miage.fr.gestionprojet.models.dao;

import com.activeandroid.query.Select;

import java.util.ArrayList;

import miage.fr.gestionprojet.models.Domaine;

/**
 * Created by Audrey on 26/04/2017.
 */

public class DaoDomaine {

    public static ArrayList<Domaine> loadAll(){
        return new Select()
                .from(Domaine.class)
                .execute();
    }

    public static Domaine getByName(String name){
        ArrayList<Domaine> lst = new Select()
                .from(Domaine.class)
                .where("nom = ?",name)
                .execute();
        if(lst.size()>0){
            return lst.get(0);
        }else{
            return null;
        }
    }
}

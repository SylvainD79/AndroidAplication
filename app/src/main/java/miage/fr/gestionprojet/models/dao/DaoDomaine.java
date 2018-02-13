package miage.fr.gestionprojet.models.dao;

import com.activeandroid.query.Select;

import java.util.List;

import miage.fr.gestionprojet.models.Domaine;

public class DaoDomaine {

    private DaoDomaine() {
        // private constructor for static class
    }

    public static List<Domaine> loadAll(){
        return new Select()
                .from(Domaine.class)
                .execute();
    }

    public static Domaine getDomaineByName(String name){
        List<Domaine> domaines = new Select()
                .from(Domaine.class)
                .where("nom = ?",name)
                .execute();
        if (!domaines.isEmpty()) {
            return domaines.get(0);
        } else {
            return null;
        }
    }
}

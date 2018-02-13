package miage.fr.gestionprojet.models.dao;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import miage.fr.gestionprojet.models.Ressource;

public class DaoRessource {

    private DaoRessource() {
        // private constructor for static class
    }

    public static List<Ressource> loadAll(){
        return new Select()
                .from(Ressource.class)
                .execute();
    }

    public static List<Ressource> loadAllWithInitialNotEmpty(){
        List<Ressource> ressources = loadAll();
        List<Ressource> ressourcesFinales = new ArrayList<>();
        for (int i=0; i < ressources.size(); i++){
            if (!ressources.get(i).getInitiales().equals("")
                    && ressources.get(i).getInitiales().length() > 0) {
                ressourcesFinales.add(ressources.get(i));
            }
        }
        return ressourcesFinales;
    }

    public static List<String> getAllRessourcesInitiales(){
        List<Ressource> ressources = new Select().from(Ressource.class).execute();
        List<String> initiales = new ArrayList<>();

        for (int i=0; i < ressources.size(); i++){
            if (!ressources.get(i).getInitiales().equals("")
                    && ressources.get(i).getInitiales().length() > 0) {
                initiales.add(ressources.get(i).getInitiales());
            }
        }
        return initiales;
    }

    public static Ressource getRessourcesByInitial(String initiales){
        List<Ressource> ressources = new Select()
                .from(Ressource.class)
                .where("initiales = ?", initiales)
                .execute();
        if (!ressources.isEmpty()) {
            return ressources.get(0);
        } else {
            return null;
        }
    }
}

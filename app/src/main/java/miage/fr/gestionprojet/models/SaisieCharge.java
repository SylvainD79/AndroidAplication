package miage.fr.gestionprojet.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

@Table(name="SaisieCharge")
public class SaisieCharge extends Model {

    @Column(name="action")
    Action action;

    @Column(name="nb_unites_cibles")
    private int nbUnitesCibles;

    @Column(name="heure_par_unite")
    private float heureParUnite;

    // Les attributs suivants ne sont pas chargé à partir des fichiers, ils sont calculés et doivent être mis à jour à chaque nouvelle mesure enregistrée

    @Column(name="charge_totale_estime_en_heure")
    private float chargeTotaleEstimeeEnHeure;

    @Column(name="nb_semaines")
    private Float nbSemaines;

    @Column(name="charge_estime_par_semaine")
    private float chargeEstimeeParSemaine;

    @Column(name="charge_restante_estime_en_heure")
    private float chargeRestanteEstimeeEnHeure;

    @Column(name="nb_semaine_passee")
    private int nbSemainePassee;

    @Column(name="nb_semaines_restantes")
    private int nbSemainesRestantes;

    @Column(name="charge_restante_par_semaine")
    private float chargeRestanteParSemaine;

    @Column(name="temps_passe_par_semaine")
    private float tempsPasseParSemaine;

    @Column(name="prct_charge_faite_par_semaine_par_cahre_estimee")
    private float prctChargeFaiteParSemaineParChargeEstimee;

    private List<Mesure> mesures;

    public SaisieCharge() {
        super();
    }

    public int getNbUnitesCibles() {
        return nbUnitesCibles;
    }

    public void setNbUnitesCibles(int nbUnitesCibles) {
        this.nbUnitesCibles = nbUnitesCibles;
    }

    public float getHeureParUnite() {
        return heureParUnite;
    }

    public void setHeureParUnite(float heureParUnite) {
        this.heureParUnite = heureParUnite;
    }

    public float getChargeTotaleEstimeeEnHeure() {
        return chargeTotaleEstimeeEnHeure;
    }

    public void setChargeTotaleEstimeeEnHeure(float chargeTotaleEstimeeEnHeure) {
        this.chargeTotaleEstimeeEnHeure = chargeTotaleEstimeeEnHeure;
    }

    public Float getNbSemaines() {
        return nbSemaines;
    }

    public void setNbSemaines(Float nbSemaines) {
        this.nbSemaines = nbSemaines;
    }

    public float getChargeEstimeeParSemaine() {
        return chargeEstimeeParSemaine;
    }

    public void setChargeEstimeeParSemaine(float chargeEstimeeParSemaine) {
        this.chargeEstimeeParSemaine = chargeEstimeeParSemaine;
    }

    public float getChargeRestanteEstimeeEnHeure() {
        return chargeRestanteEstimeeEnHeure;
    }

    public void setChargeRestanteEstimeeEnHeure(float chargeRestanteEstimeeEnHeure) {
        this.chargeRestanteEstimeeEnHeure = chargeRestanteEstimeeEnHeure;
    }

    public int getNbSemainePassee() {
        return nbSemainePassee;
    }

    public void setNbSemainePassee(int nbSemainePassee) {
        this.nbSemainePassee = nbSemainePassee;
    }

    public int getNbSemainesRestantes() {
        return nbSemainesRestantes;
    }

    public void setNbSemainesRestantes(int nbSemainesRestantes) {
        this.nbSemainesRestantes = nbSemainesRestantes;
    }

    public float getChargeRestanteParSemaine() {
        return chargeRestanteParSemaine;
    }

    public void setChargeRestanteParSemaine(float chargeRestanteParSemaine) {
        this.chargeRestanteParSemaine = chargeRestanteParSemaine;
    }

    public float getTempsPasseParSemaine() {
        return tempsPasseParSemaine;
    }

    public void setTempsPasseParSemaine(float tempsPasseParSemaine) {
        this.tempsPasseParSemaine = tempsPasseParSemaine;
    }

    public float getPrctChargeFaiteParSemaineParChargeEstimee() {
        return prctChargeFaiteParSemaineParChargeEstimee;
    }

    public void setPrctChargeFaiteParSemaineParChargeEstimee(float prctChargeFaiteParSemaineParChargeEstimee) {
        this.prctChargeFaiteParSemaineParChargeEstimee = prctChargeFaiteParSemaineParChargeEstimee;
    }

    public List<Mesure> getMesures() {
       return getMany(Mesure.class, "action");
    }

    public void setMesures(List<Mesure> mesures) {
      this.mesures = mesures;
    }

    @Override
    public String toString(){
        return this.action.getCode();
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}

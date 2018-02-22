package miage.fr.gestionprojet.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

@Table(name="Action")
public class Action extends Model{

    @Column(name="typeTravail")
    private String typeTravail;

    @Column(name="ordre")
    private int ordre;

    @Column(name="tarif")
    private String tarif;

    @Column(name="phase")
    private String phase;

    @Column(name="code")
    private String code;

    @Column(name="domaine", onDelete = Column.ForeignKeyAction.CASCADE)
    private Domaine domaine;

    @Column(name="apparaitr_planing")
    private boolean apparaitrePlanning;

    @Column(name="type_facturation")
    private String typeFacturation;

    @Column(name="nb_jours_prevus")
    private Float nbJoursPrevus;

    @Column(name="cout_par_pour")
    private float coutParJour;

    @Column(name="resp_oeu", onDelete = Column.ForeignKeyAction.CASCADE)
    private Ressource respOeu;

    @Column(name="resp_ouv", onDelete = Column.ForeignKeyAction.CASCADE)
    private Ressource respOuv;

    //@Column(name="utilisateurs_ouv")
   // private ArrayList<Ressource> lstUtilisateursOuv;

    @Column(name="dt_debut")
    private Date dtDeb;

    @Column(name="dt_fin_prevue")
    private Date dtFinPrevue;

    @Column(name="dt_fin_reelle")
    private Date dtFinReelle;

    @Column(name="reste_a_faire")
    private float resteAFaire;

    @Column(name="ecart_projete")
    private float ecartProjete;

    public Action() {
        super();
    }

    public String getTypeTravail() {
        return typeTravail;
    }

    public void setTypeTravail(String typeTravail) {
        this.typeTravail = typeTravail;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public String getTarif() {
        return tarif;
    }

    public void setTarif(String tarif) {
        this.tarif = tarif;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isApparaitrePlanning() {
        return apparaitrePlanning;
    }

    public void setApparaitrePlanning(boolean apparaitrePlanning) {
        this.apparaitrePlanning = apparaitrePlanning;
    }

    public String getTypeFacturation() {
        return typeFacturation;
    }

    public void setTypeFacturation(String typeFacturation) {
        this.typeFacturation = typeFacturation;
    }

    public Float getNbJoursPrevus() {
        return nbJoursPrevus;
    }

    public void setNbJoursPrevus(float nbJoursPrevus) {
        this.nbJoursPrevus = nbJoursPrevus;
    }

    public float getCoutParJour() {
        return coutParJour;
    }

    public void setCoutParJour(float coutParJour) {
        this.coutParJour = coutParJour;
    }
    public Ressource getRespOeu() {
        return respOeu;
    }

    public void setRespOeu(Ressource respOeu) {
        this.respOeu = respOeu;
    }

    public Ressource getRespOuv() {
        return respOuv;
    }

    public void setRespOuv(Ressource respOuv) {
        this.respOuv = respOuv;
    }

    /*public ArrayList<Ressource> getLstUtilisateursOuv() {
        return lstUtilisateursOuv;
    }

    public void setLstUtilisateursOuv(ArrayList<Ressource> lstUtilisateursOuv) {
        this.lstUtilisateursOuv = lstUtilisateursOuv;
    }*/
    public Date getDtDeb() {
        return dtDeb;
    }

    public void setDtDeb(Date dtDeb) {
        this.dtDeb = dtDeb;
    }

    public Date getDtFinPrevue() {
        return dtFinPrevue;
    }

    public void setDtFinPrevue(Date dtFinPrevue) {
        this.dtFinPrevue = dtFinPrevue;
    }

    public Date getDtFinReelle() {
        return dtFinReelle;
    }

    public void setDtFinReelle(Date dtFinReelle) {
        this.dtFinReelle = dtFinReelle;
    }

    public Domaine getDomaine() {
        return domaine;
    }

    public void setDomaine(Domaine domaine) {
        this.domaine = domaine;
    }

    public String toString(){
        return this.code;
    }

    public float getResteAFaire() {
        return resteAFaire;
    }

    public float getEcartProjete() {
        return ecartProjete;
    }

    public void setResteAFaire(float resteAFaire) {this.resteAFaire = resteAFaire;}

    public void setEcartProjete(float ecartProjete) {this.ecartProjete = ecartProjete;}
}

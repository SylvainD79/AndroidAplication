package miage.fr.gestionprojet.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

@Table(name = "Formation")
public class Formation extends Model {

    @Column(name="action", onDelete = Column.ForeignKeyAction.CASCADE)
    private Action action;

    @Column(name="avancement_total")
    private float avancementTotal;

    @Column(name="avancement_pre_requis")
    private float avancementPreRequis;

    @Column(name="avancement_objectif")
    private float avancementObjectif;

    @Column(name="avancement_post_formation")
    private float avancementPostFormation;

    public Formation() {
        super();
    }

    public List<EtapeFormation> getEtapesFormation() {
        return getMany(EtapeFormation.class, "formation");
    }

    public float getAvancementTotal() {
        return avancementTotal;
    }

    public void setAvancementTotal(float avancementTotal) {
        this.avancementTotal = avancementTotal;
    }

    public float getAvancementPreRequis() {
        return avancementPreRequis;
    }

    public void setAvancementPreRequis(float avancementPreRequis) {
        this.avancementPreRequis = avancementPreRequis;
    }

    public float getAvancementObjectif() {
        return avancementObjectif;
    }

    public void setAvancementObjectif(float avancementObjectif) {
        this.avancementObjectif = avancementObjectif;
    }

    public float getAvancementPostFormation() {
        return avancementPostFormation;
    }

    public void setAvancementPostFormation(float avancementPostFormation) {
        this.avancementPostFormation = avancementPostFormation;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}

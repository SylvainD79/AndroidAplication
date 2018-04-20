package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.models.dao.DaoEtapeFormation;

/**
 * Created by utilisateur on 11/04/2018.
 */

public class DetailPlanFormationActivity extends AppCompatActivity {

    @BindView(R.id.phase)
    TextView phase;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.typeElement)
    TextView typeElement;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.commentaire)
    EditText commentaire;

    @BindView(R.id.responsable)
    TextView acteur;

    @BindView(R.id.checkBox)
    CheckBox actionRealise;

    @BindView(R.id.valider)
    Button valider;

    EtapeFormation dataEtape;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_detail_etape_formation);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDetailPlanData();
        associerData();
    }

    private void getDetailPlanData() {
        Intent intent = getIntent();
        Long idDetail = intent.getLongExtra(FormationActivity.PLAN_FORMATION_SELECTED,0);
        dataEtape = DaoEtapeFormation.getEtapeFormationById(idDetail);
    }
    private void associerData() {
        phase.setText(dataEtape.getFormation().getAction().getPhase());
        name.setText(dataEtape.getFormation().getAction().getCode());
        typeElement.setText(dataEtape.getTypeElement());
        acteur.setText(dataEtape.getTypeActeur());
        description.setText(dataEtape.getDescription());
        commentaire.setText(dataEtape.getCommentaire());

        actionRealise.setChecked(dataEtape.isObjectifAtteint());
    }

    @OnClick(R.id.valider)
    public void validerSaisieEtape() {
        dataEtape.setCommentaire(commentaire.getText().toString());
        dataEtape.setObjectifAtteint(actionRealise.isChecked());
        dataEtape.save();
        finish();
    }
}

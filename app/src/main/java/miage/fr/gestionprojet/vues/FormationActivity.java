package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.EtapeFormationAdapter;
import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.models.Formation;
import miage.fr.gestionprojet.models.dao.DaoEtapeFormation;
import miage.fr.gestionprojet.models.dao.DaoFormation;

public class FormationActivity extends AppCompatActivity {
    public static final String PLAN_FORMATION_SELECTED = "detail-selected";

    // TODO rendre "transparent"
    // TODO afficher les descriptions

    protected Formation formationData;

    List<EtapeFormation> listeEtapeFormation;

    @BindView(R.id.formationName)
    TextView formationName;

    @BindView(R.id.formationPhase)
    protected TextView formationPhase;

    @BindView(R.id.formationDescriptionsList)
    protected ListView formationDescriptionsList;

    @BindView(R.id.formationTotalProgressBar)
    protected ProgressBar formationTotalProgressBar;

    @BindView(R.id.formationPreRequisProgressBar)
    protected ProgressBar formationPreRequisProgressBar;

    @BindView(R.id.formationObjectifProgressBar)
    protected ProgressBar formationObjectifProgressBar;

    @BindView(R.id.formationPostFormatProgressBar)
    protected ProgressBar formationPostFormatProgressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formation_activity);
        ButterKnife.bind(this);

        getFormationData();
        fillFormationData();
        setDetailFormationItemClickListener();
    }


    protected void getFormationData() {
        Intent intent = getIntent();
        long id = intent.getLongExtra(FormationsActivity.FORMATION_SELECTED,0);
        formationData = DaoFormation.getFormation(id);
    }

    protected void fillFormationData() {
        formationName.setText(formationData.getAction().getCode());
        formationPhase.setText(formationData.getAction().getPhase());
        formationTotalProgressBar.setProgress((int) formationData.getAvancementTotal());
        formationPreRequisProgressBar.setProgress((int) formationData.getAvancementPreRequis());
        formationObjectifProgressBar.setProgress((int) formationData.getAvancementObjectif());
        formationPostFormatProgressBar.setProgress((int) formationData.getAvancementPostFormation());

        listeEtapeFormation = DaoEtapeFormation.getEtapeFormationByFormation(formationData);

        EtapeFormationAdapter etapeFormationsAdapter = new EtapeFormationAdapter(this, R.layout.item_etape_formation, listeEtapeFormation);
        formationDescriptionsList.setAdapter(etapeFormationsAdapter);
    }

    protected void setDetailFormationItemClickListener() {
        formationDescriptionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), DetailPlanFormationActivity.class);
                intent.putExtra(PLAN_FORMATION_SELECTED, (listeEtapeFormation.get(i).getId()));
                startActivity(intent);
            }
        });
    }
}

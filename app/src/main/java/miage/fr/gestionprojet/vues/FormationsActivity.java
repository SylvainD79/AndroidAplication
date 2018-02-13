package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.FormationsAdapter;
import miage.fr.gestionprojet.models.Formation;
import miage.fr.gestionprojet.models.dao.DaoFormation;

public class FormationsActivity extends AppCompatActivity {

    private static final String EXTRA_INITIAL = "initial";
    public static final String FORMATION_SELECTED = "formation-selected";
    protected ListView formationsListView;
    protected List<Formation> formations;
    private String initialUtilisateur = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formations);

        initialUtilisateur = getIntent().getStringExtra(EXTRA_INITIAL);
        formationsListView = (ListView) findViewById(R.id.formationsList);
        formations = DaoFormation.getFormations();

        fillFormationsList();
        setFormationItemClickListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial_utilisateur, menu);
        menu.findItem(R.id.initial_utilisateur).setTitle(initialUtilisateur);
        return true;
    }

    protected void fillFormationsList() {
        FormationsAdapter formationsAdapter = new FormationsAdapter(this, R.layout.item_formation, formations);
        formationsListView.setAdapter(formationsAdapter);
        formationsAdapter.notifyDataSetChanged();
        System.out.println("remplissage de la liste");
    }

    protected void setFormationItemClickListener() {
        formationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), FormationActivity.class);
                intent.putExtra(FORMATION_SELECTED, (formations.get(i).getId()));
                startActivity(intent);
            }
        });
    }
}

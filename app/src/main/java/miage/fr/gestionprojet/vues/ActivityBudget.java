package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.activeandroid.Model;

import java.util.ArrayList;
import java.util.List;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.AdapterBudgetDomaine;
import miage.fr.gestionprojet.adapters.AdapterBudgetType;
import miage.fr.gestionprojet.adapters.AdapterBudgetUtilisateur;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.Projet;
import miage.fr.gestionprojet.models.Ressource;
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.models.dao.DaoRessource;

public class ActivityBudget extends AppCompatActivity {
    private ListView liste;
    private ArrayList<String> choixAffichage;
    private String initialUtilisateur;
    private Projet projet;

    private static final String DOMAINE = "Domaine";
    private static final String TYPE = "Type";
    private static final String UTILISATEUR = "Utilisateur";
    public static final String EXTRA_INITIAL = "initial";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        Intent intentInitial = getIntent();
        initialUtilisateur = intentInitial.getStringExtra(EXTRA_INITIAL);
        choixAffichage = new ArrayList<>();
        choixAffichage.add(DOMAINE);
        choixAffichage.add(TYPE);
        choixAffichage.add(UTILISATEUR);

        Spinner spinChoixAffichage = (Spinner) findViewById(R.id.spinnerChoixAffichage);
        this.liste = (ListView) findViewById(R.id.lstViewBudget);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, choixAffichage);
        spinChoixAffichage.setAdapter(adapter);
        //on récupère le projet sélectionné
        Intent intent = getIntent();
        long id =  intent.getLongExtra(ActivityDetailsProjet.PROJET,0);
        if (id > 0) {
            projet = Model.load(Projet.class, id);
        } else {
            projet = new Projet();
        }
        spinChoixAffichage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String valeurSelectionnee = choixAffichage.get(i);
                switch(valeurSelectionnee){
                    case DOMAINE:
                        affichageDomaine();
                        break;

                    case TYPE:
                        affichageType();
                        break;

                    case UTILISATEUR:
                        affichageUtilisateur();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    private void affichageDomaine(){
        List<Domaine> domaines = projet.getLstDomaines();
        AdapterBudgetDomaine adapter = new AdapterBudgetDomaine(ActivityBudget.this,R.layout.lst_view_budget,domaines);
        this.liste.setAdapter(adapter);
    }

    private void affichageType(){
        List<String> types = DaoAction.getLstTypeTravail();
        AdapterBudgetType adapter = new AdapterBudgetType(ActivityBudget.this,R.layout.lst_view_budget,types);
        this.liste.setAdapter(adapter);
    }

    private void affichageUtilisateur(){
        List<Ressource> utilisateurs = DaoRessource.loadAll();
        AdapterBudgetUtilisateur adapter = new AdapterBudgetUtilisateur(ActivityBudget.this,R.layout.lst_view_budget,utilisateurs);
        this.liste.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial_utilisateur, menu);
        menu.findItem(R.id.initial_utilisateur).setTitle(initialUtilisateur);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.initial_utilisateur:
                return true;

            case R.id.charger_donnees:
                Intent intent = new Intent(ActivityBudget.this, ChargementDonnees.class);
                intent.putExtra(EXTRA_INITIAL, (initialUtilisateur));
                startActivity(intent);
                return true;

            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}

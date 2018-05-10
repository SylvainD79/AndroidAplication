package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.Model;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.AdapterSaisieCharge;
import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.Projet;
import miage.fr.gestionprojet.models.Ressource;
import miage.fr.gestionprojet.models.SaisieCharge;
import miage.fr.gestionprojet.models.dao.DaoSaisieCharge;


public class IndicateursSaisieChargeActivity extends AppCompatActivity {

    private List<SaisieCharge> saisiesCharge;

    @BindView(R.id.textViewNomProjetSaisieCharge)
    TextView projectNameTextView;

    @BindView(R.id.listViewSaisieCharge)
    ListView liste;

    public static final String SAISIECHARGE = "saisie charge";
    public static final String EXTRA_INITIAL = "initial";
    private String initialUtilisateur;

    private static final String SAISIE = "Saisie";
    private static final String TEST = "Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicateurs_saisie_charge);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String projectName = intent.getStringExtra("projectName");
        projectNameTextView.setText(projectName);
        //on récupère le projet sélectionné
        long id =  intent.getLongExtra(DetailsProjetActivity.PROJET_VISU,0);
        initialUtilisateur = intent.getStringExtra(DetailsProjetActivity.EXTRA_INITIAL);

        if (id > 0 ) {
            // on récupère les données associées à ce projet
            Projet projet = Model.load(Projet.class, id);
            // on récupère la liste des travaux à afficher
            saisiesCharge = new ArrayList<>();
            List<Domaine> domaines = projet.getLstDomaines();
            for(Domaine domaine : domaines){
                for(Action action : domaine.getActions()){
                    manageSaisiesCharge(action);
                }
            }

            //on affiche cette liste
            final ArrayAdapter<SaisieCharge> adapter = new AdapterSaisieCharge(this, R.layout.list_view_layout_saisie_charge, saisiesCharge);
            liste.setAdapter(adapter);
            liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(IndicateursSaisieChargeActivity.this, DetailsIndicateursSaisieChargeActivity.class);
                    intent.putExtra(SAISIECHARGE, saisiesCharge.get(position).getId());
                    intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                    startActivity(intent);
                }
            });
        } else {
            // si pas de saisiecharge en cours
            ArrayList<String> list = new ArrayList<>(1);
            list.add("Aucune saisie en cours");
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
            liste.setAdapter(adapter);
        }
    }

    private void manageSaisiesCharge(Action action) {
        if(SAISIE.equals(action.getTypeTravail()) || TEST.equals(action.getTypeTravail())){
            SaisieCharge s = DaoSaisieCharge.loadSaisiesChargeByAction(action.getId());
            if(s != null){
                saisiesCharge.add(s);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial_utilisateur, menu);
        menu.findItem(R.id.initial_utilisateur).setTitle(initialUtilisateur);
        getMenuInflater().inflate(R.menu.activity_indicateurs_saisie_charge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.initial_utilisateur:
                return true;

            case R.id.charger_donnees:
                Intent intent = new Intent(IndicateursSaisieChargeActivity.this, ChargementDonneesActivity.class);
                intent.putExtra(EXTRA_INITIAL, (initialUtilisateur));
                startActivity(intent);
                return true;

            case R.id.envoyer_mail:
                Intent intentSendMail = new Intent(IndicateursSaisieChargeActivity.this, SendMailActivity.class);
                startActivity(intentSendMail);
                return true;

            case R.id.menu_trie_utilisateur:
                showPopup("utilisateurs");
                return true;

            case R.id.menu_trie_domaine:
                showPopup("domaine");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<Domaine> getDomainesAffiches(){
        ArrayList<Domaine> domaines = new ArrayList<>();
        for(SaisieCharge s : saisiesCharge){
            if (domaines.indexOf(s.getAction().getDomaine()) < 0) {
                domaines.add(s.getAction().getDomaine());
            }
        }
        return domaines;
    }

    private ArrayList<Ressource> getRessourcesAffiches(){
        ArrayList<Ressource> ressources = new ArrayList<>();
        for(SaisieCharge s : saisiesCharge){
            if(ressources.indexOf(s.getAction().getRespOeu()) < 0 && s.getAction().getRespOeu() != null) {
                ressources.add(s.getAction().getRespOeu());
            }
            if(ressources.indexOf(s.getAction().getRespOuv()) < 0 && s.getAction().getRespOuv() != null) {
                ressources.add(s.getAction().getRespOuv());
            }
        }
        return ressources;
    }

    private void showPopup(String type){
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenu);
        PopupMenu pMenu = new PopupMenu(wrapper,liste);
        Menu menu = pMenu.getMenu();
        if (type.equalsIgnoreCase("domaine")) {
            pMenu.getMenuInflater().inflate(R.menu.popup_menu_domaine,menu);
            pMenu.setGravity(Gravity.CENTER);
            ArrayList<Domaine> doms = getDomainesAffiches();
            for (Domaine domaine : doms) {
                menu.add(0, (int)(long)domaine.getId(), 0, domaine.getNom());
            }
            setMenuItemClickListenerForDomaine(pMenu);
        }

        if (type.equalsIgnoreCase("utilisateurs")) {
            pMenu.getMenuInflater().inflate(R.menu.popup_menu_utilisateur,menu);
            pMenu.setGravity(Gravity.CENTER);
            ArrayList<Ressource> ressources = getRessourcesAffiches();
            for (Ressource ressource : ressources) {
                if(!ressource.getInitiales().isEmpty()) {
                    menu.add(0, (int)(long)ressource.getId(), 0, ressource.getInitiales());
                }
            }
            setMenuItemClickListenerForUser(pMenu);
        }
        pMenu.show();
    }

    private void setMenuItemClickListenerForUser(PopupMenu pMenu) {
        pMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.all) {
                    refreshAdapter(saisiesCharge);
                } else {
                    refreshAdapter(DaoSaisieCharge.loadSaisiesChargeByUtilisateur(item.getItemId()));
                }
                return true;
            }
        });
    }

    private void setMenuItemClickListenerForDomaine(PopupMenu pMenu) {
        pMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.all) {
                    refreshAdapter(saisiesCharge);
                } else {
                    refreshAdapter(DaoSaisieCharge.loadSaisiesChargesByDomaine(item.getItemId()));
                }
                return true;
            }
        });
    }

    private void refreshAdapter(List<SaisieCharge> actions){
        if (!actions.isEmpty()) {
            AdapterSaisieCharge adapter = new AdapterSaisieCharge(this, R.layout.list_view_layout_saisie_charge,actions);
            liste.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}

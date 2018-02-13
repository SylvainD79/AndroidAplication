package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.activeandroid.Model;

import java.util.ArrayList;
import java.util.List;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.AdapterSaisieCharge;
import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.Projet;
import miage.fr.gestionprojet.models.Ressource;
import miage.fr.gestionprojet.models.SaisieCharge;
import miage.fr.gestionprojet.models.dao.DaoSaisieCharge;

public class ActivityIndicateursSaisieCharge extends AppCompatActivity {

    private Projet projet;
    private List<SaisieCharge> saisiesCharge;
    private ListView liste;
    public static final String SAISIECHARGE = "saisie charge";
    public static final String EXTRA_INITIAL = "initial";
    private String initialUtilisateur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicateurs_saisie_charge);
        Intent intent = getIntent();
        //on récupère le projet sélectionné
        long id =  intent.getLongExtra(ActivityDetailsProjet.PROJET_VISU,0);
        liste = (ListView) findViewById(R.id.listViewSaisieCharge);
        initialUtilisateur = intent.getStringExtra(ActivityDetailsProjet.EXTRA_INITIAL);

        if (id > 0 ) {
            // on récupère les données associées à ce projet
            projet = Model.load(Projet.class, id);
            // on récupère la liste des travaux à afficher
            saisiesCharge = new ArrayList<>();
            List<Domaine> domaines = projet.getLstDomaines();
            for(Domaine domaine : domaines){
                for(Action action : domaine.getActions()){
                    if(action.getTypeTravail().equals("Saisie") || action.getTypeTravail().equals("Test")){
                        SaisieCharge s = DaoSaisieCharge.loadSaisiesChargeByAction(action.getId());
                        if(s != null){
                            saisiesCharge.add(s);
                        }
                    }
                }
            }

            //on affiche cette liste
            final ArrayAdapter<SaisieCharge> adapter = new AdapterSaisieCharge(this, R.layout.list_view_layout_saisie_charge, saisiesCharge);
            liste.setAdapter(adapter);
            liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ActivityIndicateursSaisieCharge.this, ActivityDetailsIndicateursSaisieCharge.class);
                    intent.putExtra(SAISIECHARGE, saisiesCharge.get(position).getId());
                    intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
                    startActivity(intent);
                }
            });
        }else{
            // si pas de saisiecharge en cours
            ArrayList<String> list = new ArrayList<>(1);
            list.add("Aucune saisie en cours");
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
            liste.setAdapter(adapter);
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
                Intent intent = new Intent(ActivityIndicateursSaisieCharge.this, ChargementDonnees.class);
                intent.putExtra(EXTRA_INITIAL, (initialUtilisateur));
                startActivity(intent);
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
            if(ressources.indexOf(s.getAction().getRespOeu())<0){
                ressources.add(s.getAction().getRespOeu());
            }
            if(ressources.indexOf(s.getAction().getRespOuv())<0){
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

        if (type.equalsIgnoreCase("utilisateurs")) {
            pMenu.getMenuInflater().inflate(R.menu.popup_menu_utilisateur,menu);
            pMenu.setGravity(Gravity.CENTER);
            ArrayList<Ressource> ressources = getRessourcesAffiches();
            for (Ressource ressource : ressources) {
                menu.add(0, (int)(long)ressource.getId(), 0, ressource.getInitiales());
            }
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
        pMenu.show();
    }

    private void refreshAdapter(List<SaisieCharge> actions){
        if (!actions.isEmpty()) {
            AdapterSaisieCharge adapter = new AdapterSaisieCharge(this, R.layout.list_view_layout_saisie_charge,actions);
            liste.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}

package miage.fr.gestionprojet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Ressource;
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.outils.Outils;
import miage.fr.gestionprojet.vues.ActivityBudget;

/**
 * Created by Audrey on 25/04/2017.
 */

public class AdapterBudgetUtilisateur extends ArrayAdapter<Ressource> {

    private List<Ressource> utilisateurs;
    private ActivityBudget activity;
    private ArrayList<Integer> nbActionsRealisees;
    private ArrayList<Integer> nbActions;

    public AdapterBudgetUtilisateur(ActivityBudget context,  int resource,  List<Ressource> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.utilisateurs = objects;
        chargerNbAction();
    }

    @Override
    public int getCount() {
        return utilisateurs.size();
    }

    @Override
    public Ressource getItem(int position) {
        return utilisateurs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nonnull
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        AdapterBudgetUtilisateur.ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // on récupère la vue à laquelle doit être ajouter l'image
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lst_view_budget, parent, false);
            holder = new AdapterBudgetUtilisateur.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AdapterBudgetUtilisateur.ViewHolder) convertView.getTag();
        }

        // on définit le texte à afficher
        holder.utilisateur.setText(getItem(position).toString());
        String actionsProgress = this.nbActionsRealisees.get(position) + "/" + this.nbActions.get(position);
        holder.nbActionsRealisees.setText(actionsProgress);
        holder.avancement.setProgress(Outils.calculerPourcentage(this.nbActionsRealisees.get(position),this.nbActions.get(position)));
        return convertView;
    }

    private void chargerNbAction(){
        this.nbActions = new ArrayList<>();
        this.nbActionsRealisees = new ArrayList<>();
        HashMap<String, Integer> results= DaoAction.getNbActionRealiseeGroupByUtilisateurOeu();
        if (!results.isEmpty()) {
            for (Ressource ressource : this.utilisateurs) {
                if (results.get(String.valueOf(ressource.getId())) != null) {
                    this.nbActionsRealisees.add(results.get(String.valueOf(ressource.getId())));
                } else {
                    this.nbActionsRealisees.add(0);
                }
            }

        }

        results= DaoAction.getNbActionRealiseeGroupByUtilisateurOuv();
        if (!results.isEmpty()) {
            for (int i = 0; i<this.utilisateurs.size(); i++) {
                if (results.get(String.valueOf(this.utilisateurs.get(i).getId())) != null) {
                    this.nbActionsRealisees.add(i,
                            this.nbActionsRealisees.get(i) + results.get(String.valueOf(this.utilisateurs.get(i).getId())));
                } else {
                    this.nbActionsRealisees.add(0);
                }
            }
        }

        results= DaoAction.getNbActionTotalGroupByUtilisateurOeu();
        if (!results.isEmpty()) {
            for (Ressource ressource : this.utilisateurs){
                if (results.get(String.valueOf(ressource.getId())) != null) {
                    this.nbActions.add(results.get(String.valueOf(ressource.getId())));
                } else {
                    this.nbActions.add(0);
                }
            }
        }

        results= DaoAction.getNbActionTotalGroupByUtilisateurOuv();
        if (!results.isEmpty()) {
            for (int i = 0; i<this.utilisateurs.size(); i++){
                if (results.get(String.valueOf(this.utilisateurs.get(i).getId())) != null) {
                    this.nbActions.add(i,
                            this.nbActions.get(i) + results.get(String.valueOf(this.utilisateurs.get(i).getId())));
                } else {
                    this.nbActions.add(0);
                }
            }
        }
    }

    private class ViewHolder {
        private TextView utilisateur;
        private TextView nbActionsRealisees;
        private ProgressBar avancement;

        public ViewHolder(View v) {
            utilisateur = (TextView) v.findViewById(R.id.typeAffiche);
            nbActionsRealisees = (TextView) v.findViewById(R.id.nbActionRealisees);
            avancement = (ProgressBar) v.findViewById(R.id.progress_bar_budget);
        }
    }
}

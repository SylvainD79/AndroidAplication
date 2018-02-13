package miage.fr.gestionprojet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.outils.Outils;
import miage.fr.gestionprojet.vues.ActivityBudget;

public class AdapterBudgetDomaine extends ArrayAdapter<Domaine> {

    private List<Domaine> domaines;
    private ActivityBudget activity;
    private ArrayList<Integer> nbActionsRealisees;
    private ArrayList<Integer> nbActions;

    public AdapterBudgetDomaine(ActivityBudget context, int resource, List<Domaine> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.domaines = objects;
        chargerNbAction();
    }

    @Override
    public int getCount() {
        return domaines.size();
    }

    @Override
    public Domaine getItem(int position) {
        return domaines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nonnull
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        AdapterBudgetDomaine.ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // on récupère la vue à laquelle doit être ajouter l'image
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lst_view_budget, parent, false);
            holder = new AdapterBudgetDomaine.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AdapterBudgetDomaine.ViewHolder) convertView.getTag();
        }

        // on définit le texte à afficher
        holder.domaine.setText(getItem(position).toString());
        String actionsProgress = this.nbActionsRealisees.get(position) + "/" + this.nbActions.get(position);
        holder.nbActionsRealisees.setText(actionsProgress);
        holder.avancement.setProgress(Outils.calculerPourcentage(this.nbActionsRealisees.get(position),
                this.nbActions.get(position)));
        return convertView;
    }

    private void chargerNbAction(){
        this.nbActions = new ArrayList<>();
        this.nbActionsRealisees = new ArrayList<>();
        Map<String, Integer> results = DaoAction.getNbActionRealiseeGroupByDomaine();
        if (!results.isEmpty()) {
            for (Domaine domaine : this.domaines) {
                if (results.get(String.valueOf(domaine.getId())) != null) {
                    this.nbActionsRealisees.add(results.get(String.valueOf(domaine.getId())));
                } else {
                    this.nbActionsRealisees.add(0);
                }
            }
        }

        results = DaoAction.getNbActionTotalGroupByDomaine();
        if (!results.isEmpty()) {
            for (Domaine d : this.domaines) {
                if (results.get(String.valueOf(d.getId())) != null) {
                    this.nbActions.add(results.get(String.valueOf(d.getId())));
                } else {
                    this.nbActions.add(0);
                }
            }
        }
    }

    class ViewHolder {
        private TextView domaine;
        private TextView nbActionsRealisees;
        private ProgressBar avancement;

        public ViewHolder(View v) {
            domaine = (TextView) v.findViewById(R.id.typeAffiche);
            nbActionsRealisees = (TextView) v.findViewById(R.id.nbActionRealisees);
            avancement = (ProgressBar) v.findViewById(R.id.progress_bar_budget);
        }
    }
}

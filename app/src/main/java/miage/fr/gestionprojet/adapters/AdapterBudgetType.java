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
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.outils.Outils;
import miage.fr.gestionprojet.vues.ActivityBudget;

public class AdapterBudgetType extends ArrayAdapter<String> {

    private List<String> typesTravail;
    private ActivityBudget activity;
    private ArrayList<Integer> nbActionsRealisees;
    private ArrayList<Integer> nbActions;

    public AdapterBudgetType(ActivityBudget context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.typesTravail = objects;
        chargerNbAction();
    }

    @Override
    public int getCount() {
        return typesTravail.size();
    }

    @Override
    public String getItem(int position) {
        return typesTravail.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nonnull
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        AdapterBudgetType.ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // on récupère la vue à laquelle doit être ajouter l'image
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lst_view_budget, parent, false);
            holder = new AdapterBudgetType.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AdapterBudgetType.ViewHolder) convertView.getTag();
        }

        // on définit le texte à afficher
        holder.type.setText(getItem(position).toString());
        String actionsProgress = this.nbActionsRealisees.get(position) + "/" + this.nbActions.get(position);
        holder.actionsRealisees.setText(actionsProgress);
        holder.avancement.setProgress(Outils.calculerPourcentage(this.nbActionsRealisees.get(position),this.nbActions.get(position)));
        return convertView;
    }

    private void chargerNbAction(){
        this.nbActions = new ArrayList<>();
        this.nbActionsRealisees = new ArrayList<>();
        Map<String, Integer> results= DaoAction.getNbActionRealiseeGroupByTypeTravail();
        if (!results.isEmpty()) {
            for (String typeTravail : this.typesTravail){
                if (results.get(typeTravail) != null) {
                    this.nbActionsRealisees.add(results.get(typeTravail));
                } else {
                    this.nbActionsRealisees.add(0);
                }
            }
        }

        results = DaoAction.getNbActionTotalGroupByTypeTravail();
        if (!results.isEmpty()) {
            for (String typeTravail : this.typesTravail){
                if (results.get(typeTravail) != null) {
                    this.nbActions.add(results.get(typeTravail));
                } else {
                    this.nbActions.add(0);
                }
            }
        }
    }
    private class ViewHolder {
        private TextView type;
        private TextView actionsRealisees;
        private ProgressBar avancement;

        public ViewHolder(View v) {
            type = (TextView) v.findViewById(R.id.typeAffiche);
            actionsRealisees = (TextView) v.findViewById(R.id.nbActionRealisees);
            avancement = (ProgressBar) v.findViewById(R.id.progress_bar_budget);
        }
    }
}

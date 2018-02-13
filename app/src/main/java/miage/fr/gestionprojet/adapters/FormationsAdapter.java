package miage.fr.gestionprojet.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.annotation.Nonnull;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Formation;
import miage.fr.gestionprojet.vues.FormationsActivity;

public class FormationsAdapter extends ArrayAdapter<Formation> {

    private List<Formation> formations;

    public FormationsAdapter(FormationsActivity formationsActivity, int ressource, List<Formation> formations) {
        super(formationsActivity, ressource);
        this.formations = formations;
    }

    @Override
    public int getCount() {
        return formations.size();
    }

    @Override
    public Formation getItem(int position) {
        return formations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nonnull
    public View getView(int position, View view, @Nonnull ViewGroup parent) {
        FormationViewHolder formationHolder;
        LayoutInflater formationInflater = (LayoutInflater) this.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);


        if (view == null) {
            view = formationInflater.inflate(R.layout.item_formation, parent, false);
            formationHolder = new FormationsAdapter.FormationViewHolder(view);
            view.setTag(formationHolder);
        } else {
            formationHolder = (FormationsAdapter.FormationViewHolder) view.getTag();
        }

        Formation formation = formations.get(position);
        formationHolder.formationName.setText(formation.getAction().getCode());
        formationHolder.formationPhase.setText(formation.getAction().getPhase());
        String formationPercentage  = ((int) formation.getAvancementTotal()) + "%";
        formationHolder.formationPercentage.setText(formationPercentage);
        formationHolder.formationProgressBar.setProgress((int) formation.getAvancementTotal());

        return view;
    }

    private class FormationViewHolder extends RecyclerView.ViewHolder {

        TextView formationPhase;
        TextView formationName;
        TextView formationPercentage;
        ProgressBar formationProgressBar;
        LinearLayout formationContainer;

        public FormationViewHolder(View itemView) {
            super(itemView);

            formationName = (TextView) itemView.findViewById(R.id.formationName);
            formationPhase = (TextView) itemView.findViewById(R.id.formationPhase);
            formationPercentage = (TextView) itemView.findViewById(R.id.formationPercentage);
            formationProgressBar = (ProgressBar) itemView.findViewById(R.id.formationProgressBar);
            formationContainer = (LinearLayout) itemView.findViewById(R.id.formationContainer);
        }
    }
}

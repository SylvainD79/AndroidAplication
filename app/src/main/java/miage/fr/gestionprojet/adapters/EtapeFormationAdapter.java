package miage.fr.gestionprojet.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import javax.annotation.Nonnull;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.vues.FormationActivity;

/**
 * Created by utilisateur on 06/04/2018.
 */

public class EtapeFormationAdapter extends ArrayAdapter<EtapeFormation> {

    private List<EtapeFormation> etapeFormations;


    public EtapeFormationAdapter(FormationActivity formationActivity, int resource, List<EtapeFormation> listeEtapeFormations) {
        super(formationActivity, resource);
        this.etapeFormations = listeEtapeFormations;
    }

    @Override
    public int getCount() {
        return etapeFormations.size();
    }

    @Override
    public EtapeFormation getItem(int position) {
        return etapeFormations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nonnull
    public View getView(int position, View view, @Nonnull ViewGroup parent) {
        EtapeFormationViewHolder formationHolder;
        LayoutInflater formationInflater = (LayoutInflater) this.getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);


        if (view == null) {
            view = formationInflater.inflate(R.layout.item_etape_formation, parent, false);
            formationHolder = new EtapeFormationViewHolder(view);
            view.setTag(formationHolder);
        } else {
            formationHolder = (EtapeFormationViewHolder) view.getTag();
        }

        EtapeFormation etapeFormation = etapeFormations.get(position);
        formationHolder.typeElement.setText(etapeFormation.getTypeElement());
        formationHolder.typeActeur.setText(etapeFormation.getTypeActeur());
        formationHolder.checkView.setVisibility((etapeFormation.isObjectifAtteint())? View.VISIBLE : View.INVISIBLE);

        return view;
    }


    private class EtapeFormationViewHolder extends RecyclerView.ViewHolder {

        TextView typeActeur;
        TextView typeElement;
        ImageView checkView;

        public EtapeFormationViewHolder(View itemView) {
            super(itemView);

            typeActeur = (TextView) itemView.findViewById(R.id.typeActeur);
            typeElement = (TextView) itemView.findViewById(R.id.typeElement);
            checkView = (ImageView) itemView.findViewById(R.id.check);
        }
    }
}

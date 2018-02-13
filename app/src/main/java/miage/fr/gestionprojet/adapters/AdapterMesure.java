package miage.fr.gestionprojet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Nonnull;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Mesure;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.vues.ActivityMesures;

/**
 * Created by Audrey on 01/02/2017.
 */

public class AdapterMesure extends ArrayAdapter<Mesure>{

    private List<Mesure> mesures;
    private ActivityMesures activity;

    public AdapterMesure(ActivityMesures context, int resource, List<Mesure> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.mesures = objects;
    }

    @Override
    public int getCount() {
        return mesures.size();
    }

    @Override
    public Mesure getItem(int position) {
        return mesures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nonnull
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // on récupère la vue à laquelle doit être ajouter l'image
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lst_view_mesures, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // on définit le text à afficher
        String nbUnites = "Nombre de saisies : " + getItem(position).getNbUnitesMesures();
        holder.nbUnites.setText(nbUnites);
        String date = new SimpleDateFormat(Constants.DATE_FORMAT).format(getItem(position).getDtMesure());
        holder.date.setText(date);
        return convertView;
    }

    private class ViewHolder {
        private TextView nbUnites;
        private TextView date;

        public ViewHolder(View v) {
            nbUnites = (TextView) v.findViewById(R.id.nbUnites);
            date = (TextView) v.findViewById(R.id.dateMesure);
        }
    }
}

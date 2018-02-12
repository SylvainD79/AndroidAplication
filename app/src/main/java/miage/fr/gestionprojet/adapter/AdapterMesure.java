package miage.fr.gestionprojet.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Mesure;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.vues.ActivityMesures;

/**
 * Created by Audrey on 01/02/2017.
 */

public class AdapterMesure extends ArrayAdapter<Mesure>{

    private List<Mesure> lstMesures;
    private ActivityMesures activity;

    public AdapterMesure(ActivityMesures context, int resource, List<Mesure> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.lstMesures = objects;

    }

    @Override
    public int getCount() {
        return lstMesures.size();
    }

    @Override
    public Mesure getItem(int position) {
        return lstMesures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
        holder.nbUnite.setText("Nombre de saisies : "+String.valueOf(getItem(position).getNbUnitesMesures()));
        holder.date.setText(new SimpleDateFormat(Constants.DATE_FORMAT).format(getItem(position).getDtMesure()));
        return convertView;
    }

    private class ViewHolder {
        private TextView nbUnite;
        private TextView date;

        public ViewHolder(View v) {
            nbUnite = (TextView) v.findViewById(R.id.nbUnites);
            date = (TextView) v.findViewById(R.id.dateMesure);
        }
    }

}

package miage.fr.gestionprojet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Nonnull;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Mesure;
import miage.fr.gestionprojet.models.SaisieCharge;
import miage.fr.gestionprojet.models.dao.DaoMesure;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.outils.Outils;
import miage.fr.gestionprojet.vues.IndicateursSaisieChargeActivity;

public class AdapterSaisieCharge extends ArrayAdapter<SaisieCharge>{

    private List<SaisieCharge> saisiesCharge;
    private IndicateursSaisieChargeActivity activity;

    public AdapterSaisieCharge(IndicateursSaisieChargeActivity context, int resource, List<SaisieCharge> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.saisiesCharge = objects;
    }

    @Override
    public int getCount() {
        return saisiesCharge.size();
    }

    @Override
    public SaisieCharge getItem(int position) {
        return saisiesCharge.get(position);
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
            convertView = inflater.inflate(R.layout.list_view_layout_saisie_charge, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // on définit le text à afficher
        holder.action.setText(getItem(position).toString());

        //on récupère la première lettre du domaine associé au travail
        String firstLetter = String.valueOf(getItem(position).getAction().getPhase());

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        // generate random color
        int color = generator.getColor(getItem(position));

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color); // radius in px

        // on ajoute l'image de l'initial du domaine
        holder.imageView.setImageDrawable(drawable);

        // on affiche l'état d'avancment du travail
        Mesure mesure = DaoMesure.getLastMesureBySaisieCharge(getItem(position).getId());
        holder.avancement.setProgress(Outils.calculerPourcentage(mesure.getNbUnitesMesures(),getItem(position).getNbUnitesCibles()));

        String date = new SimpleDateFormat(Constants.DATE_FORMAT).format(getItem(position).getAction().getDtFinPrevue());
        holder.date.setText(date);
        return convertView;
    }

    private class ViewHolder {
        private ImageView imageView;
        private TextView action;
        private ProgressBar avancement;
        private TextView date;

        public ViewHolder(View v) {
            imageView = (ImageView) v.findViewById(R.id.icon_ttravail);
            action = (TextView) v.findViewById(R.id.label);
            avancement = (ProgressBar) v.findViewById(R.id.progress_bar_saisiecharge_crit);
            date = (TextView) v.findViewById(R.id.textViewDateSaisieCharge);
        }
    }
}

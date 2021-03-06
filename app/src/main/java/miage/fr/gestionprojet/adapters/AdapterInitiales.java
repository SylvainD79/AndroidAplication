package miage.fr.gestionprojet.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.List;

import javax.annotation.Nonnull;

import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Ressource;
import miage.fr.gestionprojet.vues.GestionDesInitialsActivity;

public class AdapterInitiales extends ArrayAdapter<Ressource> {

    private List<Ressource> initiales;
    private GestionDesInitialsActivity activity;

    public AdapterInitiales(GestionDesInitialsActivity context, int resource, List<Ressource> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.initiales = objects;
    }

    @Override
    public int getCount() {
        return initiales.size();
    }

    @Override
    public Ressource getItem(int position) {
        return initiales.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nonnull
    public View getView(int position, View convertView, @Nonnull ViewGroup parent) {
        AdapterInitiales.ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // on récupère la vue à laquelle doit être ajouter l'image
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_view_initiales, parent, false);
            holder = new AdapterInitiales.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AdapterInitiales.ViewHolder) convertView.getTag();
        }

        //on récupère la première lettre du domaine associé au travail
        String firstLetter = String.valueOf(getItem(position).getInitiales());

        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        int color = generator.getColor(getItem(position));

        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter, color); // radius in px

        // on ajoute l'image de l'initial du domaine
        holder.imageView.setImageDrawable(drawable);

        return convertView;
    }

    private class ViewHolder {
        private ImageView imageView;

        public ViewHolder(View v) {
            imageView = (ImageView) v.findViewById(R.id.icon_ttravail);
        }
    }
}

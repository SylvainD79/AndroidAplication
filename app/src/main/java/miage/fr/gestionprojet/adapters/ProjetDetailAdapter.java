package miage.fr.gestionprojet.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import miage.fr.gestionprojet.R;

/**
 * Created by Sylvain on 09/05/2018.
 */

public class ProjetDetailAdapter extends ArrayAdapter<String> {
    private Context context;

    public ProjetDetailAdapter(Context context, ArrayList<String> details) {
        super(context,-1,details);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.projet_detail_adapter, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String projetDetails = getItem(position);
        switch(position) {
            case 0: holder.detailsIcon.setImageResource(R.drawable.saisie_icone);
                    holder.detailsText.setText(projetDetails);
                    break;
            case 1: holder.detailsIcon.setImageResource(R.drawable.icone_formations);
                    holder.detailsText.setText(projetDetails);
                    break;
            case 2: holder.detailsIcon.setImageResource(R.drawable.planning_icone);
                    holder.detailsText.setText(projetDetails);
                    break;
            case 3: holder.detailsIcon.setImageResource(R.drawable.budget_icone);
                    holder.detailsText.setText(projetDetails);
                    break;
            default:
                break;
        }
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.details_icon)
        ImageView detailsIcon;

        @BindView(R.id.details_text)
        TextView detailsText;

        ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}

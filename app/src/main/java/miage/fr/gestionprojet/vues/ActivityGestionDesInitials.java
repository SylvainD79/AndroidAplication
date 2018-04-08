package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.activeandroid.ActiveAndroid;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.AdapterInitiales;
import miage.fr.gestionprojet.models.Ressource;
import miage.fr.gestionprojet.models.dao.DaoRessource;

public class ActivityGestionDesInitials extends AppCompatActivity {

    private List<Ressource> ressourcesInitiales = null;
    public static final String EXTRA_INITIAL = "Initial";

    @BindView(R.id.listViewInitials)
    ListView liste;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActiveAndroid.initialize(this);
        setContentView(R.layout.activity_gestion_des_initials);

        ButterKnife.bind(this);

        //on récupère la liste des ressources
        ressourcesInitiales = DaoRessource.loadAllWithInitialNotEmpty();

        // si le nombre de ressource est supérieur à 1 on affiche une liste
        if (!ressourcesInitiales.isEmpty()) {
            //on affiche cette liste
            final ArrayAdapter<Ressource> adapter2 = new AdapterInitiales(this, R.layout.list_view_initiales, ressourcesInitiales);
            liste.setAdapter(adapter2);
            liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Intent intent = new Intent(ActivityGestionDesInitials.this, MainActivity.class);
                    intent.putExtra(EXTRA_INITIAL, (ressourcesInitiales.get(position).getInitiales()));
                    startActivity(intent);
                }
            });
        } else {
                // sinon on affiche un message indiquand qu'il n'y a aucun projet en cours
                ArrayList<String> list = new ArrayList<>(1);
                list.add("Cliquez ici !!");
                final ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list);
                liste.setAdapter(adapter2);

            liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Intent intent = new Intent(ActivityGestionDesInitials.this, MainActivity.class);
                    intent.putExtra(EXTRA_INITIAL,"");
                    startActivity(intent);
                }
            });
        }
    }
}

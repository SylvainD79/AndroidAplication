package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.activeandroid.Model;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.AdapterMesure;
import miage.fr.gestionprojet.models.Mesure;
import miage.fr.gestionprojet.models.SaisieCharge;
import miage.fr.gestionprojet.models.dao.DaoMesure;

public class MesuresActivity extends AppCompatActivity {
    public static final String EXTRA_INITIAL = "initial";
    private String initialUtilisateur;

    @BindView(R.id.lstViewMesures)
    ListView lstViewMesures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesures);
        ButterKnife.bind(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark)));

        Intent intent = getIntent();
        long id = intent.getLongExtra(IndicateursSaisieChargeActivity.SAISIECHARGE,0);
        initialUtilisateur = intent.getStringExtra(EXTRA_INITIAL);

        if (id > 0) {
            Model.load(SaisieCharge.class, id);

            List<Mesure> lstMesures = DaoMesure.getMesureByAction(id);
            final AdapterMesure adapter = new AdapterMesure(this, R.layout.lst_view_mesures, lstMesures);
            lstViewMesures.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial_utilisateur, menu);
        menu.findItem(R.id.initial_utilisateur).setTitle(initialUtilisateur);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.initial_utilisateur:
                return true;

            case R.id.charger_donnees:
                Intent intent = new Intent(MesuresActivity.this, ChargementDonneesActivity.class);
                intent.putExtra(EXTRA_INITIAL, (initialUtilisateur));
                startActivity(intent);
                return true;

            case R.id.envoyer_mail:
                Intent intentSendMail = new Intent(MesuresActivity.this, SendMailActivity.class);
                startActivity(intentSendMail);
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

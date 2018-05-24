    package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.Model;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Mesure;
import miage.fr.gestionprojet.models.SaisieCharge;
import miage.fr.gestionprojet.models.dao.DaoMesure;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.outils.Outils;

public class DetailsIndicateursSaisieChargeActivity extends AppCompatActivity {

    private SaisieCharge saisieCharge = null;
    public static final String EXTRA_INITIAL = "initial";
    public static final String EXTRA_SAISIECHARGE = "saisie charge";
    private String initialUtilisateur;

    @BindView(R.id.textViewSaisieCharge)
    TextView txtSaisieCharge;

    @BindView(R.id.progressBarAvancement)
    CircularProgressBar circularProgressBar;

    @BindView(R.id.textViewPrct)
    TextView txtPrct;

    @BindView(R.id.txtDtDeb)
    TextView txtDateDeb;

    @BindView(R.id.txtDtFin)
    TextView txtDateFin;

    @BindView(R.id.progressBarDate)
    ProgressBar progressBarDate;

    @BindView(R.id.ListViewDetailsSaisieCharge)
    ListView lstViewIndicateur;

    @BindView(R.id.btnMesures)
    Button btnMessures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_indicateurs_saisie_charge);
        ButterKnife.bind(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));

        Intent intent = getIntent();
        long id = intent.getLongExtra(IndicateursSaisieChargeActivity.SAISIECHARGE,0);
        initialUtilisateur = intent.getStringExtra(EXTRA_INITIAL);

        if (id > 0) {
            saisieCharge = Model.load(SaisieCharge.class, id);
            Mesure mesure = DaoMesure.getLastMesureBySaisieCharge(saisieCharge.getId());
            txtSaisieCharge.setText(saisieCharge.toString());

            int progression = Outils.calculerPourcentage(mesure.getNbUnitesMesures(),saisieCharge.getNbUnitesCibles());
            circularProgressBar.setProgress(progression);

            String textViewPrct = "Heure/unite:"+saisieCharge.getHeureParUnite()+"\n"+"ChargeTotale:"+saisieCharge.getChargeTotaleEstimeeEnHeure()
                    +"\n"+"Charge/semaine:"+saisieCharge.getChargeEstimeeParSemaine();
            txtPrct.setText(textViewPrct);


            String dateDeb = new SimpleDateFormat(Constants.DATE_FORMAT).format(saisieCharge.getAction().getDtDeb());
            txtDateDeb.setText(dateDeb);
            String dateFin = new SimpleDateFormat(Constants.DATE_FORMAT).format(saisieCharge.getAction().getDtFinPrevue());
            txtDateFin.setText(dateFin);


            Calendar c = Calendar.getInstance();
            int progress = Outils.calculerPourcentage((double) c.getTimeInMillis() - saisieCharge.getAction().getDtDeb().getTime(),
                    (double) saisieCharge.getAction().getDtFinPrevue().getTime() - saisieCharge.getAction().getDtDeb().getTime());
            progressBarDate.setProgress(progress);


            List<String> indicateurs = new ArrayList<>();
            indicateurs.add("Nombre d'unités produites:"+mesure.getNbUnitesMesures()+"/"+saisieCharge.getNbUnitesCibles());
            indicateurs.add("Temps restant (semaines): "+saisieCharge.getNbSemainesRestantes());
            indicateurs.add("Dernière mesure saisie:"+mesure.getDtMesure());
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, indicateurs);
            lstViewIndicateur.setAdapter(adapter);

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
        switch(id){
            case R.id.initial_utilisateur:
                return true;

            case R.id.charger_donnees:
                Intent intent = new Intent(DetailsIndicateursSaisieChargeActivity.this, ChargementDonneesActivity.class);
                intent.putExtra(EXTRA_INITIAL, (initialUtilisateur));
                startActivity(intent);
                return true;

            case R.id.envoyer_mail:
                Intent intentSendMail = new Intent(DetailsIndicateursSaisieChargeActivity.this, SendMailActivity.class);
                startActivity(intentSendMail);
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btnMesures)
    public void visualiserMesure() {
        Intent intent = new Intent(DetailsIndicateursSaisieChargeActivity.this, MesuresActivity.class);
        intent.putExtra(EXTRA_SAISIECHARGE, saisieCharge.getId());
        intent.putExtra(EXTRA_INITIAL,initialUtilisateur);
        startActivity(intent);
    }
}

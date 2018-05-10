package miage.fr.gestionprojet.vues;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.adapters.FormationsAdapter;
import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.models.Formation;
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.models.dao.DaoFormation;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.outils.GoogleServices;
import miage.fr.gestionprojet.outils.Outils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class FormationsActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String EXTRA_INITIAL = "initial";
    public static final String FORMATION_SELECTED = "formation-selected";

    protected List<Formation> formations;
    private String initialUtilisateur = null;

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};

    private GoogleAccountCredential mCredential;

    private ProgressDialog mProgress;

    private static final String RANGE_PLAN_FORMATION = "Plan de formation!A2:I";
    private static final String RANGE_FORMATIONS = "Indicateurs formation!A3:Z";

    private Context context;

    @BindView(R.id.formationsList)
    ListView formationsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formations);
        ButterKnife.bind(this);
        context = this;

        initialUtilisateur = getIntent().getStringExtra(EXTRA_INITIAL);
        // Initialize credentials and service object.
        mCredential = GoogleServices.getCredential(context, SCOPES);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Mise à jour des données ...");
        getUpdatedFormationsData();
    }

    private void getUpdatedFormationsData() {
        String sheetId = Outils.getSheetId(this);
        if (sheetId == null) {
            Toast.makeText(this, "Pas de document chargé préalablement.", Toast.LENGTH_LONG).show();
        } else if (!GoogleServices.isGooglePlayServicesAvailable(context)) {
            GoogleServices.acquireGooglePlayServices(context);
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!GoogleServices.isDeviceOnline(context)) {
            Toast.makeText(context, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            new FormationsActivity.MakeRequestTask(mCredential, sheetId).execute();
        }
    }

    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(Constants.PREF_ACCOUNT_NAME,null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getUpdatedFormationsData();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential.newChooseAccountIntent(),Constants.REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    Constants.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    String text = "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.";
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                } else {
                    getUpdatedFormationsData();
                }
                break;

            case Constants.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Constants.PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getUpdatedFormationsData();
                    }
                }
                break;

            case Constants.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getUpdatedFormationsData();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
    }

    public class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        private Sheets mService = null;
        private Exception mLastError = null;
        private String sheetId;

        MakeRequestTask(GoogleAccountCredential credential, String sheetId) {
            mService = GoogleServices.getSheetsService(credential);
            this.sheetId = sheetId;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void output) {
            formations = DaoFormation.getFormations();
            fillFormationsList();
            setFormationItemClickListener();
            mProgress.hide();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    GoogleServices.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode(), context);
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Constants.REQUEST_AUTHORIZATION);
                } else {
                    String toToast = "Une erreur est survenue : ";
                    if (mLastError instanceof GoogleJsonResponseException) {
                        GoogleJsonResponseException googleJsonException= (GoogleJsonResponseException) mLastError;
                        if (Constants.ERROR_CODE_NOT_FOUND == googleJsonException.getStatusCode()) {
                            toToast +=  "L'id de ce fichier est introuvable.";
                        } else {
                            toToast += googleJsonException.getStatusMessage();
                        }
                    } else {
                        toToast += mLastError.getMessage();
                    }
                    Toast.makeText(context, toToast, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, "Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                getUpdatedFormationsData();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        private void getUpdatedFormationsData() throws IOException {
            // Récupération des indicateurs mis à jour
            ValueRange responseformation = this.mService.spreadsheets().values()
                    .get(sheetId, RANGE_FORMATIONS)
                    .execute();
            List<List<Object>> valuesformation = responseformation.getValues();
            if (valuesformation != null) {
                intialiserFormation(reglerDonnees(valuesformation));
            }

            // Récupération des plans de formation mis à jour
            ValueRange responsePlanFormation = this.mService.spreadsheets().values()
                    .get(sheetId, RANGE_PLAN_FORMATION)
                    .execute();
            List<List<Object>> valuePlanFormation = responsePlanFormation.getValues();
            if(valuePlanFormation != null) {
                initialiserEtapeFormation(reglerDonnees(valuePlanFormation));
            }
        }

        private List<List<Object>> reglerDonnees(List<List<Object>> values) {
            for (List row : values) {
                int index = 26 - row.size();
                for (int i = 0; i < index; i++) {
                    row.add("");
                }
            }
            return values;
        }

        private void initialiserEtapeFormation(List<List<Object>> values) {
            new Delete().from(EtapeFormation.class).execute();

            ActiveAndroid.beginTransaction();
            int size = values.size();
            for (int i = 0; i < size; i++) {
                List row = values.get(i);
                EtapeFormation etapeFormation = new EtapeFormation();
                List<Action> action = DaoAction.loadActionsByCode(row.get(2).toString());
                if(!action.isEmpty()) {
                    Formation formation = DaoFormation.getFormationByAction(action.get(0));

                    etapeFormation.setTypeElement(row.get(1).toString());
                    etapeFormation.setFormation(formation);
                    etapeFormation.setDescription(row.get(3).toString());
                    etapeFormation.setTypeActeur(row.get(4).toString());
                    etapeFormation.setObjectifAtteint("1".equals(row.get(5).toString()));
                    etapeFormation.setCommentaire(row.get(6).toString());
                    etapeFormation.setIdLigne(i + 2); // seul moyen de récupérer l'id de la ligne dans le spreadhseet (désolé, moi aussi ça me fait gerber)

                    etapeFormation.save();
                }
            }
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
        }

        private void intialiserFormation(List<List<Object>> values) {
            new Delete().from(Formation.class).execute();

            Action action;
            ActiveAndroid.beginTransaction();
            try {

                for (List row : values) {
                    Formation formation = new Formation();
                    List<Action> actionList = DaoAction.getActionbyCode(row.get(5).toString());

                    if (!actionList.isEmpty()){
                        action = actionList.get(0);
                        formation.setAction(action);
                        formation.setAvancementObjectif(chaineToFloat(row.get(8).toString().replace('%', '0')));
                        formation.setAvancementTotal(chaineToFloat(row.get(6).toString().replace('%', '0')));
                        formation.setAvancementPreRequis(chaineToFloat(row.get(7).toString().replace('%', '0')));
                        formation.setAvancementPostFormation(chaineToFloat(row.get(9).toString().replace('%', '0')));
                    }
                    formation.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        private Float chaineToFloat(String s) {
            Float resultat;
            if (s == null || s.equals("") || s.equals("-") || s.matches(".*[a-zA-Z]+.*")
                    || s.equals("RETARD") || s.equals("#DIV/0!")) {
                resultat = (float)0.0;
            } else {
                resultat = Float.parseFloat(s.replace(',', '.'));
            }
            return resultat;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.initial_utilisateur, menu);
        menu.findItem(R.id.initial_utilisateur).setTitle(initialUtilisateur);
        return true;
    }

    protected void fillFormationsList() {
        FormationsAdapter formationsAdapter = new FormationsAdapter(this, R.layout.item_formation, formations);
        formationsListView.setAdapter(formationsAdapter);
        formationsAdapter.notifyDataSetChanged();
    }

    protected void setFormationItemClickListener() {
        formationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), FormationActivity.class);
                intent.putExtra(FORMATION_SELECTED, (formations.get(i).getId()));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.initial_utilisateur:
                return true;

            case R.id.charger_donnees:
                Intent intent = new Intent(FormationsActivity.this, ChargementDonneesActivity.class);
                intent.putExtra(EXTRA_INITIAL, (initialUtilisateur));
                startActivity(intent);
                return true;

            case R.id.envoyer_mail:
                Intent intentSendMail = new Intent(FormationsActivity.this, SendMailActivity.class);
                startActivity(intentSendMail);
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

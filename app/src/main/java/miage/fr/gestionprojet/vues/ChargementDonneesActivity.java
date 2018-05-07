
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
import android.widget.Button;
import android.widget.EditText;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.Action;
import miage.fr.gestionprojet.models.Domaine;
import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.models.Formation;
import miage.fr.gestionprojet.models.Mesure;
import miage.fr.gestionprojet.models.Projet;
import miage.fr.gestionprojet.models.Ressource;
import miage.fr.gestionprojet.models.SaisieCharge;
import miage.fr.gestionprojet.models.dao.DaoAction;
import miage.fr.gestionprojet.models.dao.DaoDomaine;
import miage.fr.gestionprojet.models.dao.DaoFormation;
import miage.fr.gestionprojet.models.dao.DaoProjet;
import miage.fr.gestionprojet.models.dao.DaoRessource;
import miage.fr.gestionprojet.models.dao.DaoSaisieCharge;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.outils.GoogleServices;
import miage.fr.gestionprojet.outils.Outils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ChargementDonneesActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};

    private GoogleAccountCredential mCredential;

    private ProgressDialog mProgress;

    private String userInput;

    private Context context;

    @BindView(R.id.call_api_button)
    Button callApiButton;

    @BindView(R.id.user_input)
    EditText userInputEditText;

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chargement_donnees);
        ButterKnife.bind(this);
        context = this;

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Préparation de la base de données  ...");

        // Initialize credentials and service object.
        mCredential = GoogleServices.getCredential(context, SCOPES);
    }

    @OnClick(R.id.call_api_button)
    public void loadApi() {
        this.userInput = userInputEditText.getText().toString();
        callApiButton.setEnabled(false);
        getResultsFromApi(userInput);
        callApiButton.setEnabled(true);
    }

    @OnClick(R.id.defaut_id_button)
    public void setDefaultId() {
        userInputEditText.setText(Constants.SPREAD_SHEET_DEFAULT_ID);
    }

    private void saveSheetIdInSharedPreferences(String sheetId) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE).edit();
        editor.putString(Constants.SHEET_ID_KEY, sheetId);
        editor.apply();
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi(String userInput) {
        if (userInputEditText.length() > 0) {
            if (!GoogleServices.isGooglePlayServicesAvailable(context)) {
                GoogleServices.acquireGooglePlayServices(context);
            } else if (mCredential.getSelectedAccountName() == null) {
                chooseAccount();
            } else if (!GoogleServices.isDeviceOnline(context)) {
                Toast.makeText(context, "No network connection available.", Toast.LENGTH_LONG).show();
            } else {
                saveSheetIdInSharedPreferences(userInput);
                new MakeRequestTask(mCredential, userInput).execute();
            }
        } else {
            Toast.makeText(this, "Renseignez Id du projet", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(Constants.PREF_ACCOUNT_NAME,null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi(userInput);
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

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
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
                    getResultsFromApi(userInput);
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
                        getResultsFromApi(userInput);
                    }
                }
                break;

            case Constants.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi(userInput);
                }
                break;

            default:
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
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

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        private Sheets mService = null;
        private Exception mLastError = null;
        private String userInput;

        MakeRequestTask(GoogleAccountCredential credential, String userInput) {
            mService = GoogleServices.getSheetsService(credential);
            this.userInput = userInput;
        }

        /**
         * Background task to call Google Sheets API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                getDataFromApi(userInput);
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1yw_8OO4oFYR6Q25KH0KE4LOr86UfwoNl_E6hGgq2UD4/edit
         *
         * @return List of names and majors
         * @throws IOException
         */
        private void getDataFromApi(String spreadsheetId) throws IOException, ParseException {
            String rangeProject = "Informations générales!A2:E";
            String rangeActions = "Liste des actions projet!A3:Z";
            String rangeDcConso = "DC et détails conso!A5:Z";
            String rangeSaisieCharge = "Indicateurs de saisie/charge!A5:Z";

            String rangeRessources = "Ressources!A2:Z";
            String rangeformation = "Indicateurs formation!A3:Z";
            String rangeMesure = "Mesures de saisie/charge!A2:D";
            String rangePlanFormation = "Plan de formation!A2:I";
            ValueRange reponsesmesure = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangeMesure)
                    .execute();
            ValueRange responseproject = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangeProject)
                    .execute();
            mProgress.setProgress(Outils.calculerPourcentage(0, 7));
            ValueRange responseAction = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangeActions)
                    .execute();
            mProgress.setProgress(Outils.calculerPourcentage(1, 7));
            ValueRange responseDcConso = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangeDcConso)
                    .execute();
            mProgress.setProgress(Outils.calculerPourcentage(2, 7));
            ValueRange responseressources = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangeRessources)
                    .execute();
            ValueRange responsesaisieCharge = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangeSaisieCharge)
                    .execute();
            mProgress.setProgress(Outils.calculerPourcentage(3, 7));
            ValueRange responseformation = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangeformation)
                    .execute();
            ValueRange responsePlanFormation = this.mService.spreadsheets().values()
                    .get(spreadsheetId, rangePlanFormation)
                    .execute();

            mProgress.setProgress(Outils.calculerPourcentage(4, 7));
            List<List<Object>> values = responseAction.getValues();
            List<List<Object>> valueproject = responseproject.getValues();
            List<List<Object>> valuesSaisieCharge = responsesaisieCharge.getValues();
            List<List<Object>> valuesDcConso = responseDcConso.getValues();
            List<List<Object>> valuesMEsure = reponsesmesure.getValues();
            List<List<Object>> valuePlanFormation = responsePlanFormation.getValues();
            mProgress.setProgress(Outils.calculerPourcentage(5, 7));
            List<List<Object>> valuesressources = responseressources.getValues();
            if (valueproject != null) {
                initialiserPojet(valueproject);
            }
            if (valuesressources != null) {
                initialiserRessource(reglerDonnees(valuesressources));
            }
            mProgress.setProgress(Outils.calculerPourcentage(6, 7));
            if (values != null && valuesDcConso != null) {
                initialiserAction(reglerDonnees(values), reglerDonnees(valuesDcConso));
            }

            mProgress.setProgress(Outils.calculerPourcentage(7, 7));
            List<List<Object>> valuesformation = responseformation.getValues();
            if (valuesformation != null) {
                intialiserFormation(reglerDonnees(valuesformation));
            }

            if (valuesSaisieCharge != null) {
                initialiserSaisieCharge(reglerDonnees(valuesSaisieCharge));
            }

            if (valuesMEsure != null) {
                initialiserMesures(reglerDonnees(valuesMEsure));
            }

            if(valuePlanFormation != null) {
                initialiserEtapeFormation(reglerDonnees(valuePlanFormation));
            }
        }

        /*
            homogéner les données
         */
        private List<List<Object>> reglerDonnees(List<List<Object>> values) {
            for (List row : values) {
                int index = 26 - row.size();
                for (int i = 0; i < index; i++) {
                    row.add("");
                }
            }
            return values;
        }

        private Date chaineToDate(String date) throws ParseException {
            Date resultat;
            if (date == null || date.equals("") || date.equals("NON PREVU")) {
                resultat = new SimpleDateFormat(Constants.DATE_FORMAT).parse("00/00/0000");
            } else {
                resultat = new SimpleDateFormat(Constants.DATE_FORMAT).parse(date);
            }
            return resultat;
        }

        private Boolean chaineToBoolean(String booleanString) {
            return booleanString.equals("1");
        }

        private int chaineToInteger(String integer) {
            int resultat;
            if (integer == null || integer.equals("") || integer.matches("%[a-zA-Z]%")) {
                resultat = 0;
            } else {
                resultat = Integer.valueOf(integer);
            }
            return resultat;
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

        private void initialiserRessource(List<List<Object>> values) {
            new Delete().from(Ressource.class).execute();

            Ressource resource = new Ressource();
            resource.setNom("");
            resource.setEmail("");
            resource.setEntreprise("");
            resource.setFonction("");
            resource.setInformationsDiverses("");
            resource.setInitiales("");
            resource.setPrenom("");
            resource.setTelephoneFixe("");
            resource.setTelephoneMobile("");
            resource.save();
            ActiveAndroid.beginTransaction();
            try {
                for (List row : values) {
                    resource.setNom(row.get(2).toString());
                    resource.setEmail(row.get(5).toString());
                    resource.setEntreprise(row.get(3).toString());
                    resource.setFonction(row.get(4).toString());
                    resource.setInformationsDiverses(row.get(8).toString());
                    resource.setInitiales(row.get(0).toString());
                    resource.setPrenom(row.get(1).toString());
                    resource.setTelephoneFixe(row.get(6).toString());
                    resource.setTelephoneMobile(row.get(7).toString());
                    resource.save();

                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        private void initialiserAction(List<List<Object>> values, List<List<Object>> valuesDcConso) throws ParseException {
            new Delete().from(Action.class).execute();
            new Delete().from(Domaine.class).execute();

            Projet projet = DaoProjet.loadAll().get(0);

            ActiveAndroid.beginTransaction();
            try {
                for (List<Object> row : values) {
                    saveAction(row, valuesDcConso, projet);
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        private void saveAction(List<Object> row, List<List<Object>> valuesDcConso, Projet projet) throws ParseException {
            Action action = new Action();
            action.setCode(row.get(5).toString());
            action.setOrdre(chaineToInteger(row.get(1).toString()));
            action.setTarif(row.get(2).toString());
            action.setTypeTravail(row.get(0).toString());
            action.setPhase(row.get(4).toString());
            action.setCode(row.get(5).toString());

            Domaine domaine = DaoDomaine.getDomaineByName(row.get(3).toString());
            if (domaine == null) {
                domaine = new Domaine(row.get(3).toString(), "description demo", projet);
                domaine.save();
            }
            Ressource respOuv;
            if (row.get(13).toString() == null || row.get(13).toString().length() == 0) {
                respOuv = new Ressource();
                respOuv.setInitiales("");
            }
            respOuv = DaoRessource.getRessourcesByInitial(row.get(13).toString());
            if (respOuv == null) {
                respOuv = new Ressource();
                respOuv.setInitiales(row.get(13).toString());
                respOuv.setNom("");
                respOuv.setEmail("");
                respOuv.setEntreprise("");
                respOuv.setFonction("");
                respOuv.setInformationsDiverses("");
                respOuv.setPrenom("");
                respOuv.setTelephoneFixe("");
                respOuv.setTelephoneMobile("");
                respOuv.save();
            }
            action.setRespOuv(respOuv);
            Ressource respOeu;
            if (row.get(12).toString() == null || row.get(12).toString().length() == 0) {
                respOeu = new Ressource();
                respOeu.setInitiales("");
            }
            respOeu = DaoRessource.getRessourcesByInitial(row.get(12).toString());
            if (respOeu == null) {
                respOeu = new Ressource();
                respOeu.setInitiales(row.get(12).toString());
                respOeu.setNom("");
                respOeu.setEmail("");
                respOeu.setEntreprise("");
                respOeu.setFonction("");
                respOeu.setInformationsDiverses("");
                respOeu.setPrenom("");
                respOeu.setTelephoneFixe("");
                respOeu.setTelephoneMobile("");
                respOeu.save();
            }
            action.setRespOuv(respOuv);

            action.setDomaine(domaine);

            action.setApparaitrePlanning(chaineToBoolean(row.get(6).toString()));
            action.setTypeFacturation(row.get(7).toString());
            action.setNbJoursPrevus(chaineToFloat(row.get(8).toString()));
            action.setCoutParJour(chaineToFloat(row.get(11).toString()));
            Date datedebut = chaineToDate(row.get(9).toString());
            Date datefin = chaineToDate(row.get(10).toString());
            action.setDtDeb(datedebut);
            action.setDtFinPrevue(datefin);
            action.setDtFinReelle(datefin);

            manageEcartAndResteAFaire(action, valuesDcConso);

            action.save();
        }

        private void manageEcartAndResteAFaire(Action action, List<List<Object>> valuesDcConso) {
            for (List row_Dc : valuesDcConso) {
                if (action.getCode().equals(row_Dc.get(5).toString())) {
                    if (row_Dc.get(20).toString() == null || row_Dc.get(20).toString().length() == 0) {
                        action.setEcartProjete(0);
                    } else {
                        action.setEcartProjete(chaineToFloat(row_Dc.get(20).toString()));
                    }
                    if (row_Dc.get(18).toString() == null || row_Dc.get(18).toString().length() == 0) {
                        action.setResteAFaire(0);
                    } else {
                        action.setResteAFaire(chaineToFloat(row_Dc.get(18).toString()));
                    }
                }
            }
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

        private void initialiserPojet(List<List<Object>> values) throws ParseException {
            new Delete().from(Projet.class).execute();
            Projet projet = new Projet();
            projet.setDescription("");
            projet.setNom("");
            projet.setDateDebut(chaineToDate("20/01/2017"));
            projet.setDateFinReelle(chaineToDate("20/05/2018"));
            projet.setDateFinInitiale(chaineToDate("20/05/2018"));
            ActiveAndroid.beginTransaction();
            try {
                for (List row : values) {
                    projet.setNom(row.get(0).toString());
                    projet.setDescription("Projet_Master2_MIAGE");
                    projet.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        private void initialiserSaisieCharge(List<List<Object>> values) {
            new Delete().from(SaisieCharge.class).execute();
            for (List row : values) {
                SaisieCharge saisiecharge = new SaisieCharge();
                if (!row.get(0).equals("")) {
                    ActiveAndroid.beginTransaction();
                    try {
                        saisiecharge.setNbSemainePassee(chaineToInteger(row.get(11).toString()));
                        saisiecharge.setNbSemaines(chaineToFloat(row.get(8).toString()));
                        saisiecharge.setChargeEstimeeParSemaine(chaineToFloat(row.get(9).toString()));
                        saisiecharge.setChargeRestanteEstimeeEnHeure(chaineToFloat(row.get(12).toString()));
                        saisiecharge.setChargeTotaleEstimeeEnHeure(chaineToFloat(row.get(5).toString()));
                        saisiecharge.setHeureParUnite(chaineToFloat(row.get(4).toString()));
                        saisiecharge.setNbUnitesCibles(chaineToInteger(row.get(3).toString()));
                        saisiecharge.setChargeRestanteParSemaine(chaineToFloat(row.get(15).toString()));
                        saisiecharge.setPrctChargeFaiteParSemaineParChargeEstimee(chaineToFloat(row.get(17).toString().replace('%', ' ')));
                        List<Action> listesActions = DaoAction.getActionbyCode(row.get(2).toString());
                        if (!listesActions.isEmpty()) {
                            saisiecharge.setAction(listesActions.get(0));
                        }
                        saisiecharge.save();
                        ActiveAndroid.setTransactionSuccessful();
                    } finally {
                        ActiveAndroid.endTransaction();
                    }
                }
            }
        }

        private void initialiserMesures(List<List<Object>> values) {
            new Delete().from(Mesure.class).execute();

            ActiveAndroid.beginTransaction();
            try {
                for (List row : values) {
                    Mesure mesure = new Mesure();
                    List<SaisieCharge> listsaisieCharges= new ArrayList<>();
                    List<Action> listeaction = DaoAction.getActionbyCode(row.get(0).toString());
                    if (!listeaction.isEmpty()){
                        listsaisieCharges=DaoSaisieCharge.loadSaisiesByAction(listeaction.get(0));
                    }

                    if(!listsaisieCharges.isEmpty()) {
                        SaisieCharge action =  listsaisieCharges.get(0);
                        mesure.setAction(action);
                    }
                    try {
                        mesure.setDtMesure(chaineToDate(row.get(2).toString()));
                    } catch (ParseException e) {
                        mesure.setDtMesure(null);
                    }
                    mesure.setNbUnitesMesures(chaineToInteger(row.get(1).toString()));
                    mesure.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void output) {
            mProgress.hide();
            Intent intentInitial = getIntent();
            String initialUtilisateur = intentInitial.getStringExtra(GestionDesInitialsActivity.EXTRA_INITIAL);
            Intent intent = new Intent(ChargementDonneesActivity.this,GestionDesInitialsActivity.class);
            intent.putExtra(GestionDesInitialsActivity.EXTRA_INITIAL, initialUtilisateur);
            startActivity(intent);
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
    }
}

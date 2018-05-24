package miage.fr.gestionprojet.vues;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.models.EtapeFormation;
import miage.fr.gestionprojet.models.dao.DaoEtapeFormation;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.outils.GoogleServices;
import miage.fr.gestionprojet.outils.Outils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by utilisateur on 11/04/2018.
 */

public class DetailPlanFormationActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};

    private GoogleAccountCredential mCredential;

    private ProgressDialog mProgress;

    @BindView(R.id.phase)
    TextView phase;

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.typeElement)
    TextView typeElement;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.commentaire)
    EditText commentaire;

    @BindView(R.id.responsable)
    TextView acteur;

    @BindView(R.id.checkBox)
    CheckBox actionRealise;

    @BindView(R.id.valider)
    Button valider;

    EtapeFormation dataEtape;

    private static final String SHEET_PAGE = "Plan de formation!";

    private static final String COLUMN_OBJECTIF_ATTEINT = "F";

    private static final String COLUMN_COMMENTAIRE = "G";

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_detail_etape_formation);
        ButterKnife.bind(this);
        context = this;

        // Initialize credentials and service object.
        mCredential = GoogleServices.getCredential(context, SCOPES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDetailPlanData();
        associerData();
    }

    private void getDetailPlanData() {
        Intent intent = getIntent();
        Long idDetail = intent.getLongExtra(FormationActivity.PLAN_FORMATION_SELECTED,0);
        dataEtape = DaoEtapeFormation.getEtapeFormationById(idDetail);
    }
    private void associerData() {
        phase.setText(dataEtape.getFormation().getAction().getPhase());
        name.setText(dataEtape.getFormation().getAction().getCode());
        typeElement.setText(dataEtape.getTypeElement());
        acteur.setText(dataEtape.getTypeActeur());
        description.setText(dataEtape.getDescription());
        commentaire.setText(dataEtape.getCommentaire());

        actionRealise.setChecked(dataEtape.isObjectifAtteint());
    }

    @OnClick(R.id.valider)
    public void validerSaisieEtape() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Mise à jour des données ...");
        dataEtape.setCommentaire(commentaire.getText().toString());
        dataEtape.setObjectifAtteint(actionRealise.isChecked());
        dataEtape.save();
        updateFormationsData();
    }

    private void updateFormationsData() {
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
            new DetailPlanFormationActivity.MakeRequestTask(mCredential, sheetId).execute();
        }
    }

    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(Constants.PREF_ACCOUNT_NAME,null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                updateFormationsData();
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
                    updateFormationsData();
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
                        updateFormationsData();
                    }
                }
                break;

            case Constants.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    updateFormationsData();
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
            mProgress.hide();
            finish();
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
                updateFormationsData();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        private void updateFormationsData() throws IOException {
            // petit trick java car List<String> n'hérite pas de List<Object> (nik)
            List<String> datasToUpdate = Arrays.asList(
                    Outils.booleanToInt(dataEtape.isObjectifAtteint()),
                    dataEtape.getCommentaire()
            );
            List<Object> datasToUpdateInObject = new ArrayList<>();
            datasToUpdateInObject.addAll(datasToUpdate);
            List<List<Object>> values = Arrays.asList(
                    datasToUpdateInObject
            );

            final String rangeToUpdate = SHEET_PAGE + COLUMN_OBJECTIF_ATTEINT +
                    dataEtape.getIdLigne() + ":" + COLUMN_COMMENTAIRE + dataEtape.getIdLigne();

            ValueRange body = new ValueRange()
                    .setValues(values);
            UpdateValuesResponse result =
                    this.mService.spreadsheets().values().update(sheetId, rangeToUpdate, body)
                            .setValueInputOption("RAW")
                            .execute();
            if(result.getUpdatedCells() == 2) {
                Toast.makeText(context, "Mise à jour effectuée", Toast.LENGTH_LONG).show();
            }

        }
    }
}

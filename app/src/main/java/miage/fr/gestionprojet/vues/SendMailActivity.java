package miage.fr.gestionprojet.vues;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miage.fr.gestionprojet.R;
import miage.fr.gestionprojet.outils.Constants;
import miage.fr.gestionprojet.outils.GoogleServices;
import miage.fr.gestionprojet.outils.Outils;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class SendMailActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    @BindView(R.id.send_mail)
    Button sendMail;

    @BindView(R.id.mail_destinataire)
    EditText mailDestinataire;

    private GoogleAccountCredential mCredential;

    private static final String TAG = "[SendMailActivity]";

    private static final String[] SCOPES = {DriveScopes.DRIVE};

    private ProgressDialog progressDialog;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);
        ButterKnife.bind(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
        context = this;
        mCredential = GoogleServices.getCredential(context, SCOPES);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Chargement du fichier  ...");
    }

    @OnClick(R.id.send_mail)
    public void sendMyMail(){
        getResultsFromApi();
    }

    private void getResultsFromApi() {
        String sheetId = Outils.getSheetId(this);
        if (sheetId == null) {
            Toast.makeText(this, "Pas de document chargé préalablement.", Toast.LENGTH_LONG).show();
        } else if (!GoogleServices.isGooglePlayServicesAvailable(context)) {
            GoogleServices.acquireGooglePlayServices(context);
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!GoogleServices.isDeviceOnline(context)) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential, sheetId).execute();
        }
    }

    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(Constants.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                startActivityForResult(mCredential.newChooseAccountIntent(), Constants.REQUEST_ACCOUNT_PICKER);
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
        switch(requestCode) {
            case Constants.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
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
                        getResultsFromApi();
                    }
                }
                break;

            case Constants.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;

            case Constants.REQUEST_PERMISSION_EXTERNAL_STORAGE:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
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
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int i, List<String> list) {
        // do nothing
    }

    @Override
    public void onPermissionsDenied(int i, List<String> list) {
        // do nothing
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        Drive mService = null;
        private Exception mLastError = null;
        private String sheetId;

        MakeRequestTask(GoogleAccountCredential credential, String sheetId) {
            mService = GoogleServices.getDriveService(credential);
            this.sheetId = sheetId;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            progressDialog.hide();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                Log.e(TAG, e.toString());
            }
            return new ArrayList<>();
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // Création du fichier pdf qui va recevoir le download du spreadhsheet
            File localFolder = new File(Environment.getExternalStorageDirectory(), "WebPageToPDF");
            if(!localFolder.exists()) {
                localFolder.mkdirs();
            }
            File pdfFile =  new File (localFolder, "MySamplePDFFile.pdf");
            try (FileOutputStream outputStream = new FileOutputStream(pdfFile)){
                if (pdfFile.canWrite() || pdfFile.setWritable(true)) {
                    mService.files().export(sheetId, "application/pdf").executeMediaAndDownloadTo(outputStream);
                    sendMailWithAttachment(pdfFile);
                } else {
                    // mettre un loader
                    Toast.makeText(context, "Impossible de récupérer le fichier : l'écriture sur votre appareil est impossible", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                mLastError = e;
                Log.e(TAG, e.toString());
                cancel(true);
            }
            return new ArrayList<>();
        }

        @Override
        protected void onCancelled() {
            progressDialog.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    GoogleServices.showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode(), context);
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            Constants.REQUEST_AUTHORIZATION);
                } else if (mLastError instanceof FileNotFoundException) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_PERMISSION_EXTERNAL_STORAGE);
                }
            }
        }

        private void sendMailWithAttachment(File fileToSend) {
            Uri path = Uri.fromFile(fileToSend);
            String[] to = {mailDestinataire.getText().toString().toLowerCase().trim()};
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("vnd.android.cursor.dir/email");
            intent.putExtra(Intent.EXTRA_EMAIL, to);
            intent.putExtra(Intent.EXTRA_STREAM, path);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Document Big Follow");
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }

}

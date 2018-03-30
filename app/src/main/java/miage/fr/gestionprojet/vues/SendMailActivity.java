package miage.fr.gestionprojet.vues;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miage.fr.gestionprojet.R;

public class SendMailActivity extends AppCompatActivity {
    @BindView(R.id.send_mail)
    Button sendMail;

    @BindView(R.id.mail_destinataire)
    EditText mailDestinataire;

    private GoogleAccountCredential mCredential;

    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};

    private static HttpTransport HTTP_TRANSPORT;

    private static final String SPREAD_SHEET_DEFAULT_ID = "1yw_8OO4oFYR6Q25KH0KE4LOr86UfwoNl_E6hGgq2UD4";

    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);
        ButterKnife.bind(this);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

    }

    @OnClick(R.id.send_mail)
    public void sendMyMail(){
        OutputStream myDriveFile = this.sendMail();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        String mailDestination = this.mailDestinataire.getText().toString();
        intent.putExtra(Intent.EXTRA_EMAIL, mailDestination);
        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    public Drive getDriveServices() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, this.mCredential).setApplicationName("Big Follow").build();
    }

    public OutputStream sendMail() {
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            this.getDriveServices().files().export(SPREAD_SHEET_DEFAULT_ID, "application/pdf").executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return outputStream;
    }
}

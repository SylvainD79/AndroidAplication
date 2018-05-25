package miage.fr.gestionprojet.vues;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import miage.fr.gestionprojet.R;

public class ConnexionActivity extends AppCompatActivity {

    private static final String TAG = "[ActionsActivity]";

    private static final int RC_SIGN_IN = 1234;

    @BindView(R.id.account_information_container)
    RelativeLayout accountInformationContainer;

    @BindView(R.id.connected_buttons_container)
    LinearLayout connectedButtonsContainer;

    @BindView(R.id.sign_in_button)
    Button signInButton;

    @BindView(R.id.account_user_name)
    TextView accountUserName;

    @BindView(R.id.account_email)
    TextView accountEmail;

    @BindView(R.id.account_profile_picture)
    CircleImageView accountProfilePicture;

    ProgressDialog progressDialog;

    GoogleSignInClient googleSignInClient;

    GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);
        ButterKnife.bind(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark)));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    protected void onStart() {
        super.onStart();
        account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @OnClick(R.id.sign_in_button)
    public void clickedOnSignInButton() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Chargement....");

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.sign_out_button)
    public void clickedOnSignOutButton() {
        googleSignInClient
                .signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    @OnClick(R.id.go_next_activity)
    public void clickedOnGoNextActivityButton() {
        Intent intent = new Intent(this, GestionDesInitialsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            updateUI(account);
            progressDialog.dismiss();
        } catch (ApiException e) {
            Log.w(TAG, "signInResult : failed code = " + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            accountInformationContainer.setVisibility(View.INVISIBLE);
            connectedButtonsContainer.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
        } else {
            manageAccountInformation();
            signInButton.setVisibility(View.INVISIBLE);
            accountInformationContainer.setVisibility(View.VISIBLE);
            connectedButtonsContainer.setVisibility(View.VISIBLE);
        }
    }

    private void manageAccountInformation() {
        if (account != null) {
            String accountUserNameText = "User name : " + account.getDisplayName();
            accountUserName.setText(accountUserNameText);

            String accountEmailText = "Mail : " + account.getEmail();
            accountEmail.setText(accountEmailText);

            Picasso.get().load(account.getPhotoUrl()).into(accountProfilePicture);
        }
    }
}

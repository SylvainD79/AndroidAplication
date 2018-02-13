package miage.fr.gestionprojet.vues;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import java.io.InputStream;
import java.util.ArrayList;

import miage.fr.gestionprojet.R;

public class ActivityConnexion extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<People.LoadPeopleResult> {

    private static final int SIGN_IN_CODE = 0;
    private static final int PROFILE_PIC_SIZE = 120;
    private static final String TAG = "[ActionsActivity]";

    GoogleApiClient googleApiClient;
    GoogleApiAvailability googleApiAvailability;
    SignInButton signInButton;
    private ConnectionResult connectionResult;
    private boolean isIntentInProgress;
    private boolean isSignInButtonClicked;
    private int requestCode;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSignInButtonClicked =true;
        buidNewGoogleApiClient();
        setContentView(R.layout.activity_connexion);
        //Customize sign-in button.a red button may be displayed when Google+ scopes are requested
        custimizeSignBtn();
        setBtnClickListeners();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Chargement....");
    }

    /*
    create and  initialize GoogleApiClient object to use Google Plus Api.
    While initializing the GoogleApiClient object, request the Plus.SCOPE_PLUS_LOGIN scope.
    */
    private void buidNewGoogleApiClient(){
        googleApiClient =  new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    /*
      Customize sign-in button. The sign-in button can be displayed in
      multiple sizes and color schemes. It can also be contextually
      rendered based on the requested scopes. For example. a red button may
      be displayed when Google+ scopes are requested, but a white button
      may be displayed when only basic profile is requested. Try adding the
      Plus.SCOPE_PLUS_LOGIN scope to see the  difference.
    */
    private void custimizeSignBtn(){
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(new Scope[]{Plus.SCOPE_PLUS_LOGIN});
    }

    /*
      Set on click Listeners on the sign-in sign-out and disconnect buttons
     */
    private void setBtnClickListeners(){
        // Button listeners
        signInButton.setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.frnd_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            googleApiAvailability.getErrorDialog(this, result.getErrorCode(), requestCode).show();
        } else if (!isIntentInProgress) {
            connectionResult = result;
            if (isSignInButtonClicked) {
                resolveSignInError();
            }
        }
    }

    /*
      Will receive the activity result and check which request we are responding to
     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        // Check which request we're responding to
        if (requestCode == SIGN_IN_CODE) {
            this.requestCode = requestCode;
            if (responseCode != RESULT_OK) {
                isSignInButtonClicked = false;
                progressDialog.dismiss();
            }
            isIntentInProgress = false;
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        isSignInButtonClicked = false;
        // Get user's information and set it into the layout
        getProfileInfo();
        // Update the UI after signin
        changeUI();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        googleApiClient.connect();
        changeUI();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                progressDialog.show();
                gPlusSignIn();
                break;

            case R.id.sign_out_button:
                progressDialog.show();
                gPlusSignOut();
                break;

            case R.id.disconnect_button:
                progressDialog.show();
                gPlusRevokeAccess();
                break;

            case R.id.frnd_button:
                Plus.PeopleApi.loadVisible(googleApiClient, null)
                        .setResultCallback(this);
                break;

            default:
                break;
        }
    }

    /*
      Sign-in into the Google + account
     */
    private void gPlusSignIn() {
        if (!googleApiClient.isConnecting()) {
            Log.d("user connected","connected");
            isSignInButtonClicked = true;
            progressDialog.show();
            resolveSignInError();
            Intent intent = new Intent(ActivityConnexion.this,ActivityGestionDesInitials.class);
            startActivity(intent);
        }
    }

    /*
     Revoking access from Google+ account
     */
    private void gPlusRevokeAccess() {
        if (googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(googleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.d("ActivityConnexion", "User access revoked!");
                            buidNewGoogleApiClient();
                            googleApiClient.connect();
                            changeUI();
                        }

                    });
        }
    }

    /*
      Method to resolve any signin errors
     */
    private void resolveSignInError() {
        if (connectionResult == null) {
            gPlusRevokeAccess();
        } else if (connectionResult.hasResolution()) {
            try {
                isIntentInProgress = true;
                connectionResult.startResolutionForResult(this, SIGN_IN_CODE);
                Log.d("resolve error", "sign in error resolved");
            } catch (IntentSender.SendIntentException e) {
                isIntentInProgress = false;
                googleApiClient.connect();
            }
        }
    }

    /*
      Sign-out from Google+ account
     */
    private void gPlusSignOut() {
        if (googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            googleApiClient.disconnect();
            googleApiClient.connect();
            changeUI();
        }
    }

    /*
     get user's information name, email, profile pic,Date of birth,tag line and about me
     */
    private void getProfileInfo() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
                setPersonalInfo(currentPerson);
            } else {
                Toast.makeText(getApplicationContext(),
                        "No Personal info mention", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /*
     set the User information into the views defined in the layout
     */
    private void setPersonalInfo(Person currentPerson){
        String personName = currentPerson.getDisplayName();
        String personPhotoUrl = currentPerson.getImage().getUrl();
        TextView userName = (TextView) findViewById(R.id.userName);
        String usernameText = "Name: " + personName;
        userName.setText(usernameText);
        progressDialog.dismiss();
        setProfilePic(personPhotoUrl);
    }

    /*
     By default the profile pic url gives 50x50 px image.
     If you need a bigger image we have to change the query parameter value from 50 to the size you want
    */
    private void setProfilePic(String profilePic){
        profilePic = profilePic.substring(0, profilePic.length() - 2) + PROFILE_PIC_SIZE;
        ImageView userPicture = (ImageView)findViewById(R.id.profile_pic);
        new ActivityConnexion.LoadProfilePic(userPicture).execute(profilePic);
    }

    /*
     Show and hide of the Views according to the user login status
     */
    private void changeUI() {
        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            ArrayList<String> list = new ArrayList<>();
            ArrayList<String> images= new ArrayList<>();
            try {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {
                    list.add(personBuffer.get(i).getDisplayName());
                    images.add(personBuffer.get(i).getImage().getUrl());
                }
            } finally {
                personBuffer.release();
            }
        } else {
            Log.e("circle error", "Error requesting visible circles: " + peopleData.getStatus());
        }
    }

   /*
    Perform background operation asynchronously, to load user profile picture with new dimensions from the modified url
    */
    private class LoadProfilePic extends AsyncTask<String, Void, Bitmap> {
        ImageView bitmapImage;

        public LoadProfilePic(ImageView bitmapImage) {
            this.bitmapImage = bitmapImage;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap newIcon = null;
            try {
                InputStream inputStream = new java.net.URL(url).openStream();
                newIcon = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return newIcon;
        }

        @Override
        protected void onPostExecute(Bitmap resultImage) {
            bitmapImage.setImageBitmap(resultImage);
        }
    }
}

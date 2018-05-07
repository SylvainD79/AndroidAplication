package miage.fr.gestionprojet.outils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;

import java.util.Arrays;

/**
 * Classe utilitaire pour gérer les permissions Google
 */
public class GoogleServices {

    private GoogleServices() {
        // private constructor
    }

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        return getConnectionStatusCode(context) == ConnectionResult.SUCCESS;
    }

    public static void acquireGooglePlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int connectionStatusCode = getConnectionStatusCode(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode, context);
        }
    }

    public static void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode,
                                                                     Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                (Activity) context,
                connectionStatusCode,
                Constants.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private static int getConnectionStatusCode(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        return apiAvailability.isGooglePlayServicesAvailable(context);
    }

    public static GoogleAccountCredential getCredential(Context context, String[] scopes) {
        return GoogleAccountCredential
                .usingOAuth2(context, Arrays.asList(scopes))
                .setBackOff(new ExponentialBackOff());
    }

    public static Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();
    }

    public static Sheets getSheetsService(GoogleAccountCredential credential) {
        return new Sheets.Builder(
                AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();
    }
}

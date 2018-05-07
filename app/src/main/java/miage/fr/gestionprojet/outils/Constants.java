package miage.fr.gestionprojet.outils;

public class Constants {

    private Constants() {
        // private constructor
    }

    public static final String APPLICATION_NAME = "BigFollow";

    public static final String SHARED_PREFERENCES_NAME ="BIG_FOLLOW_SHARED_PREFERENCES";

    public static final String SHEET_ID_KEY = "SHEET_ID_KEY";

    public static final String DATE_FORMAT = "dd/MM/yyyy";

    // Spreadsheet originel non modifiable
    //public static final String SPREAD_SHEET_DEFAULT_ID = "1yw_8OO4oFYR6Q25KH0KE4LOr86UfwoNl_E6hGgq2UD4";

    // Clone du spreadsheet originel, modifiable
    public static final String SPREAD_SHEET_DEFAULT_ID =  "1fKEH_jg6RuWoAP1T-QT_sVFFH9VH_qo1td4BQa5siR8";

    public static final String PREF_ACCOUNT_NAME = "accountName";

    public static final int REQUEST_ACCOUNT_PICKER = 1000;

    public static final int REQUEST_AUTHORIZATION = 1001;

    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;

    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    public static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 1004;

    public static final int ERROR_CODE_NOT_FOUND = 404;
}

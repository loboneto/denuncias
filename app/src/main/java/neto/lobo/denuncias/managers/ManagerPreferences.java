package neto.lobo.denuncias.managers;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;


public class ManagerPreferences {

    // keys de SharedPreferences
    private final static String ID     			= "id";				        // Long
    private final static String EMAIL  			= "email";			        // String
    private final static String PASS_PLAIN  	= "pass_plain";		        // String (nao codificado)
    private final static String NAME_FIRST  	= "name_first";		        // String
    private final static String NAME_LAST   	= "name_last";		        // String
    private final static String NAME_NICK   	= "name_nick";		        // String
    private final static String FLAG_LOGIN  	= "flag_login";			    // Boolean
    private final static String FLAG_SAVE_EMAIL = "flag_save_email";	    // Boolean
    private final static String FLAG_SAVE_PASS  = "flag_save_pass";		    // Boolean
    private final static String FLAG_FIRST_LOGIN = "flag_first_login";      // Boolean
    private final static String FLAG_LOGIN_MOODLE = "flag_login_moodle";    // int
    private final static String TOKEN_API     	= "token_api";			    // String
    private final static String TOKEN_GCM     	= "token_gcm";			    // String

    private final static String CONTENT_FILTER  = "content_filter";         // Boolean
    private final static String PERSON_FILTER   = "person_filter";          // Boolean
    private final static String GROUP_FILTER    = "group_filter";           // Boolean
    private final static String MISSION_FILTER  = "mission_filter";         // Boolean

    private final static String NOTIFICATION_ONOFF = "notification_onoff";   // Boolean



    private SharedPreferences sharedPreferences;
    private Editor editor;

    public ManagerPreferences(android.content.Context contextAndroid) {
        this.sharedPreferences = contextAndroid.getSharedPreferences("YouubiPref", 0); // 0 = private mode
        this.editor = sharedPreferences.edit();
    }


    //------------------------------------------------------------------------
    // ESPECIAIS
    //------------------------------------------------------------------------
    public void loginSession(long idPerson, String strEmail, String strPass, String strNameFirst, String strNameLast, boolean flagCheckEmail, boolean flagCheckPass, String tokenAPI)
    {
        // Se fizer login com ID diferente OU se for o primeiro login: limpa-se tudo antes.
        if( idPerson != this.getId() ) {
            clear();
        }

        // Se for o primeiro login (true)
        // coloca false e nao alterna mais
        if(getFlagFirstLogin()) {
           editor.putBoolean(FLAG_FIRST_LOGIN, false);
        }


        editor.putBoolean(FLAG_LOGIN, true);
        editor.putLong(ID, idPerson);
        editor.putString(EMAIL, strEmail);
        editor.putString(PASS_PLAIN, strPass);
        editor.putString(NAME_FIRST, strNameFirst);
        editor.putString(NAME_LAST, strNameLast);
        editor.putBoolean(FLAG_SAVE_EMAIL, flagCheckEmail);
        editor.putBoolean(FLAG_SAVE_PASS, flagCheckPass);
        editor.putString(TOKEN_API, tokenAPI);

        editor.commit();
    }

    public void logoutSession()
    {
        editor.putBoolean(FLAG_LOGIN, false);
        editor.putString(TOKEN_API, "0");
        editor.commit();
    }

    public void clear()
    {
        editor.clear();
        editor.commit();
    }

    //------------------------------------------------------------------------
    // GETTERS e SETTERS
    //------------------------------------------------------------------------
    public long getId() {
        return sharedPreferences.getLong(ID, -1);
    }

    public void setId(Long id) {
        editor.putLong(ID, id);
        editor.commit();
    }


    public String getEmail() {
        return sharedPreferences.getString(EMAIL, "");
    }

    public void setEmail(String email) {
        editor.putString(EMAIL, email);
        editor.commit();
    }


    public String getPassPlain() {
        return sharedPreferences.getString(PASS_PLAIN, "");
    }

    public void setPassPlain(String passPlain) {
        editor.putString(PASS_PLAIN, passPlain);
        editor.commit();
    }


    public String getNameFirst() {
        return sharedPreferences.getString(NAME_FIRST, "");
    }


    public void setNameFirst(String nameFirst) {
        editor.putString(NAME_FIRST, nameFirst);
        editor.commit();
    }

    public void setNameNick(String nameFirst) {
        editor.putString(NAME_FIRST, nameFirst);
        editor.commit();
    }

    public String getNameNick() {
        return sharedPreferences.getString(NAME_NICK, "");
    }


    public String getNameLast() {
        return sharedPreferences.getString(NAME_LAST, "");
    }

    public void setNameLast(String nameLast) {
        editor.putString(NAME_LAST, nameLast);
        editor.commit();
    }


    public boolean getFlagLogin() {
        return sharedPreferences.getBoolean(FLAG_LOGIN, false);
    }

    public void setFlagLogin(boolean flagLogin) {
        editor.putBoolean(FLAG_LOGIN, flagLogin);
        editor.commit();
    }


    public boolean getFlagFirstLogin() {
        return sharedPreferences.getBoolean(FLAG_FIRST_LOGIN, true);
    }

    public void setFlagFirstLogin(boolean flagFirstLogin) {
        editor.putBoolean(FLAG_FIRST_LOGIN, flagFirstLogin);
        editor.commit();
    }


    public boolean getFlagSaveEmail() {
        return sharedPreferences.getBoolean(FLAG_SAVE_EMAIL, false);
    }

    public void setFlagSaveEmail(boolean flagSaveEmail) {
        editor.putBoolean(FLAG_SAVE_EMAIL, flagSaveEmail);
        editor.commit();
    }


    public boolean getFlagSavePass() {
        return sharedPreferences.getBoolean(FLAG_SAVE_PASS, false);
    }

    public void setFlagSavePass(boolean flagSavePass) {
        editor.putBoolean(FLAG_SAVE_PASS, flagSavePass);
        editor.commit();
    }


    // Obtem o metodo de cadastro
    // 0 é sem o moodle
    // 1 é com o moodle
    // 2 é opcional
    public int getFlagLoginMoodle() {
        return sharedPreferences.getInt(FLAG_LOGIN_MOODLE, 0);
    }

    public void setFlagLoginMoodle(int flagLoginMoodle) {
        editor.putInt(FLAG_LOGIN_MOODLE, flagLoginMoodle);
        editor.commit();
    }


    public String getTokenAPI() {
        return sharedPreferences.getString(TOKEN_API, "0");
    }

    public void setTokenAPI(String token) {
        editor.putString(TOKEN_API, token);
        editor.commit();
    }


    public String getTokenGCM() {
        return sharedPreferences.getString(TOKEN_GCM, "");
    }

    public void setTokenGCM(String tokenGCM) {
        editor.putString(TOKEN_GCM, tokenGCM);
        editor.commit();
    }


    // Filtros
    public boolean getFlagContentFilter() {
        return sharedPreferences.getBoolean(CONTENT_FILTER, true);
    }

    public void setFlagContentFilter(boolean flagContentFilter) {
        editor.putBoolean(CONTENT_FILTER, flagContentFilter);
        editor.commit();
    }


    public boolean getFlagPersonFilter() {
        return sharedPreferences.getBoolean(PERSON_FILTER, true);
    }

    public void setFlagPersonFilter(boolean flagPersonFilter) {
        editor.putBoolean(PERSON_FILTER, flagPersonFilter);
        editor.commit();
    }


    public boolean getFlagGroupFilter() {
        return sharedPreferences.getBoolean(GROUP_FILTER, true);
    }

    public void setFlagGroupFilter(boolean flagGroupFilter) {
        editor.putBoolean(GROUP_FILTER, flagGroupFilter);
        editor.commit();
    }


    public boolean getFlagMissionFilter() {
        return sharedPreferences.getBoolean(MISSION_FILTER, true);
    }

    public void setFlagMissionFilter(boolean flagMissionFilter) {
        editor.putBoolean(MISSION_FILTER, flagMissionFilter);
        editor.commit();
    }

    public boolean getFlagNotification() {
        return sharedPreferences.getBoolean(NOTIFICATION_ONOFF, true);
    }

    public void setFlagNofitication(boolean flagNofitication) {
        editor.putBoolean(NOTIFICATION_ONOFF, flagNofitication);
        editor.commit();
    }

}

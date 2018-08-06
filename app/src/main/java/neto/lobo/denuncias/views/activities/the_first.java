package neto.lobo.denuncias.views.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.common.constants.ConstResult;
import youubi.common.to.PersonTO;
import youubi.common.to.ResultTO;

public class the_first extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_the_first);

        ManagerPreferences pref =  new ManagerPreferences(this);
        ManagerRest rest = new ManagerRest(this);

        //Toast.makeText(this, pref.getPassPlain(), Toast.LENGTH_LONG).show();

        if(pref.getEmail() != null && pref.getPassPlain() != null){

            ResultTO result  = rest.login(pref.getEmail(), pref.getPassPlain());

            if(result.getCode() == ConstResult.CODE_OK){
                Intent home = new Intent(this, HomeActivity.class);
                pref.loginSession(((PersonTO)result.getObject()).getId(), ((PersonTO)result.getObject()).getEmail(),
                        ((PersonTO)result.getObject()).getPassPlain(),
                        ((PersonTO)result.getObject()).getNameFirst(),
                        ((PersonTO)result.getObject()).getNameLast(),
                        false, true, ((PersonTO)result.getObject()).getToken());
               /* preferences.setTokenAPI(((PersonTO)result.getObject()).getToken());
                preferences.setNameFirst(((PersonTO)result.getObject()).getNameFirst());
                preferences.setId(((PersonTO)result.getObject()).getId());*/
                startActivity(home);
                startActivity(home);
                finish();
            }else{
                Intent login = new Intent(this, LoginActivity.class);
                startActivity(login);
                finish();
            }
        }
    }
}

package neto.lobo.denuncias.views.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ManagerPreferences pref =  new ManagerPreferences(this);

        if(pref.getFlagLogin()){
            Intent home = new Intent(this, HomeActivity.class);
            startActivity(home);
            finish();
        } else {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
            finish();
        }

    }


}

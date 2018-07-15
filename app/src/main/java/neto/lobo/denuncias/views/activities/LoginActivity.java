package neto.lobo.denuncias.views.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import neto.lobo.denuncias.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    public void signUp(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void signIn(View view) {
        startActivity(new Intent(this, HomeActivity.class));
    }

}

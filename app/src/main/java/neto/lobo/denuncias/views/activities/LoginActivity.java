package neto.lobo.denuncias.views.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.invoke.ConstantCallSite;
import java.util.ArrayList;
import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.common.Common;
import neto.lobo.denuncias.constants.ConstAndroid;
import neto.lobo.denuncias.managers.ManagerContexto;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.PersonTO;
import youubi.common.to.ResultTO;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ManagerPreferences managerPreferences = new ManagerPreferences(this);
        if(managerPreferences.getEmail() != null && managerPreferences.getPassPlain() !=  null){
            EditText email = findViewById(R.id.email);
            EditText senha = findViewById(R.id.senha);

            email.setText(managerPreferences.getEmail());
            senha.setText(managerPreferences.getPassPlain());
        }

    }


    public void signUp(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void signIn(View view) {
        Button signIn = findViewById(R.id.buttonSignIn);
        signIn.setEnabled(false);
        EditText email = findViewById(R.id.email);
        EditText senha = findViewById(R.id.senha);

        ManagerRest rest = new ManagerRest(LoginActivity.this);
        ResultTO result  = rest.login(email.getText().toString(), senha.getText().toString());

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Carregando...");
        progressDialog.show();


        if(result.getCode() == ConstResult.CODE_OK){
            Intent home = new Intent(this, HomeActivity.class);
            ManagerPreferences pref =  new ManagerPreferences(this);
            pref.loginSession(((PersonTO)result.getObject()).getId(), ((PersonTO)result.getObject()).getEmail(),
                    ((PersonTO)result.getObject()).getPassPlain(),
                    ((PersonTO)result.getObject()).getNameFirst(),
                    ((PersonTO)result.getObject()).getNameLast(),
                    false, true, ((PersonTO)result.getObject()).getToken());
            startActivity(home);
            startActivity(home);
            finish();
        }else{
            Toast.makeText(LoginActivity.this, result.getDescription(), Toast.LENGTH_LONG).show();
            signIn.setEnabled(true);
            progressDialog.cancel();
        }

    }

}

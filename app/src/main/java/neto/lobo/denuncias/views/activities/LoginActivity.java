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
import android.os.Looper;
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

    private ManagerRest rest;
    private DataBaseLocal dataBaseLocal;

    private Button signIn;
    private EditText email;
    private EditText senha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        rest = new ManagerRest(LoginActivity.this);
        dataBaseLocal = DataBaseLocal.getInstance(this);

        signIn = findViewById(R.id.buttonSignIn);
        email = findViewById(R.id.email);
        senha = findViewById(R.id.senha);

        ManagerPreferences managerPreferences = new ManagerPreferences(this);
        if(managerPreferences.getEmail() != null && managerPreferences.getPassPlain() !=  null){

            email.setText(managerPreferences.getEmail());
            senha.setText(managerPreferences.getPassPlain());
        }

    }


    public void signUp(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void signIn(View view) {

        signIn.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Carregando...");
        progressDialog.show();

        final String emailStr = email.getText().toString();
        final String passStr = senha.getText().toString();

        new Thread(){
            @Override
            public void run () {
                Looper.prepare();

                final ResultTO result  = rest.login(emailStr, passStr);

                if(result.getCode() == ConstResult.CODE_OK){

                    PersonTO personTO = (PersonTO) result.getObject();
                    dataBaseLocal.storePerson(personTO, personTO.getId());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            progressDialog.cancel();
                            Intent home = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(home);
                            finish();

                        }
                    });


                }else{

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(LoginActivity.this, result.getDescription(), Toast.LENGTH_LONG).show();
                            signIn.setEnabled(true);
                            progressDialog.cancel();

                        }
                    });

                }


            }
        }.start();


    }

}

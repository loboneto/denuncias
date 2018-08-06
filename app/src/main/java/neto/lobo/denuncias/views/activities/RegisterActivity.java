package neto.lobo.denuncias.views.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.constants.ConstAndroid;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContextoTO;
import youubi.common.to.CoordTO;
import youubi.common.to.PersonTO;
import youubi.common.to.PrivacyTO;
import youubi.common.to.ResultTO;
import youubi.common.tools.CalendarTools;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void register(View view){
        EditText name = findViewById(R.id.name);
        EditText email = findViewById(R.id.email);
        EditText senha = findViewById(R.id.senha);
        EditText senha2 = findViewById(R.id.senha2);

        if(name.getText().length() == 0){
            Toast.makeText(RegisterActivity.this, "Campo nome invalido.", Toast.LENGTH_LONG).show();
        }else if(email.getText().length() == 0){
            Toast.makeText(RegisterActivity.this, "Campo email invalido.", Toast.LENGTH_LONG).show();
        }else if(senha.getText().length() == 0){
            Toast.makeText(RegisterActivity.this, "Campo senha invalido.", Toast.LENGTH_LONG).show();
        }else if(senha2.getText().length() == 0){
            Toast.makeText(RegisterActivity.this, "Campo repetir senha invalido.", Toast.LENGTH_LONG).show();
        }else if(!(senha2.getText().toString().equals(senha.getText().toString()))){
            Toast.makeText(RegisterActivity.this, "As senhas não coincidem.", Toast.LENGTH_LONG).show();
        }else{

            Button register = findViewById(R.id.button);
            register.setEnabled(false);

            PersonTO user = new PersonTO();
            user.setNameFirst(name.getText().toString());
            user.setEmail(email.getText().toString());
            user.setPassPlain(senha.getText().toString());

            //valores default
            user.setNameLast("undefined");
            user.setProfession("undefined");
            user.setDateBirth("01-01-2000 00:01:01");

            PrivacyTO pri  = new PrivacyTO();
            pri.setHideBirth(0);
            pri.setHideChat(0);
            pri.setHideDistance(0);
            pri.setHideEmail(0);
            pri.setHideOnline(0);
            pri.setHidePlace(0);
            user.setPrivacyTO(pri);
            //isso deve mudar para pegar a localizão
            ContextoTO cont = new ContextoTO();
            cont.setLatitude(0);
            cont.setLongitude(0);

            ManagerRest rest = new ManagerRest(RegisterActivity.this);
            ResultTO result = rest.createPerson("", 0, user, senha.getText().toString(), null, null);

            final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Carregando...");
            progressDialog.show();

            Log.d("Create Person: ", result.toString());

            if(result.getCode() == ConstResult.CODE_OK){
                ManagerPreferences pref = new ManagerPreferences(this);
                pref.loginSession(((PersonTO)result.getObject()).getId(), ((PersonTO)result.getObject()).getEmail(),
                        ((PersonTO)result.getObject()).getPassPlain(),
                        ((PersonTO)result.getObject()).getNameFirst(),
                        ((PersonTO)result.getObject()).getNameLast(),
                        false, false, ((PersonTO)result.getObject()).getToken());
                Intent home = new Intent(this, HomeActivity.class);
                progressDialog.cancel();
                startActivity(home);
                finish();
            }else{
                Toast.makeText(RegisterActivity.this, result.getDescription(), Toast.LENGTH_LONG).show();
                register.setEnabled(true);
                progressDialog.cancel();
            }
        }
    }

    public void selectAvatar(View view){
        Intent intent = new Intent(this, ListAvatarActivity.class);
        startActivity(intent);
    }

    public void returnLogin(View view) {
        finish();
    }
}

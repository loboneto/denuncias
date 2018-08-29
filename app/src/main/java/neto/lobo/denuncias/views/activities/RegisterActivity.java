package neto.lobo.denuncias.views.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.hdodenhof.circleimageview.CircleImageView;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.constants.ConstAndroid;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.ContextoTO;
import youubi.common.to.CoordTO;
import youubi.common.to.PersonTO;
import youubi.common.to.PrivacyTO;
import youubi.common.to.ResultTO;
import youubi.common.tools.CalendarTools;

public class RegisterActivity extends AppCompatActivity {

    private ManagerPreferences managerPreferences;
    private DataBaseLocal dataBaseLocal;
    private CircleImageView photoProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        managerPreferences = new ManagerPreferences(this);
        dataBaseLocal = DataBaseLocal.getInstance(this);
        photoProfile = findViewById(R.id.photoProfile);
    }

    public void register(View view){
        EditText name = findViewById(R.id.name);
        EditText email = findViewById(R.id.email);
        final EditText senha = findViewById(R.id.senha);
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

            final Button register = findViewById(R.id.button);
            register.setEnabled(false);

            final PersonTO user = new PersonTO();
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

            user.setNickname(managerPreferences.getNameNick());

            final ManagerRest rest = new ManagerRest(RegisterActivity.this);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Carregando...");
            progressDialog.show();

            new Thread(){
                @Override
                public void run () {
                    Looper.prepare();

                    ResultTO result = rest.createPerson("", 0, user, senha.getText().toString(), null, null);


                    Log.d("Create Person: ", result.toString());

                    if(result.getCode() == ConstResult.CODE_OK){

                        PersonTO personTO = (PersonTO) result.getObject();

                        ManagerPreferences pref = new ManagerPreferences(getApplicationContext());
                        pref.loginSession(personTO.getId(), personTO.getEmail(), personTO.getPassPlain(), personTO.getNameFirst(),
                                personTO.getNameLast(),false, false, personTO.getToken());

                        dataBaseLocal.storePerson(personTO, personTO.getId());

                        Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                        progressDialog.cancel();
                        startActivity(home);
                        finish();

                    }else{
                        Toast.makeText(RegisterActivity.this, result.getDescription(), Toast.LENGTH_LONG).show();
                        register.setEnabled(true);
                        progressDialog.cancel();
                    }

                }
            }.start();


        }
    }

    public void selectAvatar(View view){
        Intent intent = new Intent(this, ListAvatarActivity.class);
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK || requestCode == 10){

            photoProfile.setImageResource(getAvatar());

        }

    }


    private int getAvatar(){
        Resources resources = this.getResources();
        return resources.getIdentifier(managerPreferences.getNameNick(), "drawable", this.getPackageName());
    }

    public void returnLogin(View view) {
        finish();
    }
}

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
import android.view.Gravity;
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

    private final int MULTIPLE_PERMISSIONS = 150;
    private List<String> listPermissionsNeeded;
    private List<String> listPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Verifica e pede permissões
        listPermissionsNeeded = new ArrayList<String>();
        listPermissionsResult = new ArrayList<String>();

//        checkPermissions();
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
            ManagerPreferences preferences =  new ManagerPreferences(this);
            preferences.setEmail(email.getText().toString());
            preferences.setTokenAPI(((PersonTO)result.getObject()).getToken());
            preferences.setNameFirst(((PersonTO)result.getObject()).getNameFirst());
            startActivity(home);
            startActivity(home);
            finish();
        }else{
            Toast.makeText(LoginActivity.this, result.getDescription(), Toast.LENGTH_LONG).show();
            signIn.setEnabled(true);
            progressDialog.cancel();
        }

    }

    private void checkPermissions() {

        // Permissões
        String[] arrayPermissions= new String[] {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION };


        int result;

        //Checa se existe alguma permissão nao concedida
        for (String permission: arrayPermissions) {

            result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }

        }

        // Se existir permissão nao concedida, faz o pedido
        if (!listPermissionsNeeded.isEmpty()) {

            ActivityCompat.requestPermissions(LoginActivity.this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);

//            TextView title = new TextView(this);
//            title.setText("Para que o app funcione corretamente é necessario fornecer algumas permissões");
//            title.setPadding(10, 10, 10, 10);
//            title.setGravity(Gravity.CENTER);
//            title.setTextSize(20);
//            title.setTextColor(Color.BLACK);
//
//            new AlertDialog.Builder(this)
//                    .setCustomTitle(title)
//                    //.setMessage(R.string.expliPermissao)
//                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                            ActivityCompat.requestPermissions(LoginActivity.this,
//                                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
//                        }
//                    })
//                    .create()
//                    .show();
        }

//        else {
//            buttonLogin.setEnabled(true);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        int permitido = PackageManager.PERMISSION_GRANTED;
        int quantidade = grantResults.length;
        int negado = 0;
        boolean concedido = true;

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:
                //Testa se todas as permissões que foram pedidas foram concedidas
                for(int i = 0; i < quantidade; i++){
                    if(grantResults[i] != permitido) {
                        concedido = false;
                        negado = i;
                        listPermissionsResult.add(permissions[i]);
                    }
                }


                // Se todas foram, informa ao usuario
                if(concedido) {

                    Toast.makeText(getApplicationContext(), "Permissões concedidas", Toast.LENGTH_LONG).show();
                    //buttonLogin.setEnabled(true);

                } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(listPermissionsResult.get(0))){//permissions[negado])) {

                    //Exibe a mensagem para o usuario alterar a permissão pelas configurações
                    //Foi marcado a mensagem "Não exibir essa mensagem novamente"

                    TextView title = new TextView(this);
                    title.setText("Você marcou alguma permissão para não ser pedida novamente, porém o app necessita dessa permissão para funcionar, você ainda pode concede-las dentro do aplicativo");
                    title.setPadding(10, 10, 10, 10);
                    title.setGravity(Gravity.CENTER);
                    title.setTextSize(18);
                    title.setTextColor(Color.BLACK);

                    new AlertDialog.Builder(this)
                            .setCustomTitle(title)
                            //.setMessage(R.string.dontAskAgain)
                            .setPositiveButton("Ir", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivity(intent);

                                    finish();
                                }
                            })
                            .setNegativeButton("Não Ir", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .create()
                            .show();


                } else {
                    //Alguma permissão nao foi concedida
                    //Faz o pedido novamente
                    //ActivityCompat.requestPermissions(LoginActivity.this, permissions, requestCode);
                    ActivityCompat.requestPermissions(LoginActivity.this,
                            listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
                }

                break;
        }

    }
}

package neto.lobo.denuncias.views.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstResult;
import youubi.common.to.PersonTO;
import youubi.common.to.ResultTO;

public class EditProfileActivity extends Activity {

    private EditText nameEdit;
    private EditText emailEdit;
    private CircleImageView photoProfileEdit;
    private Button buttonsalveEdit;
    private ManagerRest rest;
    private ManagerPreferences preferences;
    private DataBaseLocal dataBaseLocal;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEdit = findViewById(R.id.nameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        photoProfileEdit = findViewById(R.id.photoProfileEdit);
        buttonsalveEdit = findViewById(R.id.buttonsalveEdit);

        preferences = new ManagerPreferences(this);
        dataBaseLocal = DataBaseLocal.getInstance(this);
        rest = new ManagerRest(this);

        if(!preferences.getNameNick().isEmpty())
            photoProfileEdit.setImageResource(getAvatar());

        nameEdit.setText(preferences.getNameFirst());
        emailEdit.setText(preferences.getEmail());
        emailEdit.setEnabled(false);
        buttonsalveEdit.setEnabled(true);
    }

    public void selectAvatar(View view){
        Intent intent = new Intent(this, ListAvatarActivity.class);
        startActivityForResult(intent, 10);
    }

    public void back(View v){
        finish();
    }

    public void save(View view){



        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Carregando...");
        progressDialog.show();

        PersonTO person = dataBaseLocal.getPerson(preferences.getId());

        if(person == null) {
            ResultTO result = rest.getPerson(preferences.getId());

            if (result.getCode() == ConstResult.CODE_OK) {
                person = (PersonTO) result.getObject();
            } else {
                Toast.makeText(this, "Erro ao carregar dados do usuario, tente novamente.", Toast.LENGTH_LONG).show();
            }
        }

        if(person != null) {

            person.setNickname(preferences.getNameNick());
            person.setNameFirst(nameEdit.getText().toString());

            ResultTO result = rest.editPerson(person, "", null, null);

            if (result.getCode() == ConstResult.CODE_OK) {

                person = (PersonTO) result.getObject();
                preferences.setNameFirst(person.getNameFirst());
                dataBaseLocal.storePerson(person, person.getId());

                Toast.makeText(this, "Perfil salvo.", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Erro ao editar.", Toast.LENGTH_LONG).show();
            }

            progressDialog.dismiss();
        }
    }

    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10)
            photoProfileEdit.setImageResource(getAvatar());

    }

//    private int getAvatar(String avatar){
//        switch (avatar){
//            case "avatar1": return R.drawable.avatar1;
//            case "avatar2": return R.drawable.avatar2;
//            case "avatar3": return R.drawable.avatar3;
//            case "avatar4": return R.drawable.avatar4;
//            case "avatar5": return R.drawable.avatar5;
//            case "avatar6": return R.drawable.avatar6;
//            case "avatar7": return R.drawable.avatar7;
//            case "avatar8": return R.drawable.avatar8;
//            case "avatar9": return R.drawable.avatar9;
//            case "avatar10": return R.drawable.avatar10;
//            case "avatar11": return R.drawable.avatar11;
//            case "avatar12": return R.drawable.avatar12;
//        }
//        return R.drawable.avatar1;
//    }

    private int getAvatar(){
        Resources resources = this.getResources();
        return resources.getIdentifier(preferences.getNameNick(), "drawable", this.getPackageName());
    }
}

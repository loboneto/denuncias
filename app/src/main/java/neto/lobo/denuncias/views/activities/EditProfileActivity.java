package neto.lobo.denuncias.views.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import youubi.common.constants.ConstResult;
import youubi.common.to.PersonTO;
import youubi.common.to.ResultTO;

public class EditProfileActivity extends Activity {

    EditText name;
    EditText email;
    CircleImageView photoProfileEdit;
    Button buttonsalveEdit;
    ManagerPreferences pref;
    ManagerRest rest;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.nameEdit);
        email = findViewById(R.id.emailEdit);
        photoProfileEdit = findViewById(R.id.photoProfileEdit);
        buttonsalveEdit = findViewById(R.id.buttonsalveEdit);

        pref = new ManagerPreferences(this);

        photoProfileEdit.setImageDrawable(getDrawable(getAvatar(pref.getNameNick())));
        name.setText(pref.getNameFirst());
        email.setText(pref.getEmail());
        email.setEnabled(false);
        buttonsalveEdit.setEnabled(true);
    }

    public void selectAvatar(View view){
        Intent intent = new Intent(this, ListAvatarActivity.class);
        startActivityForResult(intent, 1);
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

        PersonTO person;
        rest = new ManagerRest(this);
        ResultTO result = rest.getPerson(pref.getId());
        if(result.getCode() == ConstResult.CODE_OK){
            person = (PersonTO) result.getObject();
            person.setNickname(pref.getNameNick());
            person.setNameFirst(name.getText().toString());
        }else{
            Toast.makeText(this , "Erro ao editar.", Toast.LENGTH_LONG).show();;
            return;
        }

        result = rest.editPerson(person,"",null, null);


        if(result.getCode() == ConstResult.CODE_OK){
            person = (PersonTO) result.getObject();
            pref.setNameFirst(person.getNameFirst());
        }else{
            Toast.makeText(this , "Erro ao editar.", Toast.LENGTH_LONG).show();;
            return;
        }

        progressDialog.cancel();
        finish();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ManagerPreferences preferences = new ManagerPreferences(this);

        photoProfileEdit.setImageDrawable(getDrawable(getAvatar(preferences.getNameNick())));
    }

    private int getAvatar(String avatar){
        switch (avatar){
            case "avatar1": return R.drawable.avatar1;
            case "avatar2": return R.drawable.avatar2;
            case "avatar3": return R.drawable.avatar3;
            case "avatar4": return R.drawable.avatar4;
            case "avatar5": return R.drawable.avatar5;
            case "avatar6": return R.drawable.avatar6;
            case "avatar7": return R.drawable.avatar7;
            case "avatar8": return R.drawable.avatar8;
            case "avatar9": return R.drawable.avatar9;
            case "avatar10": return R.drawable.avatar10;
            case "avatar11": return R.drawable.avatar11;
            case "avatar12": return R.drawable.avatar12;
        }
        return R.drawable.avatar1;
    }
}

package neto.lobo.denuncias.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;

public class EditProfileActivity extends Activity {

    EditText name;
    EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = findViewById(R.id.nameEdit);
        email = findViewById(R.id.emailEdit);

        ManagerPreferences pref = new ManagerPreferences(this);

        name.setText(pref.getNameFirst());
        email.setText(pref.getEmail());
        email.setEnabled(false);
    }

    public void selectAvatar(View view){
        Intent intent = new Intent(this, ListAvatarActivity.class);
        startActivity(intent);
    }

    public void back(View v){
        finish();
    }
}

package neto.lobo.denuncias.views.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;

public class ListAvatarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_avatar);
    }

    public void onClickPhoto(View view){
        String avatar = "";
        switch (view.getId()){
            case R.id.avatar1: avatar = "avatar1"; break;
            case R.id.avatar2: avatar = "avatar2"; break;
            case R.id.avatar3: avatar = "avatar3"; break;
            case R.id.avatar4: avatar = "avatar4"; break;
            case R.id.avatar5: avatar = "avatar5"; break;
            case R.id.avatar6: avatar = "avatar6"; break;
            case R.id.avatar7: avatar = "avatar7"; break;
            case R.id.avatar8: avatar = "avatar8"; break;
            case R.id.avatar9: avatar = "avatar9"; break;
            case R.id.avatar10: avatar = "avatar10"; break;
            case R.id.avatar11: avatar = "avatar11"; break;
            case R.id.avatar12: avatar = "avatar12"; break;
        }

        ManagerPreferences preferences = new ManagerPreferences(this);
        preferences.setNameNick(avatar);
        //Toast.makeText(this, ""+view.getId(), Toast.LENGTH_LONG).show();
        finish();
    }
}

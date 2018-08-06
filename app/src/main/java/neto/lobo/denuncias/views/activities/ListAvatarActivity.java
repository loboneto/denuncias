package neto.lobo.denuncias.views.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import neto.lobo.denuncias.R;

public class ListAvatarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_avatar);
    }

    public void onClickPhoto(View view){
        Toast.makeText(this, ""+view.getId(), Toast.LENGTH_LONG).show();
        finish();
    }
}

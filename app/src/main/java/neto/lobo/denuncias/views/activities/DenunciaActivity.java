package neto.lobo.denuncias.views.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import neto.lobo.denuncias.R;
import youubi.common.to.ContentTO;

public class DenunciaActivity extends AppCompatActivity {
    private ContentTO content;

    TextView name;
    TextView description;
    TextView comments;
    TextView data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);

        name = findViewById(R.id.textNamePerson);
        description = findViewById(R.id.textDescriptionDenuncia);
        comments = findViewById(R.id.textComments);
        data = findViewById(R.id.textData);

        if (getIntent().getBundleExtra("content") != null) {
            content = (ContentTO) getIntent().getBundleExtra("content").getSerializable("content");
            name.setText(content.getPersonTO().getNameFirst());
            description.setText(content.getDescription());
            data.setText(content.getDateCreation());
            if(content.getListComment() != null){
                String aux = "";
                for(int i = 0; i< content.getListComment().size(); i++){
                    aux = aux + content.getListComment().get(i) + " ";
                }
                if(aux.equals(""))
                    aux = "Ainda não ha comentários" + getResources().getString(R.string.loren) + getResources().getString(R.string.loren);
                comments.setText(aux);
            }
        } else{
            Log.d("DialogContent", "O conteúdo veio vazio");
            finish();
        }


    }

    public void back(View view){
        finish();
    }

    public void toProfile(View view){
        Intent profile = new Intent(this, ProfileActivity.class);
        Bundle bun =  new Bundle();
        bun.putSerializable("person", content.getPersonTO());
        profile.putExtra("person", bun);
        startActivity(profile);
    }
}

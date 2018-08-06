package neto.lobo.denuncias.views.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.adapters.DenunciaAdpter;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.PersonTO;
import youubi.common.to.ResultTO;

public class ProfileActivity extends AppCompatActivity {

    private PersonTO person;
    private TextView name;
    private ManagerRest rest;
    private List<ContentTO> contents;
    private ResultTO result;
    private RecyclerView recyclerView;
    private DenunciaAdpter denunciaAdpter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name = findViewById(R.id.textNameProfile);

        if(getIntent().getBundleExtra("person") != null){
            person = (PersonTO) getIntent().getBundleExtra("person").getSerializable("person");
            name.setText(person.getNameFirst());

            rest = new ManagerRest(this);

            result = rest.getListContent(person.getId());

            if(result == null){
                Toast.makeText(this, "ResultTO null", Toast.LENGTH_LONG).show();
                finish();
            }

            if(result.getCode() == ConstResult.CODE_OK){
                contents = result.getListObjectCast();

                recyclerView = findViewById(R.id.recyclerViewProfileActivity);

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(linearLayoutManager);

                denunciaAdpter = new DenunciaAdpter(contents);

                recyclerView.setAdapter(denunciaAdpter);

            }else{
                Toast.makeText(this, "Erro ao carregar conteudos.", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, "Erro ao carregar perfil.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void back(View view){
        finish();
    }
}

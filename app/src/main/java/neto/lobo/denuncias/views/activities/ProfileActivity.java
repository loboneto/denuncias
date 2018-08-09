package neto.lobo.denuncias.views.activities;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.adapters.DenunciaAdpter;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.PersonTO;
import youubi.common.to.ResultTO;

public class ProfileActivity extends AppCompatActivity {


    private TextView name;
    private CircleImageView photoProfile;
    private RecyclerView recyclerView;
    private DenunciaAdpter denunciaAdpter;


    private ManagerRest rest;
    private ResultTO result;
    private DataBaseLocal dataBaseLocal;

    private PersonTO person;
    private List<ContentTO> contents = new ArrayList<>();
    private long idPerson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        rest = new ManagerRest(this);
        dataBaseLocal = DataBaseLocal.getInstance(this);

        name = findViewById(R.id.textNameProfile);
        photoProfile = findViewById(R.id.photoProfile);

        idPerson = getIntent().getExtras().getLong("personId");

        if(idPerson != 0)
            person = dataBaseLocal.getPerson(idPerson);


        recyclerView = findViewById(R.id.recyclerViewProfileActivity);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        denunciaAdpter = new DenunciaAdpter(contents);
        recyclerView.setAdapter(denunciaAdpter);

        loadProfile();

//        if(getIntent().getBundleExtra("person") != null){
//            person = (PersonTO) getIntent().getBundleExtra("person").getSerializable("person");
//            name.setText(person.getNameFirst());
//
//
//            result = rest.getListContent(person.getId());
//
//            if(result == null){
//                Toast.makeText(this, "ResultTO null", Toast.LENGTH_LONG).show();
//                finish();
//            }
//
//            if(result.getCode() == ConstResult.CODE_OK){
//
//                contents = result.getListObjectCast();
//                denunciaAdpter.notifyDataSetChanged();
//
//            }else{
//                Toast.makeText(this, "Erro ao carregar conteudos.", Toast.LENGTH_LONG).show();
//            }
//        }else{
//            Toast.makeText(this, "Erro ao carregar perfil.", Toast.LENGTH_LONG).show();
//            finish();
//        }
    }

    public void loadProfile(){


        if(idPerson != 0){

            if(person != null){
                String fullName = person.getNameFirst() + " " + person.getNameLast();
                name.setText(fullName);
            } else {

                new Thread(){
                    @Override
                    public void run () {
                        Looper.prepare();

                        // Load Profile Person
                        ResultTO resultTOPerson = rest.getPerson(idPerson);
                        if(resultTOPerson.getCode() == ConstResult.CODE_OK){

                            PersonTO personTO = (PersonTO) resultTOPerson.getObject();
                            dataBaseLocal.storePerson(personTO, personTO.getId());

                            final String fullName = personTO.getNameFirst() + " " + personTO.getNameLast();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    name.setText(fullName);
                                    //photoProfile.setImageBitmap();
                                }
                            });

                        } else {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    // Toast de erro
                                    Toast.makeText(ProfileActivity.this, "Erro ao carregar perfil.", Toast.LENGTH_LONG).show();

                                }
                            });

                        }


                    }
                }.start();
            }

            new Thread(){
                @Override
                public void run () {
                    Looper.prepare();

                    Log.e("--->", "Indo carregar os contents");
                    // Load Content Person
                    ResultTO resultTOContent = rest.getListContent(idPerson);
                    if(resultTOContent.getCode() == ConstResult.CODE_OK){

                        Log.e("--->", "Contents Carregados");
                        List<ContentTO> listResult = resultTOContent.getListObjectCast();

                        contents.addAll(listResult);
                        dataBaseLocal.storeListContent(contents);

                        Log.e("--->", "Lista tamanho: " + contents.size());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                denunciaAdpter.notifyDataSetChanged();
                            }
                        });

                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProfileActivity.this, "Erro ao carregar conteudos.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }



                }
            }.start();

        }
    }


    public void back(View view){
        finish();
    }
}

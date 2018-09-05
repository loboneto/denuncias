package neto.lobo.denuncias.views.activities;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
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
    private ShimmerLayout shimmerLayout;


    private ManagerRest rest;
    private ManagerPreferences managerPreferences;
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
        managerPreferences = new ManagerPreferences(this);
        dataBaseLocal = DataBaseLocal.getInstance(this);

        shimmerLayout = findViewById(R.id.shimmerLayout);
        shimmerLayout.startShimmerAnimation();

        name = findViewById(R.id.textNameProfile);
        photoProfile = findViewById(R.id.photoProfile);

        idPerson = getIntent().getExtras().getLong("personId");

        if(idPerson != 0)
            person = dataBaseLocal.getPerson(idPerson);


        recyclerView = findViewById(R.id.recyclerViewProfileActivity);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        denunciaAdpter = new DenunciaAdpter(contents, this);
        recyclerView.setAdapter(denunciaAdpter);

        loadProfile();

    }

    public void loadProfile(){


        if(idPerson != 0){

            if(person != null){
                String fullName = person.getNameFirst() + " " + person.getNameLast();
                name.setText(fullName);

                if(person.getNickname() != null && !person.getNickname().isEmpty())
                    photoProfile.setImageResource(getAvatar(person.getNickname()));

            } else {

                new Thread(){
                    @Override
                    public void run () {
                        Looper.prepare();

                        // Load Profile Person
                        ResultTO resultTOPerson = rest.getPerson(idPerson);
                        if(resultTOPerson.getCode() == ConstResult.CODE_OK){

                            final PersonTO personTO = (PersonTO) resultTOPerson.getObject();
                            dataBaseLocal.storePerson(personTO, personTO.getId());

                            final String fullName = personTO.getNameFirst() + " " + personTO.getNameLast();
                            runOnUiThread(new Runnable() {
                                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void run() {
                                    name.setText(fullName);
                                    //photoProfile.setImageBitmap();
                                    if(personTO.getNickname() != null && !personTO.getNickname().isEmpty()){
                                        photoProfile.setImageResource(getAvatar(personTO.getNickname()));
                                    }
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

                                shimmerLayout.stopShimmerAnimation();
                                shimmerLayout.setVisibility(View.GONE);
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

    private int getAvatar(String nameAvatar){
        Resources resources = this.getResources();
        return resources.getIdentifier(nameAvatar, "drawable", this.getPackageName());
    }

    public void back(View view){
        finish();
    }
}

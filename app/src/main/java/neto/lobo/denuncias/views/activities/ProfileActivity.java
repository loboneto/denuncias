package neto.lobo.denuncias.views.activities;

import android.annotation.TargetApi;
import android.os.Build;
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
import io.supercharge.shimmerlayout.ShimmerLayout;
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
    private ShimmerLayout shimmerLayout;


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
        denunciaAdpter = new DenunciaAdpter(contents);
        recyclerView.setAdapter(denunciaAdpter);

        loadProfile();

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

                            final PersonTO personTO = (PersonTO) resultTOPerson.getObject();
                            dataBaseLocal.storePerson(personTO, personTO.getId());

                            final String fullName = personTO.getNameFirst() + " " + personTO.getNameLast();
                            runOnUiThread(new Runnable() {
                                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void run() {
                                    name.setText(fullName);
                                    //photoProfile.setImageBitmap();
                                    if(personTO.getNickname() != "" && personTO.getNickname() != null){
                                        photoProfile.setImageDrawable(getDrawable(getAvatar(personTO.getNickname())));
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

    public void back(View view){
        finish();
    }
}

package neto.lobo.denuncias.views.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.activities.EditProfileActivity;
import neto.lobo.denuncias.views.activities.LoginActivity;
import neto.lobo.denuncias.views.activities.ProfileActivity;
import neto.lobo.denuncias.views.adapters.DenunciaAdpter;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.PersonContentTO;
import youubi.common.to.ResultTO;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private DenunciaAdpter denunciaAdpter;
    private List<ContentTO> contents = new ArrayList<>();
    private ShimmerLayout shimmerLayout;

    private ManagerRest managerRest;
    private ManagerPreferences preferences;
    private DataBaseLocal dataBaseLocal;

    private CircleImageView photoProfile;
    private TextView name;

    private Activity activity;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerTrending, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, containerTrending, false);

        activity = getActivity();

        managerRest = new ManagerRest(activity);
        preferences = new ManagerPreferences(activity);
        dataBaseLocal = DataBaseLocal.getInstance(getContext());
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        shimmerLayout.startShimmerAnimation();



        name = view.findViewById(R.id.textName);
        photoProfile = view.findViewById(R.id.photoProfile);

        if(preferences.getNameNick() != ""){
            photoProfile.setImageResource(getAvatar());
        }

        name.setText(preferences.getNameFirst());




        recyclerView = view.findViewById(R.id.recyclerViewProfile);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        denunciaAdpter = new DenunciaAdpter(contents);
        recyclerView.setAdapter(denunciaAdpter);


        Button edit = view.findViewById(R.id.buttonEditProfile);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(activity, EditProfileActivity.class),1);
            }
        });

        Button logout = view.findViewById(R.id.buttonLogout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getContext(), EditProfileActivity.class));

                final ProgressDialog progressDialog = new ProgressDialog(activity);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Deslogando...");
                progressDialog.show();

                new Thread() {

                    @Override
                    public void run() {
                        Looper.prepare();

                        ResultTO result = managerRest.logout();

                        if(result.getCode() == ConstResult.CODE_OK){

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    progressDialog.dismiss();
                                    startActivity(new Intent(activity, LoginActivity.class));
                                    activity.finish();

                                }
                            });


                        } else {
                            Log.e("--->", "Erro logout: " + result.getDescription() + " cod: " + result.getCode());
                        }

                    }
                }.start();
            }
        });

        loadContent();

        return view;
    }

    public void loadContent(){

        new Thread(){
            @Override
            public void run () {
                Looper.prepare();

//                List<PersonContentTO> listPersonContent = dataBaseLocal.getListPersonContentByContentByRelation(preferences.getId(), ConstModel.RELATION_CREATED);
//
//                if(listPersonContent != null && !listPersonContent.isEmpty()){
//                    Log.e("--->", "PersonContent nao vazio");
//
//                    for(PersonContentTO personContentTO : listPersonContent){
//                        contents.add(personContentTO.getContentTO());
//                    }
//
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            denunciaAdpter.notifyDataSetChanged();
//                            shimmerLayout.stopShimmerAnimation();
//                            shimmerLayout.setVisibility(View.GONE);
//                        }
//                    });
//
//                } else {

                    Log.e("--->", "PersonContent vazio");

                    final ResultTO result = managerRest.getListPersonContent(preferences.getId(), 0, ConstModel.RELATION_CREATED, ConstModel.WHAT_OBJECTS_ALL);

                    if(result.getCode() == ConstResult.CODE_OK){

                        List<PersonContentTO> listPersonContent = result.getListObjectCast();
                        if(listPersonContent != null && !listPersonContent.isEmpty()) {

                            for (PersonContentTO personContentTO : listPersonContent) {
                                contents.add(personContentTO.getContentTO());
                                //dataBaseLocal.storePersonContent(personContentTO, personContentTO.getPersonTO().getId(), personContentTO.getContentTO().getId());
                            }

                            //dataBaseLocal.storeListPersonContent(listPersonContent);
                        }


                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                denunciaAdpter.notifyDataSetChanged();
                                shimmerLayout.stopShimmerAnimation();
                                shimmerLayout.setVisibility(View.GONE);
                            }
                        });

                    } else {


                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(activity, "Erro ao carregar seus conteudos, por favor tente novamente, erro: " + result.getCode(), Toast.LENGTH_LONG).show();
                                Log.e("--->", "Erro carregar conteudo: " + result.getDescription());
                                denunciaAdpter.notifyDataSetChanged();
                                shimmerLayout.stopShimmerAnimation();
                                shimmerLayout.setVisibility(View.GONE);
                            }
                        });

                    }


                //}



            }
        }.start();

    }


    private int getAvatar(){
        Resources resources = this.getResources();
        return resources.getIdentifier(preferences.getNameNick(), "drawable", activity.getPackageName());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        photoProfile.setImageResource(getAvatar());
        name.setText(preferences.getNameFirst());

    }

}
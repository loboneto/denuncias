package neto.lobo.denuncias.views.fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.activities.EditProfileActivity;
import neto.lobo.denuncias.views.activities.LoginActivity;
import neto.lobo.denuncias.views.adapters.DenunciaAdpter;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.ResultTO;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private DenunciaAdpter denunciaAdpter;
    private List<ContentTO> contents;
    private ManagerRest managerRest;
    private ResultTO result;
    private ManagerPreferences preferences;
    private CircleImageView photoProfile;
    private TextView name;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerTrending, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_profile, containerTrending, false);

        name = view.findViewById(R.id.textName);
        photoProfile = view.findViewById(R.id.photoProfile);
        ManagerPreferences preferences = new ManagerPreferences(getContext());

        if(preferences.getNameNick() != ""){
            photoProfile.setImageDrawable(getContext().getDrawable(getAvatar(preferences.getNameNick())));
        }
        name.setText(preferences.getNameFirst());

        recyclerView = view.findViewById(R.id.recyclerViewProfile);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        managerRest = new ManagerRest(getContext());
        preferences = new ManagerPreferences(getContext());

        result = managerRest.getListContent(preferences.getId());

        //Log.d("meeerda", result.toString());

        if(result.getCode() == ConstResult.CODE_OK){

            contents = result.getListObjectCast();

            denunciaAdpter = new DenunciaAdpter(contents);

            recyclerView.setAdapter(denunciaAdpter);

        }

        Button edit = view.findViewById(R.id.buttonEditProfile);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), EditProfileActivity.class),1);
            }
        });

        Button logout = view.findViewById(R.id.buttonLogout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getContext(), EditProfileActivity.class));
                result = managerRest.logout();

                if(result.getCode() == ConstResult.CODE_OK){
                    startActivity(new Intent(getContext(), LoginActivity.class));
                    getActivity().finish();
                }
            }
        });


        return view;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ManagerPreferences preferences = new ManagerPreferences(getContext());

        photoProfile.setImageDrawable(getContext().getDrawable(getAvatar(preferences.getNameNick())));
        name.setText(preferences.getNameFirst());

    }

}
package neto.lobo.denuncias.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.activities.EditProfileActivity;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerTrending, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_profile, containerTrending, false);

        TextView name = view.findViewById(R.id.textName);
        ManagerPreferences preferences = new ManagerPreferences(getContext());
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
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });


        return view;
    }

}
package neto.lobo.denuncias.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerTrending, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_profile, containerTrending, false);

        TextView name = view.findViewById(R.id.textName);
//        TextView email = view.findViewById(R.id.textEmail);
        ManagerPreferences preferences = new ManagerPreferences(getContext());
        name.setText(preferences.getNameFirst());
//        email.setText(preferences.getEmail());
        return view;
    }



}
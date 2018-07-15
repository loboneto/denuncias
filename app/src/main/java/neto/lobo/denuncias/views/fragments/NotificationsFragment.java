package neto.lobo.denuncias.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import neto.lobo.denuncias.R;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerTrending, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_notifications, containerTrending, false);

        return view;
    }

}
package neto.lobo.denuncias.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.common.constants.ConstResult;
import youubi.common.to.NotificationTO;
import youubi.common.to.ResultTO;

public class NotificationsFragment extends Fragment {

    ListView list;
    ManagerRest rest;
    ResultTO result;
    List<NotificationTO> notifs;
    ArrayAdapter<String> adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerTrending, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_notifications, containerTrending, false);

        rest = new ManagerRest(getContext());

        result = rest.getListNotification(50,1);

        if(result.getCode() == ConstResult.CODE_OK){
            notifs = result.getListObjectCast();

            String[] strings = new String[notifs.size()];


           for(int i = 0; i<notifs.size(); i++){
                strings[i] = notifs.get(i).getText();
            }



            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, strings);

            list = view.findViewById(R.id.list_notifications);
            list.setAdapter(adapter);


        }

        return view;
    }

}
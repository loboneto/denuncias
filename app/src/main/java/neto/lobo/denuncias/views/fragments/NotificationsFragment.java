package neto.lobo.denuncias.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstNotify;
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

            Log.d("----->", notifs.toString());

            //filtrar aqui
            for(int i = 0; i<notifs.size(); i++){
                if(notifs.get(i).getType() != ConstNotify.NOTIFY_TYPE_EXP_CONTENT_COMMENTED ||
                        notifs.get(i).getType() != ConstNotify.NOTIFY_TYPE_EXP_CONTENT_RATED){
                    notifs.remove(i);
                }
            }

            String[] strings = new String[notifs.size()];

           for(int i = 0; i<notifs.size(); i++){
               if(notifs.get(i).getType() == ConstNotify.NOTIFY_TYPE_EXP_CONTENT_COMMENTED)
                    strings[i] = notifs.get(i).getElementPersonName() + " comentou sua publicação " + notifs.get(i).getElementName();
               else
                    strings[i] = notifs.get(i).getElementPersonName() + " apoiou sua publicação " + notifs.get(i).getElementName();
            }



            adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, strings);

            list = view.findViewById(R.id.list_notifications);
            list.setAdapter(adapter);


        }

        return view;
    }

}
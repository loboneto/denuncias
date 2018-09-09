package neto.lobo.denuncias.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.activities.SearchActivity;
import neto.lobo.denuncias.views.adapters.DenunciaAdpter;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.ResultTO;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private DenunciaAdpter denunciaAdpter;
    private List<ContentTO> contents;
    private ManagerRest managerRest;
    private ResultTO result;
    private ManagerPreferences preferences;
    private EditText editText;
    private ShimmerLayout shimmerLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup containerTrending, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_search, containerTrending, false);

        recyclerView = view.findViewById(R.id.recyclerViewSearch);
        editText = view.findViewById(R.id.searchEditText);

        shimmerLayout = view.findViewById(R.id.shimmerLayoutSearchFragment);
        shimmerLayout.startShimmerAnimation();

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(!editText.getText().toString().equals("")){
                        Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                        searchIntent.putExtra("textSearch", editText.getText());
                        startActivity(searchIntent);
                    }else{
                        Toast.makeText(getContext(),"Campo vazio", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(linearLayoutManager);

        managerRest = new ManagerRest(getContext());
        preferences = new ManagerPreferences(getContext());

        //MELHORAR ESSE NEGOCIO AQUI
        /*result = managerRest.getListContentRanking(ConstModel.SORT_XP_DAY, 50,1);


        if(result.getCode() == ConstResult.CODE_OK){

            contents = result.getListObjectCast();

            denunciaAdpter = new DenunciaAdpter(contents, getActivity());

            recyclerView.setAdapter(denunciaAdpter);

        }*/

        return view;
    }

}
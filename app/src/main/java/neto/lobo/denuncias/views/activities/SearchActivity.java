package neto.lobo.denuncias.views.activities;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerPreferences;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.views.adapters.DenunciaAdpter;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.ResultTO;

public class SearchActivity extends AppCompatActivity {

    private String textSearch;
    private ShimmerLayout shimmerLayout;

    private ManagerRest managerRest;
    private ManagerPreferences managerPreferences;

    private RecyclerView recyclerView;
    private DenunciaAdpter denunciaAdpter;

    private List<ContentTO> contents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        managerRest = new ManagerRest(this);
        managerPreferences = new ManagerPreferences(this);

        recyclerView = findViewById(R.id.recyclerViewSearch);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            textSearch = extras.getString("textSearch");
            Log.e("--->", "Texto: " + textSearch);
        }

        shimmerLayout = findViewById(R.id.shimmerLayoutSearch);
        shimmerLayout.startShimmerAnimation();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(linearLayoutManager);
        denunciaAdpter = new DenunciaAdpter(contents, this);
        recyclerView.setAdapter(denunciaAdpter);

        search();

    }

    private void search(){

        new Thread(){
            @Override
            public void run () {
                Looper.prepare();

                ResultTO resultTO = managerRest.searchContent(textSearch, ConstModel.ELEM_POST, 30, 1);

                if(resultTO.getCode() == ConstResult.CODE_OK){

                    final List<ContentTO> search = resultTO.getListObjectCast();
                    if(search != null)
                        contents.addAll(search);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if(search == null || search.size() == 0)
                                Toast.makeText(SearchActivity.this,"Nenhuma denuncia foi encontrado.", Toast.LENGTH_LONG).show();

                            denunciaAdpter.notifyDataSetChanged();

                            shimmerLayout.stopShimmerAnimation();
                            shimmerLayout.setVisibility(View.GONE);
                        }
                    });

                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            shimmerLayout.stopShimmerAnimation();
                            shimmerLayout.setVisibility(View.GONE);

                            Toast.makeText(SearchActivity.this,"Erro ao carregar as denuncias.", Toast.LENGTH_LONG).show();
                        }
                    });

                }


            }
        }.start();

    }


    public void back(View view){
        finish();
    }
}

package neto.lobo.denuncias.views.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import io.supercharge.shimmerlayout.ShimmerLayout;
import neto.lobo.denuncias.R;

public class SearchActivity extends AppCompatActivity {
    String textSearch;
    ShimmerLayout shimmerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        shimmerLayout = findViewById(R.id.shimmerLayoutSearch);
        shimmerLayout.startShimmerAnimation();

        //infelizmente n ta dando certo!!
        textSearch = getIntent().getStringExtra("textSearch");
        Toast.makeText(this, textSearch, Toast.LENGTH_LONG).show();


    }

    public void back(View view){
        finish();
    }
}

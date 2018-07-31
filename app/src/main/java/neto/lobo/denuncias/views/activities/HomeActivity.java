package neto.lobo.denuncias.views.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

//import neto.lobo.denuncias.views.fragments.CreateFragment;
import neto.lobo.denuncias.views.fragments.MapFragment;
import neto.lobo.denuncias.views.fragments.NotificationsFragment;
import neto.lobo.denuncias.views.fragments.ProfileFragment;
import neto.lobo.denuncias.R;
import neto.lobo.denuncias.views.fragments.SearchFragment;
import neto.lobo.denuncias.helper.BottomNavigationViewHelper;
import youubi.common.to.ContentTO;

public class HomeActivity extends AppCompatActivity {

    private FloatingActionButton createContent;
//    private Toolbar toolbar;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment fragment;

            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    toolbar.setTitle("Mapa");
                    fragment = new MapFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_search:
//                    toolbar.setTitle("Buscar");
                    fragment = new SearchFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_add:
//                    toolbar.setTitle("Criar");
                    //fragment = new CreateFragment();
                    //loadFragment(fragment);
                    return true;

                case R.id.navigation_notifications:
//                    toolbar.setTitle("Notificações");
                    fragment = new NotificationsFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_profile:
//                    toolbar.setTitle("Perfil");
                    fragment = new ProfileFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        startFragment();

        createContent = findViewById(R.id.createContent);

        createContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CreateActivity.class));
            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(navigation);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerNavigation, fragment);
        transaction.commit();
    }

    private void startFragment() {
//        toolbar.setTitle("Mapa");
        Fragment fragment = new MapFragment();
        loadFragment(fragment);
    }
}

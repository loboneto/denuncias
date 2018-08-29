package neto.lobo.denuncias.views.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.location.LocationListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import neto.lobo.denuncias.R;


public class MapActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final int REQUEST_FINE_LOCATION = 0;

    private GoogleApiClient apiClient;
    private static GoogleMap map;

    private double longitude;
    private double latitude;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean isPassiveEnabled = false;
    protected LocationManager locationManager;

    // minima distancia em metros
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 metros
    // minima distancia em minutos
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minutos


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //verifica a permissão
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION );
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);


        //Location Service
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        apiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Location location = getLocation();

        if(location == null) {
            Log.e("--->", "Location null");
            alertGps();
        } else {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(latLng).zoom(16).tilt(25);
            CameraPosition cameraPos = builder.build();

            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPos);

            map.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setOnMapClickListener(this);

        UiSettings settings = map.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setAllGesturesEnabled(true);

        //Verifica a permissão
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ){
            map.setMyLocationEnabled(true);
        }

        // Mostra o botão de localização
        map.getUiSettings().setMyLocationButtonEnabled(true);

    }

    private Location getLocation(){

        Location location = null;
        Location locGps = null;
        Location locNetwork = null;
        Location locPassive = null;

        String provider = LocationManager.GPS_PROVIDER; // default


        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        // obter GPS status e network status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);


        // Verifica permissão
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            Log.e("--->", "Sem permissão de localizaçao");
        }

        // Escolhe provider habilitado
        if (isGPSEnabled) {
            provider = LocationManager.GPS_PROVIDER;

            locationManager.requestLocationUpdates(
                    provider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            if (locationManager != null)
                locGps = locationManager.getLastKnownLocation(provider);

            Log.e("--->", "GPS esta ativo: " + isGPSEnabled);

        }

        if (isNetworkEnabled) {
            provider = LocationManager.NETWORK_PROVIDER;

            locationManager.requestLocationUpdates(
                    provider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            if (locationManager != null)
                locNetwork = locationManager.getLastKnownLocation(provider);

            Log.e("--->", "Network esta ativo: " + isNetworkEnabled);
        }

        if (isPassiveEnabled) {
            locPassive = locationManager.getLastKnownLocation(provider);
            Log.e("--->", "Passivo esta ativo: " + isPassiveEnabled);
        }

        // Obtem a coordenada mais proxima da real
        if(locGps != null && locNetwork != null){

            if (locGps.getAccuracy() > locNetwork.getAccuracy())
                location = locNetwork;
            else
                location = locGps;

        } else {

            // Se o gps e/ou network sao null pega somente 1
            if (locGps != null) {
                location = locGps;
            } else if (locNetwork != null) {
                location = locNetwork;
            } else if (locPassive != null){
                location = locPassive;
            }
        }


        return location;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent intent = this.getIntent();
        intent.putExtra("latitude",latLng.latitude);
        intent.putExtra("longitude",latLng.longitude);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    public void alertGps(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS desligado");

        // Setting Dialog Message
        alertDialog.setMessage("O GPS não está ativo, é necessario ativalo para o funcionamento do app, gostaria de ativar agora?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Configurações", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }



    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


}


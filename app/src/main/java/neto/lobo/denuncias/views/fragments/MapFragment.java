package neto.lobo.denuncias.views.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.concurrent.Executor;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerContexto;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.ContextoTO;
import youubi.common.to.CoordTO;
import youubi.common.to.ResultTO;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected View mView;
    private Context mContext;
    public List<ContentTO> list;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mView = inflater.inflate(getLayoutId(), container, false);


        setMap();
        return mView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings settings = mMap.getUiSettings();
        settings.setAllGesturesEnabled(true);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-5.203776755879636, -37.3215426877141), 10));

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(false);
        }

        ManagerRest rest = new ManagerRest(getContext());

        ResultTO result = rest.getListContent(30, 1, -5.203776755879636, -37.3215426877141);

        if(result.getCode() == ConstResult.CODE_OK){
            list = result.getListObjectCast();
            Log.d("contentx: ", " " + list.size());

            for(int i = 0; i < list.size(); i++){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(list.get(i).getCoordTO().getLatitude(), list.get(i).getCoordTO().getLongitude()))
                        .title(list.get(i).getTitle())
                        .snippet(list.get(i).getPersonTO().getNameFirst())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_denuncia)));
            }
        }else{
            Toast.makeText(getContext(), "Erro ao carregar denúnicas " + result.getCode(), Toast.LENGTH_LONG);
        }

        /*mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("---->", location.toString());
                        Toast.makeText(getContext(), "localização chegou = ", Toast.LENGTH_SHORT);
                        if (location != null) {
                            // Logic to handle location object
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        Toast.makeText(getContext(), "meeeeerda ", Toast.LENGTH_SHORT);
                    }
                });*/

        //heckGPS();

        try {
            googleMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e) {
            Log.d("Itagores", "onMapReady: sem permissao de GPS");
            ManagerContexto contextoProvider = new ManagerContexto(getContext());

            CoordTO coord = contextoProvider.buildCoord();

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(coord.getLatitude(), coord.getLongitude()))
                    .title("Você.")
                    .snippet(" "));
        }

    }

    public void setMap() {
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    protected int getLayoutId() {
        return R.layout.fragment_map;
    }

}
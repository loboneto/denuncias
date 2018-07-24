package neto.lobo.denuncias.views.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.ResultTO;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    protected View mView;
    private Context mContext;
    public List list;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mView = inflater.inflate(getLayoutId(), container, false);

        ManagerRest rest = new ManagerRest(getContext());

        ResultTO result = rest.getListContent(30, 1, -5.203776755879636, -37.3215426877141);

        if(result.getCode() == ConstResult.CODE_OK){
            list = result.getListObjectCast();
            Log.d("contentx: ", " " + list.size());
        }else{
            Toast.makeText(getContext(), "Erro ao carregar denúnicas " + result.getCode(), Toast.LENGTH_LONG);
        }

        setMap();
        return mView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-5.203776755879636, -37.3215426877141), 10));

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(false);
        }

    }

    public void setMap() {
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    protected int getLayoutId() {
        return R.layout.fragment_map;
    }
}
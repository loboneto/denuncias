package neto.lobo.denuncias.views.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.managers.ManagerContexto;
import neto.lobo.denuncias.managers.ManagerRest;
import youubi.common.constants.ConstResult;
import youubi.common.to.ContentTO;
import youubi.common.to.CoordTO;
import youubi.common.to.ResultTO;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

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

       googleMap.setOnMarkerClickListener(this);

        ManagerRest rest = new ManagerRest(getContext());

        // passar minha localização
        ResultTO result = rest.getListContent(30, 1, -5.203776755879636, -37.3215426877141);

        if(result.getCode() == ConstResult.CODE_OK){
            list = result.getListObjectCast();
            Log.d("contentx: ", " " + list.size());

            for(int i = 0; i < list.size(); i++){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(list.get(i).getCoordTO().getLatitude(), list.get(i).getCoordTO().getLongitude()))
                        .title("" + list.get(i).getId())
                        .snippet(list.get(i).getPersonTO().getNameFirst())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_denuncia)));
            }
        }else{
            Toast.makeText(getContext(), "Erro ao carregar denúnicas " + result.getCode(), Toast.LENGTH_LONG);
        }



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


    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onMarkerClick(Marker marker) {

        ContentTO content = null;

        for (int i = 0; i< list.size(); i++){
            if(marker.getTitle().equals(""+list.get(i).getId())){
                content = list.get(i);
            }
        }

        if(content != null){
            //Abrir conteúdo
            DenunciaDialogFragment cmd = new DenunciaDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("content",content);
            cmd.setArguments(bundle);
            cmd.setFragmentManager(getActivity().getFragmentManager());
            cmd.show(getActivity().getSupportFragmentManager(),"contentDialog");
        }else
            Toast.makeText(getContext(), "Erro ao carregar conteudo!", Toast.LENGTH_LONG).show();

        return true;
    }
}
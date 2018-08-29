package neto.lobo.denuncias.views.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.List;

import neto.lobo.denuncias.R;
import neto.lobo.denuncias.constants.ConstAndroid;
import neto.lobo.denuncias.managers.ManagerRest;
import neto.lobo.denuncias.util.GeocodingTask;
import neto.lobo.denuncias.util.TaskInterface;
import youubi.client.help.sqlite.DataBaseLocal;
import youubi.common.constants.ConstModel;
import youubi.common.constants.ConstResult;
import youubi.common.to.CategoryTO;
import youubi.common.to.ContentTO;
import youubi.common.to.CoordTO;
import youubi.common.to.ImageOriginalTO;
import youubi.common.to.PersonContentTO;
import youubi.common.to.ResultTO;
import youubi.common.tools.CalendarTools;

public class CreateActivity extends AppCompatActivity implements LocationListener, TaskInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, PopupMenu.OnMenuItemClickListener{

    private GoogleApiClient googleApiClient;
    // minima distancia em metros
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 metros
    // minima distancia em minutos
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // 1 minutos

    // flags
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean isPassiveEnabled = false;

    private List<CategoryTO> listCategoryTO;

    private double latitude;
    private double longitude;

    private ImageView imgVFoto;
    private EditText edtDescription;
    private Button btnLocation;
    private Spinner spinner;

    private ImageOriginalTO imageOriginalTO;

    private ManagerRest managerRest;
    private DataBaseLocal dataBaseLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        imgVFoto = findViewById(R.id.imgVFoto);
        edtDescription = findViewById(R.id.edtDescription);
        btnLocation = findViewById(R.id.btnLocation);

        spinner = findViewById(R.id.categories);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        managerRest = new ManagerRest(this);
        dataBaseLocal = DataBaseLocal.getInstance(this);


        listCategoryTO = dataBaseLocal.getListCategoryAll();
        if(listCategoryTO == null || listCategoryTO.isEmpty()){

            new Thread() {
                public void run() {
                    Looper.prepare();

                    ResultTO resultTO = managerRest.getListCategory();
                    if(resultTO.getCode() == ConstResult.CODE_OK){

                        listCategoryTO = resultTO.getListObjectCast();
                        dataBaseLocal.storeListCategory(listCategoryTO);

                    }

                }
            }.start();
        }

    }

    public void back (View view){
        finish();
    }


    public void denunciar(View v){

        final ContentTO post = new ContentTO();

        //final String tittle = edTitlePost.getText().toString();
        final String description = edtDescription.getText().toString().trim();
        final int categoryIndex = spinner.getSelectedItemPosition();


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Enviando denuncia...");
        progressDialog.show();

        if(description.isEmpty() || latitude == 0 || longitude == 0){

            if(description.isEmpty())
                Toast.makeText(this, "Por favor preencha a descrição", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Não foi possivel obter a localização da ocorrencia, por favor localize a denuncia novamente.", Toast.LENGTH_SHORT).show();


            progressDialog.dismiss();

        } else {

            // Cria o objeto post
            // Informa o tipo
            post.setTypeElem(ConstModel.ELEM_POST);
            // Preenche a descrição
            post.setDescription(description);
            // Preenche o titulo
            post.setTitle("");

            post.setCategoryTO(listCategoryTO.get(categoryIndex));

            CoordTO coordTO = new CoordTO();
            coordTO.setLatitude(latitude);
            coordTO.setLongitude(longitude);
            post.setCoordTO(coordTO);
            post.setPublicCoord(ConstModel.COMMON_YES);

            // Pega a data da criação
            final Calendar calendar = Calendar.getInstance();
            String creationDate = CalendarTools.calendarToString(calendar);
            post.setDateCreation(creationDate);

            new Thread() {

                public void run() {
                    Looper.prepare();

                    final ResultTO resultCreatePost = managerRest.createContent(post, imageOriginalTO, null);

                    // Se deu tudo certo retorna o OK
                    if (resultCreatePost.getCode() == ConstResult.CODE_OK) {

                        PersonContentTO personContentTO = (PersonContentTO) resultCreatePost.getObject();

                        //Salva no BD local
                        dataBaseLocal.storePersonContent(personContentTO, personContentTO.getPersonTO().getId(), personContentTO.getContentTO().getId());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(CreateActivity.this, "Denuncia criada", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });


                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                String erro = "Erro ao criar denuncia, cod: " + resultCreatePost.getCode();
                                Toast.makeText(CreateActivity.this, erro, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }.start();
        }

    }


    public void pickPlace(View view){

        PopupMenu imgPopUp = new PopupMenu(this, view);
        imgPopUp.inflate(R.menu.location_menu);
        imgPopUp.setOnMenuItemClickListener(this);
        imgPopUp.show();


    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.current_place:

                Location location = getLocation();

                if (location != null) {
                    Log.e("--->", "Location no onMenuItemClick não e null");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.e("--->", "Lat: " + latitude + " Long: " + longitude);

                    GeocodingTask taskLocation = new GeocodingTask(this, this);
                    taskLocation.execute(latitude, longitude);

                } else {
                    alertGps();
                }

                return true;

            case R.id.open_map:
                startActivityForResult(new Intent(this, MapActivity.class), ConstAndroid.MAP_CAPTURE);
                return true;


                default:
                    return false;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {

            switch(requestCode) {

                case ConstAndroid.MAP_CAPTURE:

                    if (data != null) {

                        latitude = data.getDoubleExtra("latitude", 0);
                        longitude = data.getDoubleExtra("longitude", 0);
                        Log.e("--->", latitude + "");
                        new GeocodingTask(this, this).execute(latitude, longitude);
                    }

                    break;
            }
        }
    }


    @Override
    public void afterTask(String address, boolean pickLatLng) {

    }

    private Location getLocation(){

        Location location = null;
        Location locGps = null;
        Location locNetwork = null;
        Location locPassive = null;

        String provider = LocationManager.GPS_PROVIDER; // default


        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        // obter GPS status e network status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);


        // Verifica permissão
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            Log.e("--->", "Permissão garantida");
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



        }

        if (isNetworkEnabled) {
            provider = LocationManager.NETWORK_PROVIDER;

            locationManager.requestLocationUpdates(
                    provider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            if (locationManager != null)
                locNetwork = locationManager.getLastKnownLocation(provider);

        }

        if (isPassiveEnabled) {
            locPassive = locationManager.getLastKnownLocation(provider);
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

    public void alertGps(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS desligado");

        // Setting Dialog Message
        alertDialog.setMessage("O GPS parece não estar ativo, é necessario ativalo para o funcionamento do app, gostaria de verificar agora?");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Configurações", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
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
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }


    // Metodos vazios
    // Api de localização
    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}

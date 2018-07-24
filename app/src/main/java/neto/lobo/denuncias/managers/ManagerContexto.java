package neto.lobo.denuncias.managers;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.GregorianCalendar;

import neto.lobo.denuncias.constants.ConstAndroid;
import youubi.common.constants.ConstModel;
import youubi.common.to.ContextoTO;
import youubi.common.to.CoordTO;
import youubi.common.tools.CalendarTools;

public class ManagerContexto
{
    private Context contextAndroid;
    private LocationManager locationManager;
    private ManagerPreferences managerPreferences;
    private LocationListener locationListenerYouubi;
    private WindowManager windowManager;
    private NetworkInfo networkInfo;

    public ManagerContexto(Context contextAndroid)
    {
        this.contextAndroid = contextAndroid;
        this.managerPreferences = new ManagerPreferences(contextAndroid);
        this.locationManager = (LocationManager) contextAndroid.getSystemService(Context.LOCATION_SERVICE);
        this.locationListenerYouubi = new LocationListenerYouubi();
        windowManager = (WindowManager) contextAndroid.getSystemService(Context.WINDOW_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) contextAndroid.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
    }



    public ContextoTO buildContexto()
    {
        ContextoTO contexto = new ContextoTO();

        //---------------------------------------------------------------------
        // PERSON
        //---------------------------------------------------------------------
        contexto.setIdPerson( managerPreferences.getId() );

        //---------------------------------------------------------------------
        // GCM
        //---------------------------------------------------------------------
        contexto.setIdDeviceCM( managerPreferences.getTokenGCM() );//MyFirebaseInstanceIDService.getToken());

        //---------------------------------------------------------------------
        // CONEXAO
        //---------------------------------------------------------------------

        if( !isConnected() ) { contexto.setConnection(ConstModel.CONNECTION_NONE); }
        if( isWiFi() ) 	  { contexto.setConnection(ConstModel.CONNECTION_WIFI); }
        if( isMobileData() ) { contexto.setConnection(ConstModel.CONNECTION_MOBILE); }

        //---------------------------------------------------------------------
        // DEVICE
        //---------------------------------------------------------------------

        contexto.setDisplayHeight(this.getHeightPixels());
        contexto.setDisplayWidth(this.getWidthPixels());
        contexto.setProductModel(this.getModel());
        contexto.setSoVersion("" + this.getSoVersion());


//        if (this.isTablet()) {
//            contexto.setTypeDevice(ConstModel.DEVICE_TYPE_TABLET);
//        } else {
//            contexto.setTypeDevice(ConstModel.DEVICE_TYPE_SMARTPHONE);
//        }

        contexto.setTypeDevice(ConstModel.DEVICE_TYPE_SMARTPHONE);

        //---------------------------------------------------------------------
        // Versao
        //---------------------------------------------------------------------
        // OBS: o cliente deve sempre usar a mesma string do servidor
        contexto.setVersionYouubi( ConstAndroid.VERSION_YOUUBI_PRI );

        //---------------------------------------------------------------------
        // Coordenadas
        //---------------------------------------------------------------------
        CoordTO coordTO = buildCoord();
        contexto.setAltitude(  coordTO.getAltitude() );
        contexto.setLatitude(  coordTO.getLatitude() );
        contexto.setLongitude( coordTO.getLongitude() );

        return contexto;
    }



    public CoordTO buildCoord()
    {
        //---------------------------------------------------------------------
        // GPS
        //---------------------------------------------------------------------
        Location locationAndroid = null;
        String provider = LocationManager.GPS_PROVIDER; // default

        if(locationManager == null) {
            locationManager = (LocationManager) contextAndroid.getSystemService(Context.LOCATION_SERVICE);
        }

        // Verifica o status dos providers
        boolean gpsEnabled     = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean passiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        // Escolhe provider habilitado
        if (gpsEnabled) {
            provider = LocationManager.GPS_PROVIDER;
        }
        else {
            if (networkEnabled) {
                provider = LocationManager.NETWORK_PROVIDER;
            }
            else {
                if (passiveEnabled) {
                    provider = LocationManager.PASSIVE_PROVIDER;
                }
            }
        }

        try {
            locationManager.requestSingleUpdate(provider, locationListenerYouubi, null);
            locationAndroid = locationManager.getLastKnownLocation(provider);
        }
        catch (SecurityException se) {
        }

        // Mesmo com o provider habilitado, o location pode ser null
        if(locationAndroid == null)
        {
            try {
                // Essa situacao acontece quando o aparelho eh reiniciado (sem cache). Solucao: usar outros Providers
                locationAndroid = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (locationAndroid == null)
                {
                    locationAndroid = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (locationAndroid == null)
                    {
                        locationAndroid = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    }
                }
            }
            catch (SecurityException se) {
            }
        }

        // Pra evitar objeto NULL, inicializo na UFERSA (problema visto no Android 4.4)
        if(locationAndroid == null)
        {
            locationAndroid = new Location(provider);
            locationAndroid.setLatitude(-5.202990);
            locationAndroid.setLongitude(-37.325141);
            locationAndroid.setAltitude(0);
        }

        Long time =  locationAndroid.getTime();
        Calendar timeGps = GregorianCalendar.getInstance();
        timeGps.setTimeInMillis(time);

        CoordTO coordTO = new CoordTO();
        coordTO.setLatitude( locationAndroid.getLatitude() );
        coordTO.setLongitude( locationAndroid.getLongitude() );
        coordTO.setAltitude( locationAndroid.getAltitude() );
        coordTO.setSpeed( locationAndroid.getSpeed() );
        coordTO.setTimeGps( CalendarTools.calendarToString(timeGps) );
        coordTO.setAccuracy( locationAndroid.getAccuracy() );
        coordTO.setProvider( locationAndroid.getProvider() );

        return coordTO;
    }


    // Classe interna que implementa LocationListener
    public class LocationListenerYouubi implements LocationListener
    {
        public LocationListenerYouubi()
        {
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.d("debug", "lat="+location.getLatitude() + " lon="+location.getLongitude() + " accuracy="+location.getAccuracy() );
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    }



    public int getHeightPixels()
    {
        DisplayMetrics display = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(display);
        return display.heightPixels;
    }

    public int getWidthPixels(){
        DisplayMetrics display = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(display);
        return display.widthPixels;
    }

    public String getProducId(){
        return Build.ID;
    }

    public String getModel(){
        return Build.MODEL;
    }

    public String getBrand(){
        return Build.BRAND;
    }

    public String getProducSerial(){
        return Build.SERIAL;
    }

    public String getProductFormalName(){
        return Build.PRODUCT;
    }


    /**
     * Verifica as polegadas da diagonal para saber se eh um Tablet.
     */
    public boolean isTablet() {
        boolean result = false;

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        float yPol = metrics.heightPixels / metrics.ydpi;
        float xPol = metrics.widthPixels / metrics.xdpi;
        double diagonalPol = Math.sqrt(xPol * xPol + yPol * yPol);
        Log.d("debug", "diagonalPol = " + diagonalPol);

        if (diagonalPol >= 6.5) {
            result = true;
        } else {
            result = false;
        }

        return result;
    }

    /**
     * Verifica a versao do Android.
     */
    public int getSoVersion() {
        return Build.VERSION.SDK_INT;
    }



    /**
     * Verifica se dispositivo esta conectado a Internet.
     */
    public boolean isConnected() {
        boolean result = false;

        if (networkInfo != null) {
            result = networkInfo.isConnectedOrConnecting();
        }

        return result;
    }


    /**
     * Verifica se dispositivo esta conectado a Internet atraves da Wifi.
     */
    public boolean isWiFi() {
        boolean result = false;

        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                result = true;
            }
        }
        return result;
    }


    /**
     * Verifica se dispositivo esta conectado a Internet atraves da rede movel.
     */
    public boolean isMobileData() {
        boolean result = false;

        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                result = true;
            }
        }
        return result;
    }


}


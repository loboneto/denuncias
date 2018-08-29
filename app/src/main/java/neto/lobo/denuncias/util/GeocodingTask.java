package neto.lobo.denuncias.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;


public class GeocodingTask extends AsyncTask<Double, Void, String> {

    private ProgressDialog progressDialog;
    private Context context;
    private boolean pickLatLng;
    private TaskInterface taskInterface;
    private double latitude;
    private double longitude;

    public GeocodingTask(Context context, TaskInterface taskInterface){

        this.context = context;
        this.taskInterface = taskInterface;

    }

    @Override
    protected void onPreExecute() {

        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Buscando localização...");
        progressDialog.show();

    }

    @Override
    protected String doInBackground(Double... params) {

        this.latitude = params[0];
        this.longitude = params[1];


        try {

            return GeocoderHelper.doReverseGeocoding(context, latitude, longitude);

        } catch (final IOException e) {

            Log.e("--->", "Erro no doInBackGround GeocodingTask: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String address) {

        pickLatLng = true;

        if (address != null) {


            if(address.trim().isEmpty()){
                address = "Latitude: " + latitude + ", Longitude: " + longitude;
            }


        } else {
            address = "Latitude: " + latitude + ", Longitude: " + longitude;//context.getString(R.string.locationNull);
        }



        taskInterface.afterTask(address, pickLatLng);

        progressDialog.dismiss();
    }
}
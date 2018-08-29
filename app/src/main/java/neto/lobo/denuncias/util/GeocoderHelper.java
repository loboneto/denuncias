package neto.lobo.denuncias.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocoderHelper {

    public static boolean isGeocoderAvailable() {
        return Geocoder.isPresent();
    }

    public static double[] doGeocoding(Context context, String addressStr) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList = geocoder.getFromLocationName(addressStr, 1);

        if (addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);
            return new double[]{address.getLatitude(), address.getLongitude()};

        } else {
            return null;
        }
    }

    public static String doReverseGeocoding(Context context, double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 5);


        if (addressList != null && addressList.size() > 0) {

            Address address = addressList.get(0);
            String s = address.getAddressLine(0);

            return s.trim();

        } else {

            return null;
        }
    }
}
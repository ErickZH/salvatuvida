package itch.salvatuvida;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

// Implementamos Location Listener
public class ServicioGPS implements LocationListener {

    private final Context context;
    double latitud;
    double longitud;
    Location location;
    boolean gpsActivo;
    LocationManager locationManager;
    String ubicacion;

    // Constructor
    public ServicioGPS(Context c) {
        super();
        this.context = c;
        get_ubicacion();
    }

    public String get_ubicacion() {
        try {
            Log.e("EZH", "Servicio GPS - 1");
            locationManager = (LocationManager) this.context.getSystemService(context.LOCATION_SERVICE);
            Log.e("EZH", "Servicio GPS - 2");
            gpsActivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.e("EZH", "Servicio GPS - 3");
            if (gpsActivo)// gps activado
            {
                Log.e("EZH", "Servicio GPS - 4");
                Log.e("EZH", "Servicio GPS - 4.5");
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, this);

                location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                Log.e("EZH","Servicio GPS - 5");
                ubicacion = "Estoy en las cordenadas: Long => "+location.getLongitude()+", Lat => "+location.getLatitude();
                Log.e("EZH","Servicio GPS - 6");
            }
            else
            {
                // activar gps
                ubicacion = "Activar GPS";
            }

        } catch (Exception e)
        {
            Log.e("EZH","Error en get_ubicacion => "+e.getMessage());
            ubicacion = "Necesito ayuda !!";
        }


        return ubicacion;
    }


    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

}

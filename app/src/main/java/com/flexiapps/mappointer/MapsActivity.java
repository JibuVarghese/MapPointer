package com.flexiapps.mappointer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION =99 ;
    private GoogleMap mMap;
    Double lat = null, lng = null;
    String geoname = null;
    Button gotopin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        gotopin = (Button) findViewById(R.id.gotopin);
        Intent intnt = getIntent();
        Bundle bundle = intnt.getExtras();
        lat = Double.valueOf(bundle.getString("lat"));
        lng = Double.valueOf(bundle.getString("lng"));
        geoname = bundle.getString("geoname");
        System.out.println("lat " + lat + " long " + lng + " name " + geoname);

        gotopin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onMapReady(mMap);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    String.valueOf(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}))) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.


                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
//            }
        }else
        {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            // Add a marker in Sydney and move the camera
//        LatLng latlng = new LatLng(lat, lng);
//        mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in "+geoname));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
////        mMap.animateCamera(CameraUpdateFactory.zoomBy(4));
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(latlng)      // Sets the center of the map to Mountain View
//                .zoom(17)                   // Sets the zoom
//                .bearing(90)                // Sets the orientation of the camera to east
//                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
//                .build();                   // Creates a CameraPosition from the builder
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            LatLng latlng = new LatLng(lat, lng);
            // Instantiating MarkerOptions class
            MarkerOptions options = new MarkerOptions();

            // Setting position for the MarkerOptions
            options.position(latlng);

            // Setting title for the MarkerOptions
            options.title("Position in "+geoname);

            // Setting snippet for the MarkerOptions
            options.snippet("Latitude:"+lat+",Longitude:"+lng);

            // Adding Marker on the Google Map
            googleMap.addMarker(options);
//        mMap.addMarker(new MarkerOptions().position(latlng));
//        mMap.addMarker(new MarkerOptions().position(latlng).title("Marker in "+geoname));
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latlng, 8);
            mMap.animateCamera(yourLocation);

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    System.out.println("clicked");
                    startNavigation(lat,lng);
                }
            });

        }
    }

    public void startNavigation(Double lat, Double lng)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+lat+","+lng));
        this.startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                {

                } else {
                    Toast.makeText(this,  "Read/Write External Storage Not Granted", Toast.LENGTH_LONG).show();
                }
        }
    }
}

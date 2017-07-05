package com.flexiapps.mappointer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class JDataActivity extends AppCompatActivity {

    private static final int CODE_OK = 0;
    private static final int CODE_ERROR = 1;
    private static final String TAG = "JSON";

    private static String COORD_N = "44.1";
    private static String COORD_S = "-9.9";
    private static String COORD_W = "-22.4";
    private static String COORD_E = "55.5";
    private static String UNAME = "DEMO";

    private ProgressDialog dialog;

    private GeonameList cities;
    ListView cities_list;
    private CustomListAdapter customListAdapter;

    private List<String> toponymName;
    private List<String> names;
    private List<String> lat;
    private List<String> lng;
    private List<String> countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jdata);

        cities_list = (ListView) findViewById(R.id.cities_list);

        Intent intnt=getIntent();
        Bundle bundle=intnt.getExtras();
        UNAME =bundle.getString("UNAME");
        COORD_N =bundle.getString("COORD_N");
        COORD_S =bundle.getString("COORD_S");
        COORD_E =bundle.getString("COORD_E");
        COORD_W =bundle.getString("COORD_W");
        System.out.println("UNMAE "+UNAME+" "+COORD_N+" "+COORD_E+" "+COORD_S+" "+COORD_W);

        callService();

        cities_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               FlashMessage("Clicked row \n" + position+"Lat "+cities.getGeonames().get(position).getLat()+"\nLong "+cities.getGeonames().get(position).getLng());

                Intent intnt = new Intent(getBaseContext(),MapsActivity.class);
                Bundle extras = new Bundle();
                extras.putString("lat", String.valueOf(cities.getGeonames().get(position).getLat()));
                extras.putString("lng", String.valueOf(cities.getGeonames().get(position).getLng()));
                extras.putString("geoname", cities.getGeonames().get(position).getToponymName());
                intnt.putExtras(extras);
                startActivity(intnt);
            }
        });
    }

    // This handler will be notified when the service has responded.
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            dialog.dismiss();
            if (msg.what == CODE_ERROR) {
                Toast.makeText(JDataActivity.this, "Service error.", Toast.LENGTH_SHORT).show();
            }
            else if (cities != null && cities.getGeonames() != null) {
                Log.i(TAG, "Cities found: " + cities.getGeonames().size());
            setlistviewadapter();
            }
        }
    };

    private void setlistviewadapter() {

        if (cities != null && cities.getGeonames() != null) {

            toponymName = new LinkedList();
            names = new LinkedList();
            lat = new LinkedList();
            lng = new LinkedList();
            countryCode = new LinkedList();
            for (int i=0; i<cities.getGeonames().size();i++) {

                toponymName.add(cities.getGeonames().get(i).getToponymName());
                names.add(cities.getGeonames().get(i).getName());
                lat.add(cities.getGeonames().get(i).getLat().toString());
                lng.add(cities.getGeonames().get(i).getLng().toString());
                countryCode.add(cities.getGeonames().get(i).getCountrycode());

            }

            System.out.println("List view : "+toponymName);
            customListAdapter = new CustomListAdapter(JDataActivity.this, R.layout.list_item_view, toponymName,
                    names,lat,lng,countryCode);
            cities_list.setAdapter(customListAdapter);
        }
    }

    private void callService() {

        // Show a loading dialog.
        dialog = ProgressDialog.show(this, "Loading", "Calling GeoNames web service...", true, false);

        // Create the thread that calls the webservice.
        Thread loader = new Thread() {
            public void run() {

                // init stuff.
                Looper.prepare();
                cities = new GeonameList();
                boolean error = false;

                // build the webservice URL from parameters.
                String wsUrl = "http://api.geonames.org/citiesJSON?lang=en&username="+UNAME;
                wsUrl += "&north="+COORD_N;
                wsUrl += "&south="+COORD_S;
                wsUrl += "&east="+COORD_E;
                wsUrl += "&west="+COORD_W;

                String wsResponse = "";

                try {
                    // call the service via HTTP.
                    wsResponse = readStringFromUrl(wsUrl);

                    // deserialize the JSON response to the cities objects.
                    cities = new Gson().fromJson(wsResponse, GeonameList.class);
                }
                catch (IOException e) {
                    // IO exception
                    Log.e(TAG, e.getMessage(), e);
                    error = true;
                }
                catch (IllegalStateException ise) {
                    // Illegal state: maybe the service returned an empty string.
                    Log.e(TAG, ise.getMessage(), ise);
                    error = true;
                }
                catch (JsonSyntaxException jse) {
                    // JSON syntax is wrong. This could be quite bad.
                    Log.e(TAG, jse.getMessage(), jse);
                    error = true;
                }

                if (error) {
                    // error: notify the error to the handler.
                    handler.sendEmptyMessage(CODE_ERROR);
                }
                else {
                    // everything ok: tell the handler to show cities list.
                    handler.sendEmptyMessage(CODE_OK);
                }
            }
        };

        // start the thread.
        loader.start();
    }

    private String readStringFromUrl(String fileURL) throws IOException {

        InputStream is = null;
        BufferedInputStream bis = null;
        ByteArrayBuffer bufH = new ByteArrayBuffer(512);
        byte[] bufL = new byte[512];

        if (checkConnectivity()) {

            try {
                URL url = new URL(fileURL);

                long startTime = System.currentTimeMillis();

                Log.d(TAG, "Started download from URL " + url);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                is = new BufferedInputStream(urlConnection.getInputStream());

                int read;
                do {
                    read = is.read(bufL, 0, bufL.length);
                    if (read > 0) bufH.append(bufL, 0, read);
                } while (read >= 0);

                Log.d(TAG, "completed download in " + ((System.currentTimeMillis() - startTime) / 1000) + " sec");
                Log.d(TAG, "downloaded " + bufH.length() + " byte");

                String text = new String(bufH.toByteArray()).trim();
                return text;
            }
            catch (SocketTimeoutException ste) {
                throw ste;
            }
            catch (IOException ioe) {
                throw ioe;
            }
            finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                }
                catch (IOException e) {	}
            }
        }
        else {
            throw new IOException("Download error: no connection");
        }
    }

    private boolean checkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo infoMobi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo infoWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State connectionMobi = NetworkInfo.State.DISCONNECTED;
        NetworkInfo.State connectionWifi = NetworkInfo.State.DISCONNECTED;
        if (infoMobi != null)
            connectionMobi = infoMobi.getState();
        if (infoWifi != null)
            connectionWifi = infoWifi.getState();
        return (connectionMobi == NetworkInfo.State.CONNECTED) || (connectionWifi == NetworkInfo.State.CONNECTED);
    }

    private void FlashMessage(final String error)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(JDataActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
}

package com.flexiapps.mappointer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonSyntaxException;

import org.apache.http.util.ByteArrayBuffer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlDataActivity extends AppCompatActivity {

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
    private String geoURL;
    private TextView txtVwgeo;
    private boolean debug=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_data);

        cities_list = (ListView) findViewById(R.id.cities_list);
        txtVwgeo = (TextView) findViewById(R.id.txtVwgeo);
        Intent intnt=getIntent();
        Bundle bundle=intnt.getExtras();
        UNAME =bundle.getString("UNAME");
        COORD_N =bundle.getString("COORD_N");
        COORD_S =bundle.getString("COORD_S");
        COORD_E =bundle.getString("COORD_E");
        COORD_W =bundle.getString("COORD_W");
        System.out.println("UNMAE "+UNAME+" "+COORD_N+" "+COORD_E+" "+COORD_S+" "+COORD_W);

        txtVwgeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{

                    // CALL GetText method to make post method call
//                    GetText();
                    callService();
                }
                catch(Exception ex)
                {

                }
            }
        });

        cities_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FlashMessage("Clicked row \n" + position+"Lat "+lat.get(position)+"\nLong "+lng.get(position));

                Intent intnt = new Intent(getBaseContext(),MapsActivity.class);
                Bundle extras = new Bundle();
                extras.putString("lat", String.valueOf(lat.get(position)));
                extras.putString("lng", String.valueOf(lng.get(position)));
                extras.putString("geoname", names.get(position));
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
                Toast.makeText(XmlDataActivity.this, "Service error.", Toast.LENGTH_SHORT).show();
            }
            else if (toponymName != null && names != null && lat != null && lng != null && countryCode != null) {
                setlistviewadapter();
            }
        }
    };

    private void setlistviewadapter() {

        if (toponymName != null && names != null && lat != null && lng != null && countryCode != null) {

            customListAdapter = new CustomListAdapter(XmlDataActivity.this, R.layout.list_item_view, toponymName,
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
//                cities = new GeonameList();
                boolean error = false;

                // build the webservice URL from parameters.
                String wsUrl = "http://api.geonames.org/cities?lang=en&username="+UNAME;
                wsUrl += "&north="+COORD_N;
                wsUrl += "&south="+COORD_S;
                wsUrl += "&east="+COORD_E;
                wsUrl += "&west="+COORD_W;

                String wsResponse = "";

                try {
                    // call the service via HTTP.
                    wsResponse = readStringFromUrl(wsUrl);
                    System.out.println("Response : "+wsResponse);

                    // deserialize the JSON response to the cities objects.
//                    cities = new Gson().fromJson(wsResponse, GeonameList.class);
                    if (wsResponse != null)
                    {
                        DeserializeXml(wsResponse);
                    }
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

    public void DeserializeXml(String xmlFile)
    {
        try
        {
            toponymName = new LinkedList();
            names = new LinkedList();
            lat = new LinkedList();
            lng = new LinkedList();
            countryCode = new LinkedList();
            DocumentBuilderFactory docFactory=DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder=docFactory.newDocumentBuilder();
            InputSource is=new InputSource();
            is.setCharacterStream(new StringReader(xmlFile));
            Document doc=docBuilder.parse(is);
            NodeList studentList=doc.getElementsByTagName("geoname");
            for(int i=0;i<studentList.getLength();i++)
            {
                Node eachNode=studentList.item(i);
                if(eachNode.getNodeType()==Node.ELEMENT_NODE)
                {

                    Element eachElement=(Element) eachNode;
                    toponymName.add(new String((eachElement).getElementsByTagName("toponymName").item(0).getTextContent()));
                    names.add(new String((eachElement).getElementsByTagName("name").item(0).getTextContent()));
                    lat.add(new String((eachElement).getElementsByTagName("lat").item(0).getTextContent()));
                    lng.add(new String((eachElement).getElementsByTagName("lng").item(0).getTextContent()));
                    countryCode.add(new String((eachElement).getElementsByTagName("countryCode").item(0).getTextContent()));

                }

            }
        }
        catch (ParserConfigurationException pce)
        {
            pce.printStackTrace();
            if (debug)
            {
                FlashMessage("Error (dom parsing) - "+pce.toString());
            }

        } catch (IOException ioe)
        {
            ioe.printStackTrace();
            if (debug)
            {
                FlashMessage("Error (dom parsing) - "+ioe.toString());
            }

        } catch (SAXException sae)
        {
            sae.printStackTrace();
            if (debug)
            {
                FlashMessage("Error (dom parsing) - "+sae.toString());
            }
        }
    }

    public  void  GetText()  throws  UnsupportedEncodingException
    {

        // Create data variable for sent values to server (adding parameters)

        String data = URLEncoder.encode("north", "UTF-8")
                + "=" + URLEncoder.encode("41.2", "UTF-8");

        data += "&" + URLEncoder.encode("south", "UTF-8") + "="
                + URLEncoder.encode("-21.2", "UTF-8");

        data += "&" + URLEncoder.encode("east", "UTF-8")
                + "=" + URLEncoder.encode("-9.9", "UTF-8");

        data += "&" + URLEncoder.encode("west", "UTF-8")
                + "=" + URLEncoder.encode("55.2", "UTF-8");

        data += "&" + URLEncoder.encode("username", "UTF-8")
                + "=" + URLEncoder.encode("jibzvarghese", "UTF-8");

        String response = "";
        BufferedReader reader=null;

        // Send data
        try
        {
            geoURL = "http://api.geonames.org/cities";
            // Defined URL  where to send data
            URL url = new URL(geoURL);

            // Send POST data request

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write( data );
            wr.flush();

            // Get the server response

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line + "\n");
            }


            response = sb.toString();
        }
        catch(Exception ex)
        {

        }
        finally
        {
            try
            {

                reader.close();
            }

            catch(Exception ex) {}
        }

        // Show response on activity
        FlashMessage("RRRRRRR " +response  );
        System.out.println(response);

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
                        Toast.makeText(XmlDataActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
}

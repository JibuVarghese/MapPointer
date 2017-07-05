package com.flexiapps.mappointer;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText username, north, south, east, west;
    String UNAME,COORD_N,COORD_S,COORD_E,COORD_W;
    Button jsonBtn, xmlBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = (EditText) findViewById(R.id.username);
        north = (EditText) findViewById(R.id.north);
        south = (EditText) findViewById(R.id.south);
        east = (EditText) findViewById(R.id.east);
        west = (EditText) findViewById(R.id.west);
        jsonBtn = (Button) findViewById(R.id.jsonBtn);
        xmlBtn = (Button) findViewById(R.id.xmlBtn);

        jsonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UNAME = username.getText().toString();
                COORD_N = north.getText().toString();
                COORD_S = south.getText().toString();
                COORD_E = east.getText().toString();
                COORD_W = west.getText().toString();
                if (UNAME.equals(""))
                {
                    COORD_N = "44.1";
                    COORD_S = "-9.9";
                    COORD_W = "-22.4";
                    COORD_E = "55.5";
                    UNAME = "jibzvarghese";

                    System.out.println("Main UNMAE "+UNAME+" "+COORD_N+" "+COORD_E+" "+COORD_S+" "+COORD_W);

                    Intent intnt = new Intent(getBaseContext(),JDataActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("UNAME", UNAME);
                    extras.putString("COORD_N", COORD_N);
                    extras.putString("COORD_S", COORD_S);
                    extras.putString("COORD_E", COORD_E);
                    extras.putString("COORD_W", COORD_W);
                    intnt.putExtras(extras);
                    startActivity(intnt);
                }



            }
        });

        xmlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FlashMessage("YET TO IMPLEMENT");
                UNAME = username.getText().toString();
                COORD_N = north.getText().toString();
                COORD_S = south.getText().toString();
                COORD_E = east.getText().toString();
                COORD_W = west.getText().toString();
                System.out.println("Main UNMAE "+UNAME+" "+COORD_N+" "+COORD_E+" "+COORD_S+" "+COORD_W);

                if (UNAME.equals(""))
                {
                    COORD_N = "44.1";
                    COORD_S = "-9.9";
                    COORD_W = "-22.4";
                    COORD_E = "55.5";
                    UNAME = "jibzvarghese";

                    System.out.println("Main UNMAE "+UNAME+" "+COORD_N+" "+COORD_E+" "+COORD_S+" "+COORD_W);

                    Intent intnt = new Intent(getBaseContext(),XmlDataActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("UNAME", UNAME);
                    extras.putString("COORD_N", COORD_N);
                    extras.putString("COORD_S", COORD_S);
                    extras.putString("COORD_E", COORD_E);
                    extras.putString("COORD_W", COORD_W);
                    intnt.putExtras(extras);
                    startActivity(intnt);
                }

            }
        });
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
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
}

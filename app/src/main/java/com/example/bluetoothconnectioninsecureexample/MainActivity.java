package com.example.bluetoothconnectioninsecureexample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Menu;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.device.MagCardReader;
import com.zebra.sdk.device.MagCardReaderFactory;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.XmlPrinter;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String MAC_ADDRESS = "00:22:58:36:36:9A";
    private AppBarConfiguration mAppBarConfiguration;

    private final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1;

    private Handler handler = new Handler();
    private FloatingActionButton fabZpl;
    private FloatingActionButton fabCpcl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        View view = findViewById(android.R.id.content).getRootView();

        permissions(view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabZpl = findViewById(R.id.fabZpl);
        fabCpcl = findViewById(R.id.fabCpcl);

        fabZpl.setOnClickListener(fabZplOnCLick());
        fabCpcl.setOnClickListener(fabCpclOnCLick());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private View.OnClickListener fabZplOnCLick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendZplOverBluetooth(MAC_ADDRESS, view);
            }
        };
    }

    private View.OnClickListener fabCpclOnCLick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCpclOverBluetooth(MAC_ADDRESS, view);
            }
        };
    }

    private void showAnExplanation(String ... permission) {
        ActivityCompat.requestPermissions(this, permission,  MY_PERMISSIONS_REQUEST_BLUETOOTH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BLUETOOTH: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void permissions(View view) {
        String[] permissions = new String[6];
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissions[0] = Manifest.permission.BLUETOOTH;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissions[1] = Manifest.permission.BLUETOOTH_ADMIN;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions[2] = Manifest.permission.ACCESS_COARSE_LOCATION;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions[3] = Manifest.permission.ACCESS_FINE_LOCATION;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions[4] = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions[5] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        }
        showAnExplanation(permissions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void sendZplOverBluetooth(final String theBtMacAddress, final View view) {

        new Thread(new Runnable() {
            public void run() {
                try {

                    Snackbar.make(view, "Inicinado impressão...", Snackbar.LENGTH_LONG).setAction("Print", null).show();

                    handler.post(new Runnable() {
                        public void run() {
                            fabZpl.setEnabled(Boolean.FALSE);
                            fabCpcl.setEnabled(Boolean.FALSE);
                        }
                    });

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection connection = new BluetoothConnectionInsecure(theBtMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    connection.open();

                    Snackbar.make(view, String.format("Impressora encontrada: %s", theBtMacAddress), Snackbar.LENGTH_LONG).setAction("Print", null).show();

                    printerStatus(connection, view);

                    AssetManager am = view.getContext().getAssets();
                    InputStream inputStream = am.open("formulario/teste.ZPL");

                    //Read text from file
                    StringBuilder text = new StringBuilder();

                    try {
                        InputStreamReader isReader = new InputStreamReader(inputStream);
                        BufferedReader br = new BufferedReader(isReader);
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                        dialogError(view.getContext(), e.getMessage());
                    }

                    // Send the data to printer as a byte array.
                    connection.write(text.toString().getBytes());

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(1000);

                    // Close the insecure connection to release resources.
                    connection.close();

                    Looper.myLooper().quit();

                    Snackbar.make(view, "Impressão OK!", Snackbar.LENGTH_LONG).setAction("Print", null).show();

                    handler.post(new Runnable() {
                        public void run() {
                            fabZpl.setEnabled(Boolean.TRUE);
                            fabCpcl.setEnabled(Boolean.TRUE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    dialogError(view.getContext(), e.getMessage());
                }
                handler.post(new Runnable() {
                    public void run() {
                        fabZpl.setEnabled(Boolean.TRUE);
                        fabCpcl.setEnabled(Boolean.TRUE);
                    }
                });
            }
        }).start();
    }

    private void sendCpclOverBluetooth(final String theBtMacAddress, final View view) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    Snackbar.make(view, "Inicinado impressão...", Snackbar.LENGTH_LONG).setAction("Print", null).show();

                    handler.post(new Runnable() {
                        public void run() {
                            fabZpl.setEnabled(Boolean.FALSE);
                            fabCpcl.setEnabled(Boolean.FALSE);
                        }
                    });

                    // Instantiate insecure connection for given Bluetooth&reg; MAC Address.
                    Connection connection = new BluetoothConnectionInsecure(theBtMacAddress);

                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    connection.open();

                    Snackbar.make(view, String.format("Impressora encontrada: %s", theBtMacAddress), Snackbar.LENGTH_LONG).setAction("Print", null).show();

                    printerStatus(connection, view);

                    AssetManager am = view.getContext().getAssets();
                    InputStream inputStream = am.open("formulario/BARCODE-AZTEC.LBL");

                    //Read text from file
                    StringBuilder text = new StringBuilder();

                    try {
                        InputStreamReader isReader = new InputStreamReader(inputStream);
                        BufferedReader br = new BufferedReader(isReader);
                        String line;

                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        br.close();
                    }
                    catch (IOException e) {
                        //You'll need to add proper error handling here
                        dialogError(view.getContext(), e.getMessage());
                    }

                    // Send the data to printer as a byte array.
                    connection.write(text.toString().getBytes("ISO-8859-1"));

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(1000);

                    // Close the insecure connection to release resources.
                    connection.close();

                    Looper.myLooper().quit();

                    Snackbar.make(view, "Impressão OK!", Snackbar.LENGTH_LONG).setAction("Print", null).show();
                    handler.post(new Runnable() {
                        public void run() {
                            fabZpl.setEnabled(Boolean.TRUE);
                            fabCpcl.setEnabled(Boolean.TRUE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    dialogError(view.getContext(), e.getMessage());
                    handler.post(new Runnable() {
                        public void run() {
                            fabZpl.setEnabled(Boolean.TRUE);
                            fabCpcl.setEnabled(Boolean.TRUE);
                        }
                    });
                }
            }
        }).start();
    }

    private void dialogError(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", null);
        builder.setTitle("Erro");
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void printerStatus(Connection connection, final View view) {
        try {
            ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);

            PrinterStatus printerStatus = printer.getCurrentStatus();
            if (printerStatus.isReadyToPrint) {
                Snackbar.make(view, "Ready To Print", Snackbar.LENGTH_LONG).setAction("PrinterStatus", null).show();
            } else if (printerStatus.isPaused) {
                Snackbar.make(view, "Cannot Print because the printer is paused.", Snackbar.LENGTH_LONG).setAction("PrinterStatus", null).show();
            } else if (printerStatus.isHeadOpen) {
                Snackbar.make(view, "Cannot Print because the printer head is open.", Snackbar.LENGTH_LONG).setAction("PrinterStatus", null).show();
            } else if (printerStatus.isPaperOut) {
                Snackbar.make(view, "Cannot Print because the paper is out.", Snackbar.LENGTH_LONG).setAction("PrinterStatus", null).show();
            } else {
                Snackbar.make(view, "Cannot Print.", Snackbar.LENGTH_LONG).setAction("PrinterStatus", null).show();
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
            Snackbar.make(view, String.format("Erro na impressão: %s", e.getMessage()), Snackbar.LENGTH_LONG).setAction("PrinterStatus", null).show();
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();
            Snackbar.make(view, String.format("Erro na impressão: %s", e.getMessage()), Snackbar.LENGTH_LONG).setAction("PrinterStatus", null).show();
        }
    }
}
package com.example.stepappv4.ui.Game;

import static androidx.databinding.DataBindingUtil.setContentView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import org.w3c.dom.Text;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stepappv4.MainActivity;
import com.example.stepappv4.R;
import com.example.stepappv4.ui.Home.HomeFragment;
import com.example.stepappv4.ui.Home.StepCounterListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// The base of this code was created by following the video series: https://www.youtube.com/watch?v=SZyuFLb_wWU
// The code can be found on github: https://github.com/qaifikhan/AndroidTutorials/tree/master/BluetoothChatApp
// Afterwards it was modified by us to fit our needs.

// The GPS integration used the guidelines from ChatGPT, and
// https://www.tabnine.com/code/java/methods/com.google.gwt.maps.client.geocode.Geocoder/getFromLocation

public class Game_01_Matching extends AppCompatActivity {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SensorManager stepManager;
    private Sensor accSensor;
    private StepCounterListener stepListener;
    private ChatUtils chatUtils;
    private final int BLUETOOTH_PERMISSION_REQUEST = 100;
    private final int LOCATION_PERMISSION_REQUEST = 101;

    private boolean isBluetoothEnabled = false;

    private final int SELECT_DEVICE = 102;

    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    public static final String DEVICE_NAME = "deviceName";
    public static final String TOAST = "toast";
    private String connectedDevice;
    private ListView listMainChat;
    private TextView myText;
    private TextView oppText;
    private TextView decision;
    private EditText edCreateMessage;
    private Button btnSendMessage;
    private ArrayAdapter<String> adapterMainChat;

    private Button btnRock;
    private Button btnPaper;
    private Button btnScissors;
    private Button btnCompare;
    private Button btnPlay;
    private Button btnReplay;
    private TableLayout table;
    private TextView position;
    private TextView steps;
    private Button home;
    private TextView custom_title;
    private TextView hasChosen;
    private TextView oppHasChosen;
    private TextView oppName;

    private final ActivityResultLauncher<Intent> deviceListLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Get the returned data from DeviceListActivity
                    Intent data = result.getData();
                    if (data != null) {
                        String address = data.getStringExtra("deviceAddress");
                        // Display a Toast message with the received string
                        if (address != null) {
                            //Toast.makeText(this, "Address: " + address, Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, "Address: " + address, Toast.LENGTH_SHORT).show();
                        }
                        chatUtils.connect(bluetoothAdapter.getRemoteDevice(address));
                    }
                }
            });

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MESSAGE_STATE_CHANGED:
                    switch (message.arg1) {
                        case ChatUtils.STATE_NONE:
                            setState("Not Connected");
                            hideActionBtns();
                            btnPlay.setVisibility(View.VISIBLE);
                            break;
                        case ChatUtils.STATE_LISTEN:
                            setState("Listening For Devices...");
                            break;
                        case ChatUtils.STATE_CONNECTING:
                            setState("Connecting To Device...");
                            break;
                        case ChatUtils.STATE_CONNECTED:
                            setState("Connected To: " + connectedDevice);
                            showPlayBtns();
                            btnPlay.setVisibility(View.GONE);
                            oppName.setText(connectedDevice + ":");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] buffer1 = (byte[]) message.obj;
                    String outputBuffer = new String(buffer1);
                    adapterMainChat.add("Me: " + outputBuffer);
                    myText.setText(outputBuffer);
                    //updateOutcome(myText, oppText);
                    break;
                case MESSAGE_READ:
                    byte[] buffer = (byte[]) message.obj;
                    String inputBuffer = new String(buffer, 0, message.arg1);
                    adapterMainChat.add(connectedDevice + ": " + inputBuffer);
                    oppText.setText(inputBuffer);
                    oppHasChosen.setVisibility(View.VISIBLE);
                    //updateOutcome(myText, oppText);
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDevice = message.getData().getString(DEVICE_NAME);
                    Toast.makeText(context, connectedDevice, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context, message.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    if (message.getData().getString(TOAST) == "Connection Lost") {
                        hideActionBtns();
                        btnReplay.setVisibility(View.GONE);
                    }
                    break;
            }
            return false;
        }
    });

    //private void updateOutcome(View root, TextView myText, TextView oppText) {
    private void updateOutcome(TextView myText, TextView oppText) {
        TextView output = findViewById(R.id.decision);

        String a = myText.getText().toString();
        String b = oppText.getText().toString();

        if (a.equals("Rock") && b.equals("Rock")) {
            output.setText("It's a DRAW!");
        }
        if (a.equals("Rock") && b.equals("Paper")) {
            output.setText("You LOST,\nBetter Luck Next Time!");
        }
        if (a.equals("Rock") && b.equals("Scissors")) {
            output.setText("Victory Royale!");
        }
        if (a.equals("Paper") && b.equals("Rock")) {
            output.setText("Victory Royale!");
        }
        if (a.equals("Paper") && b.equals("Paper")) {
            output.setText("It's a DRAW!");
        }
        if (a.equals("Paper") && b.equals("Scissors")) {
            output.setText("You LOST,\nBetter Luck Next Time!");
        }
        if (a.equals("Scissors") && b.equals("Rock")) {
            output.setText("You LOST,\nBetter Luck Next Time!");
        }
        if (a.equals("Scissors") && b.equals("Paper")) {
            output.setText("Victory Royale!");
        }
        if (a.equals("Scissors") && b.equals("Scissors")) {
            output.setText("It's a DRAW!");
        }
    }

    private void showPlayBtns() {
        btnRock.setVisibility(View.VISIBLE);
        btnPaper.setVisibility(View.VISIBLE);
        btnScissors.setVisibility(View.VISIBLE);
    }

    private void hidePlayBtns() {
        btnRock.setVisibility(View.GONE);
        btnPaper.setVisibility(View.GONE);
        btnScissors.setVisibility(View.GONE);
    }

    private void showCompBtn () {
        btnCompare.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.GONE);
    }

    private void showResult() {
        table.setVisibility(View.VISIBLE);
        decision.setVisibility(View.VISIBLE);
    }

    public void hideActionBtns() {
        btnRock.setVisibility(View.GONE);
        btnPaper.setVisibility(View.GONE);
        btnScissors.setVisibility(View.GONE);
        btnCompare.setVisibility(View.GONE);
        table.setVisibility(View.GONE);
        decision.setVisibility(View.GONE);
        listMainChat.setVisibility(View.GONE);
    }

    private void setState(CharSequence subTitle) {
        custom_title.setText(subTitle);
        custom_title.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_01_matching);

        context = this;
        myText = findViewById(R.id.myTextView);
        oppText = findViewById(R.id.oppTextView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
        custom_title = findViewById(R.id.custom_title);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        stepManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = stepManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        stepListener = new StepCounterListener(null, null, null, this);


        initializeText();
        chatUtils = new ChatUtils(context, handler);
        hideActionBtns();
    }

    public void shakePhone() {
        Integer counter = 0;
        Toast.makeText(context, "Shaken, not stirred!", Toast.LENGTH_SHORT).show();
        updateOutcome(myText, oppText);
        showResult();
        hasChosen.setVisibility(View.GONE);
        oppHasChosen.setVisibility(View.GONE);
        btnReplay.setVisibility(View.VISIBLE);
        btnCompare.setVisibility(View.GONE);
        stepManager.unregisterListener(stepListener);
    }

    private void initializeText() {
        listMainChat = findViewById(R.id.list_conversation);
        edCreateMessage = findViewById(R.id.ed_enter_message);
        btnSendMessage = findViewById(R.id.btn_send_msg);
        btnRock = findViewById(R.id.btn_rock);
        btnPaper = findViewById(R.id.btn_paper);
        btnScissors = findViewById(R.id.btn_scissors);
        btnCompare = findViewById(R.id.btn_compare);
        btnPlay = findViewById(R.id.btn_play);
        btnReplay = findViewById(R.id.btn_replay);
        table = findViewById(R.id.table);
        decision = findViewById(R.id.decision);
        position = findViewById(R.id.position);
        home = findViewById(R.id.game_to_home);
        hasChosen = findViewById(R.id.hasChosen);
        oppHasChosen = findViewById(R.id.oppHasChosen);
        oppName = findViewById(R.id.oppName);

        adapterMainChat = new ArrayAdapter<String>(context, R.layout.message_layout);
        listMainChat.setAdapter(adapterMainChat);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Game_01_Matching.this, HomeFragment.class);
                startActivity(intent);
            }
        });

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = edCreateMessage.getText().toString();
                if (!message.isEmpty()) {
                    edCreateMessage.setText("");
                    chatUtils.write(message.getBytes());
                }
            }
        });

        btnRock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Rock";
                edCreateMessage.setText("");
                chatUtils.write(message.getBytes());
                showCompBtn();
                hidePlayBtns();
                hasChosen.setVisibility(View.VISIBLE);
                if (accSensor != null) {
                    stepManager.registerListener(stepListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        btnPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Paper";
                edCreateMessage.setText("");
                chatUtils.write(message.getBytes());
                showCompBtn();
                hidePlayBtns();
                hasChosen.setVisibility(View.VISIBLE);
                if (accSensor != null) {
                    stepManager.registerListener(stepListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        btnScissors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Scissors";
                edCreateMessage.setText("");
                chatUtils.write(message.getBytes());
                showCompBtn();
                hidePlayBtns();
                hasChosen.setVisibility(View.VISIBLE);
                if (accSensor != null) {
                    stepManager.registerListener(stepListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateOutcome(myText, oppText);
                showResult();
                hasChosen.setVisibility(View.GONE);
                oppHasChosen.setVisibility(View.GONE);
                btnReplay.setVisibility(View.VISIBLE);
                btnCompare.setVisibility(View.GONE);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermissions();
            }
        });

        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decision.setText("N/A");
                decision.setVisibility(View.GONE);
                myText.setText("N/A");
                oppText.setText("N/A");
                btnCompare.setVisibility(View.GONE);
                table.setVisibility(View.GONE);
                showPlayBtns();
                btnReplay.setVisibility(View.GONE);
                stepManager.unregisterListener(stepListener);
            }
        });
    }

    private void getPermissions() {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "No bluetooth found", Toast.LENGTH_SHORT).show();
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Game_01_Matching.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_PERMISSION_REQUEST);
            } else {
                // We purposefully fail this connection attempt in the beginning because otherwise we would have to fail a connection attempt manually.
                // We could not figure out why but this seems to do the job.
                chatUtils.connectFailerOnPurpose();
                if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoveryIntent);
                } else {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(context, "Please enable GPS", Toast.LENGTH_SHORT).show();
                        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(enableLocationIntent);
                    }
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(context, "Contacting Satellites...", Toast.LENGTH_SHORT).show();
                        locationListener = new LocationListener() {
                            boolean hasDisplayedCoords = false;

                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                Toast.makeText(context, "Houston, we have location!", Toast.LENGTH_SHORT).show();
                                StringBuilder result = new StringBuilder();
                                try {
                                    Geocoder geocoder = new Geocoder(Game_01_Matching.this, Locale.getDefault());
                                    List addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                    if (addresses.size() > 0) {
                                        Address address = (Address) addresses.get(0);
                                        result.append(((Address) addresses.get(0)).getAddressLine(0));
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                position.setVisibility(View.VISIBLE);
                                position.setText("Current Location:\n" + result);
                                if (!hasDisplayedCoords) {
                                    Toast.makeText(context, "Lat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_SHORT).show();
                                    hasDisplayedCoords = true;
                                } else {
                                    if (locationManager != null) {
                                        locationManager.removeUpdates(locationListener);
                                    }
                                }
                            }
                        };
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, locationListener);
                        if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            Intent intent = new Intent(Game_01_Matching.this, DeviceListActivity.class);
                            deviceListLauncher.launch(intent);
                        }
                    }
                }
            }
        }
    }


    public void onBackPressed() {
        Intent intent = new Intent(Game_01_Matching.this, HomeFragment.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatUtils != null) {
            chatUtils.stop();
        }
        if (stepManager != null && stepListener != null) {
            stepManager.unregisterListener(stepListener);
        }
    }

}

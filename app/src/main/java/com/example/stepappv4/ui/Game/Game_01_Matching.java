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
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
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

import com.example.stepappv4.R;

//public class Game_01_Matching extends Fragment {
public class Game_01_Matching extends AppCompatActivity {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager;
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

    //private ActivityResultLauncher<Intent> enableBtLauncher = registerForActivityResult(
    //        new ActivityResultContracts.StartActivityForResult(),
    //        result -> {
    //            if (result.getResultCode() == RESULT_OK) {
    //                Toast.makeText(context, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
    //            } else {
    //                Toast.makeText(context, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
    //            }
    //        }
    //);

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
                            break;
                        case ChatUtils.STATE_LISTEN:
                            setState("Listening...");
                            break;
                        case ChatUtils.STATE_CONNECTING:
                            setState("Connecting...");
                            break;
                        case ChatUtils.STATE_CONNECTED:
                            setState("Connected: " + connectedDevice);
                            showPlayBtns();
                            btnPlay.setVisibility(View.GONE);
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
                    //Toast.makeText(context, inputBuffer, Toast.LENGTH_SHORT).show();
                    oppText.setText(inputBuffer);
                    //updateOutcome(myText, oppText);
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDevice = message.getData().getString(DEVICE_NAME);
                    Toast.makeText(context, connectedDevice, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(context, message.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
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
            output.setText("DRAW");
        }
        if (a.equals("Rock") && b.equals("Paper")) {
            output.setText("LOSS");
        }
        if (a.equals("Rock") && b.equals("Scissors")) {
            output.setText("WIN");
        }
        if (a.equals("Paper") && b.equals("Rock")) {
            output.setText("WIN");
        }
        if (a.equals("Paper") && b.equals("Paper")) {
            output.setText("DRAW");
        }
        if (a.equals("Paper") && b.equals("Scissors")) {
            output.setText("LOSS");
        }
        if (a.equals("Scissors") && b.equals("Rock")) {
            output.setText("LOSS");
        }
        if (a.equals("Scissors") && b.equals("Paper")) {
            output.setText("WIN");
        }
        if (a.equals("Scissors") && b.equals("Scissors")) {
            output.setText("DRAW");
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

    private void hideActionBtns() {
        btnRock.setVisibility(View.GONE);
        btnPaper.setVisibility(View.GONE);
        btnScissors.setVisibility(View.GONE);
        btnCompare.setVisibility(View.GONE);
        table.setVisibility(View.GONE);
        decision.setVisibility(View.GONE);
        listMainChat.setVisibility(View.GONE);
    }

    private void setState(CharSequence subTitle) {
        //getSupportActionBar().setSubtitle(subTitle);
        getSupportActionBar().setTitle(subTitle);
    }

    @Override
    //public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    public void onCreate(Bundle savedInstanceState) {
        //View root = inflater.inflate(R.layout.activity_01_matching, container, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_01_matching);

        context = this;
        Toast.makeText(context, "TEST", Toast.LENGTH_SHORT).show();
        //myText = root.findViewById(R.id.myTextView);
        myText = findViewById(R.id.myTextView);
        //oppText = root.findViewById(R.id.oppTextView);
        oppText = findViewById(R.id.oppTextView);

        //Toolbar toolbar = root.findViewById(R.id.toolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //initializeText(root);
        initializeText();
        chatUtils = new ChatUtils(context, handler);
        hideActionBtns();
        //return root;
    }

    private void initializeText() {
    //private void initializeText(View root) {
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

        adapterMainChat = new ArrayAdapter<String>(context, R.layout.message_layout);
        listMainChat.setAdapter(adapterMainChat);

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
            }
        });

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateOutcome(myText, oppText);
                showResult();
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
                        if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            Intent intent = new Intent(Game_01_Matching.this, DeviceListActivity.class);
                            deviceListLauncher.launch(intent);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatUtils != null) {
            chatUtils.stop();
        }
    }

    // OLD CODE START

    //public View onCreateView(@NonNull LayoutInflater inflater,
    //                         ViewGroup container, Bundle savedInstanceState) {

    //    View root = inflater.inflate(R.layout.activity_01_matching, container, false);

    //    return root;
    //}
    // OLD CODE END

}

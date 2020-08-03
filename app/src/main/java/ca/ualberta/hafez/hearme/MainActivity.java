package ca.ualberta.hafez.hearme;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static android.content.Context.SENSOR_SERVICE;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private SeekBar seekBar;
    private TextView counterView;
    private TextView txt;
    private FloatingActionButton recordButton;
    private FloatingActionButton automateButton;
    private int recordTime;
    private File file;
    private StringBuilder dataBuffer;
    private boolean record;
    private String lastRecord;
    private String lastTimeStamp;
    private Activity activity;
    private RadioGroup radioGroup;
    private String recordMode;
    private SoundMeter soundMeter;
    private MediaPlayer mp;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

//by Dorsa
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = findViewById(R.id.txt);
        counterView = findViewById(R.id.textView);
        recordButton = findViewById(R.id.floatingActionButton);
        automateButton = findViewById(R.id.automate);
        radioGroup = findViewById(R.id.radiogroup);
        recordTime = 0;
        seekBar = findViewById(R.id.seekBar);
        record = false;
        dataBuffer = new StringBuilder(16384);
        activity = this;
        soundMeter = new SoundMeter();
        soundMeter.start();
        mp = MediaPlayer.create(this, R.raw.oth);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        pressureSensor.isAdditionalInfoSupported();
        sensorManager.registerListener(sensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_FASTEST);
        recordMode = "s";
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.noise:
                        recordMode = "n";
                        break;
                    case R.id.silence:
                        recordMode = "s";
                        break;
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                recordTime = progress;
                counterView.setText(String.valueOf(recordTime));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    //by Alireza
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
    //by Dorsa
    @Override
    protected void onStop() {
        super.onStop();
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = sensorEvent.values;
            lastRecord = String.format("%.3f", values[0]);
            Long tsLong = sensorEvent.timestamp;
            int accuracy = sensorEvent.accuracy;
            lastTimeStamp = tsLong.toString();
            Log.d("BAROMETER",String.valueOf(lastRecord));
            if (record) {
                dataBuffer.append(lastTimeStamp + ", " + lastRecord + ", " + String.valueOf(soundMeter.getAmplitude()) + ", " + String.valueOf(accuracy) + "\n");
            }
            txt.setText(lastRecord);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    //by Dorsa
    private void logData(File file, String data) {
        try {
            FileOutputStream fileinput = new FileOutputStream(file, true);
            PrintStream printstream = new PrintStream(fileinput);
            printstream.print(data);
            fileinput.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    //by Dorsa
    public void record(View v) {
        recordButton.setEnabled(false);
        seekBar.setEnabled(false);
        record = true;
        dataBuffer.setLength(0);
        Long tsLong = System.currentTimeMillis() / 1000;
        String fName = recordMode + "_" + String.valueOf(recordTime) + "_" + tsLong.toString() + ".txt";
        verifyStoragePermissions(activity);
        File folder = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "sensor_data");
        folder.mkdirs();
        file = new File(folder.getPath() + File.separator + fName);
        new CountDownTimer(recordTime * 1000, 20) {
            public void onTick(long millisUntilFinished) {
                counterView.setText(String.valueOf(millisUntilFinished / 1000.000));
            }

            public void onFinish() {
                record = false;
                logData(file, dataBuffer.toString());
                dataBuffer.setLength(0);
                recordButton.setEnabled(true);
                seekBar.setEnabled(true);
                counterView.setText(String.valueOf(recordTime));
            }

        }.start();
    }
    //by Alireza
    public void automate(View v) {
        if (mp != null) {
            mp.release();
        }
        if (recordMode.equals("n")) {
            mp = MediaPlayer.create(this, R.raw.oth);
            mp.start();
        }
        recordButton.performClick();
        new CountDownTimer((recordTime + 2) * 1000, 20) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                if (recordMode.equals("n")) {
                    mp.stop();
                    if (mp != null) {
                        mp.release();
                    }
                    recordMode = "s";
                    RadioButton b = (RadioButton) findViewById(R.id.silence);
                    b.setChecked(true);
                } else {
                    RadioButton b = (RadioButton) findViewById(R.id.noise);
                    b.setChecked(true);
                    recordMode = "n";
                }

                new CountDownTimer(2000, 20) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        automateButton.performClick();
                    }

                }.start();
            }

        }.start();
    }
}

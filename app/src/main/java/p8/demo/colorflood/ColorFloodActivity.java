package p8.demo.colorflood;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public class ColorFloodActivity extends Activity{

    private ColorFloodView view;
    private SeekBar seekBar;
    private ToggleButton toggleButton;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = getApplicationContext().getSharedPreferences("MyPrefsFile", 0);
        editor = prefs.edit();

        seekBarHandler();
        toggleButtonHandler();
        playerHandler();

        view=(ColorFloodView) findViewById(R.id.ColorFloodView);
        view.setVisibility(View.VISIBLE);
    }

    public void seekBarHandler(){
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(4);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (view.state() < 2 ^ view.getFirstClick()) view.setLevel(progress);
            }
        });
    }

    public void toggleButtonHandler(){
        toggleButton = (ToggleButton)findViewById(R.id.toggleSound);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("sound",isChecked);
                editor.commit();
            }
        });
    }

    public void playerHandler(){
        //if(prefs.getBoolean("sound",false)){
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.laneafree);
            mediaPlayer.start();
        //}
    }

    public void onPause(){
        super.onPause();
    }

    public void onResume(){
        super.onResume();
    }

    public void onStop(){
        super.onStop();
    }

    public void onRestart(){
        view.setRestart(true);
        super.onRestart();
    }
}
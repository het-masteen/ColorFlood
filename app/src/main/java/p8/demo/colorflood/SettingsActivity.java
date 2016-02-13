package p8.demo.colorflood;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPrefsFile", 0);
        final SharedPreferences.Editor editor = prefs.edit();

        final Button button_reset = (Button)findViewById(R.id.buttonReset);
        button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final CheckBox checkBoxSound = (CheckBox)findViewById(R.id.checkBoxSound);
        checkBoxSound.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(checkBoxSound.isChecked()){
                    editor.putBoolean("sound",true);
                }else{
                    editor.putBoolean("sound",false);
                }
                editor.commit();
            }
        });
    }
}

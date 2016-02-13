package p8.demo.colorflood;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class HighscoreActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscore);

        Bundle bundle = getIntent().getExtras();
        int nbLevel = bundle.getInt("nbLevel");

        //get the highscore for each level
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPrefsFile", 0);
        String score = "";
        for (int i = 0; i < nbLevel; i++) {
            score += prefs.getInt("highscore" + i, 0);
            if (i < 4) score += "-";
        }
        String[] scores = score.split("-");

        TextView tv;
        for(int i=0; i<nbLevel; i++){
            tv = (TextView)findViewById(this.getResources().getIdentifier("R.id.highscore"+i,"id",this.getPackageName()));
            tv.setText(scores[i]);
        }

        /*TextView textView = (TextView)findViewById(R.id.highscore0);
        textView.setText(scores[0]);
        textView = (TextView)findViewById(R.id.textHighScore0);
        textView.setText(getResources().getString(R.string.level)+" 0");
        textView = (TextView)findViewById(R.id.highscore1);
        textView.setText(scores[1]);
        textView = (TextView)findViewById(R.id.textHighscore1);
        textView.setText(getResources().getString(R.string.level)+" 1");
        textView = (TextView)findViewById(R.id.highscore2);
        textView.setText(scores[2]);
        textView = (TextView)findViewById(R.id.textHighscore2);
        textView.setText(getResources().getString(R.string.level)+" 2");
        textView = (TextView)findViewById(R.id.highscore3);
        textView.setText(scores[3]);
        textView = (TextView)findViewById(R.id.textHighscore3);
        textView.setText(getResources().getString(R.string.level)+" 3");
        textView = (TextView)findViewById(R.id.highscore4);
        textView.setText(scores[4]);
        textView = (TextView)findViewById(R.id.textHighscore4);
        textView.setText(getResources().getString(R.string.level)+" 4");*/
    }
}

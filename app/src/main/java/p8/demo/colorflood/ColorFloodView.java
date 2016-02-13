package p8.demo.colorflood;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.lang.*;

public class ColorFloodView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    boolean restart = false; //indique si l'activity a redémarrer

    int stateToSave= 0; //identifiant de sauvegarde de la vue

    boolean firstClick = true; //indique si l'utilisateur a commencé à jouer

    public static final String PREFS_NAME = "MyPrefsFile";

    Canvas c;

    int level = 0, score = 0, score_max = 0;
    int point_X = 0, point_Y = 0; //position du clique de l'utilisateur
    int posXWin = 0, posYWin = 0; //position du message de victoire ou de défaite

    //position des cercles de couleur en dessous du plateau de jeu
    int choiceBlueX = 0, choiceBlueY = 0;
    int choiceRedX = 0, choiceRedY = 0;
    int choiceYellowX = 0, choiceYellowY = 0;
    int choiceGreenX = 0, choiceGreenY = 0;
    int choiceOrangeX = 0, choiceOrangeY = 0;
    int choicePurpleX = 0, choicePurpleY = 0;

    // Declaration des images
    private Bitmap 		win;
    private Bitmap      lose;

    private Bitmap      bitmapBlue;
    private Bitmap      bitmapGreen;
    private Bitmap      bitmapYellow;
    private Bitmap      bitmapPurple;
    private Bitmap      bitmapRed;
    private Bitmap      bitmapOrange;
    private Bitmap[]    choice = new Bitmap[6];

    // Declaration des objets Ressources et Context permettant d'acceder aux ressources de notre application et de les charger
    private Resources resources;
    private Context context;

    // tableau modelisant la carte du jeu
    int[][] carte = new int[carteHeight][carteWidth];

    // ancres pour pouvoir centrer la carte du jeu
    int        carteTopAnchor;                   // coordonnees en Y du point d'ancrage de notre carte
    int        carteLeftAnchor;                  // coordonnees en X du point d'ancrage de notre carte

    // taille de la carte
    static final int    carteWidth    = 10;
    static final int    carteHeight   = 10;
    static final int    carteTileSize = 25;

    static final int    blue    = 0;
    static final int    red     = 1;
    static final int    yellow  = 2;
    static final int    green   = 3;
    static final int    purple  = 4;
    static final int    orange  = 5;

    // thread utiliser pour animer les zones de depot des diamants
    private     boolean in      = true;
    private     Thread  cv_thread;
    SurfaceHolder holder;

    Paint paint;

    public ColorFloodView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // permet d'ecouter les surfaceChanged, surfaceCreated, surfaceDestroyed        
        holder = getHolder();
        holder.addCallback(this);

        // chargement des images
        this.context = context;
        resources = this.context.getResources();

        lose        = BitmapFactory.decodeResource(resources, R.drawable.lose);
        win 		= BitmapFactory.decodeResource(resources, R.drawable.win);

        bitmapBlue  = BitmapFactory.decodeResource(resources, R.drawable.blue);
        bitmapRed  = BitmapFactory.decodeResource(resources, R.drawable.red);
        bitmapYellow = BitmapFactory.decodeResource(resources, R.drawable.yellow);
        bitmapGreen  = BitmapFactory.decodeResource(resources, R.drawable.green);
        bitmapPurple = BitmapFactory.decodeResource(resources, R.drawable.purple);
        bitmapOrange = BitmapFactory.decodeResource(resources, R.drawable.orange);

        choice[0]  = BitmapFactory.decodeResource(resources, R.drawable.rondbleu);
        choice[1]  = BitmapFactory.decodeResource(resources, R.drawable.rondrouge);
        choice[2] = BitmapFactory.decodeResource(resources, R.drawable.rondjaune);
        choice[3]  = BitmapFactory.decodeResource(resources, R.drawable.rondvert);
        choice[4] = BitmapFactory.decodeResource(resources, R.drawable.rondviolet);
        choice[5] = BitmapFactory.decodeResource(resources, R.drawable.rondorange);

        // initialisation des parametres du jeu
        initparameters();

        // creation du thread
        cv_thread   = new Thread(this);
        // prise de focus pour gestion des touches
        setFocusable(true);
    }

    public void initparameters() {
        paint = new Paint();
        paint.setColor(0xff0000);

        paint.setDither(true);
        paint.setColor(0x0000FF00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        paint.setTextAlign(Paint.Align.LEFT);
        initMax();
        loadlevel();
        carteTopAnchor = (getHeight() - carteHeight * carteTileSize) / 2;
        carteLeftAnchor = (getWidth() - carteWidth * carteTileSize) / 2;

        if ((cv_thread!=null) && (!cv_thread.isAlive())) {
            cv_thread.start();
        }
    }

    /**
     * Détermine le score maximum pour gagner pour chaque niveau
     */
    public void initMax(){
        switch (level) {
            case 0: score_max=5; break;
            case 1: score_max=15; break;
            case 2: score_max=20; break;
            case 3: score_max=25; break;
            case 4: score_max=30; break;
            default : score_max=1; break;
        }
    }

    /**
     * Détermine le niveau voulu et initialise les paramètres et le plateau en conséquence
     * @param level numéro de niveau sélectionné dans la SeekBar
     */
    public void setLevel(int level){
        this.level=level;
        initMax();
        initparameters();
        loadlevel();
        paintCarte(c);
        paintScore(c);
        paintChoice(c);
    }

    public void setRestart(boolean restart){
        this.restart=restart;
    }

    public boolean getFirstClick(){
        return firstClick;
    }

    /**
     * Initialise le plateau avec des valeurs aléatoires en fonction du niveau
     * L'initialisation ne s'effectue que si l'activity n'a pas redémarré, afin d'éviter de redessiner sur la partie sauvegardée
     */
    private void loadlevel() {
        if(!restart) {
            Random rnd = new Random();
            for (int y = 0; y < carteHeight; y++) {
                for (int x = 0; x < carteWidth; x++) {
                    carte[y][x] = rnd.nextInt(level + 2);
                }
            }
        }else{
            restart=false;
        }
    }

    /**
     * Dessine le message de victoire
     * @param canvas
     */
    private void paintWin(Canvas canvas) {
        posXWin=carteLeftAnchor+ 2*carteTileSize-30;
        posYWin=carteTopAnchor+ 4*carteTileSize-60;
        canvas.drawBitmap(win, posXWin, posYWin, null);
    }

    /**
     * Dessine le message de défaite
     * @param canvas
     */
    private void paintLost(Canvas canvas){
        posXWin=carteLeftAnchor+ 2*carteTileSize-30;
        posYWin=carteTopAnchor+ 4*carteTileSize-60;
        canvas.drawBitmap(lose, posXWin, posYWin, null);
    }

    /**
     * Dessine le plateau de jeu avec les couleurs appropriées
     * @param canvas
     */
    private void paintCarte(Canvas canvas) {
        for (int i=0; i< carteHeight; i++) {
            for (int j=0; j< carteWidth; j++) {
                switch (carte[i][j]) {
                    case blue:
                        canvas.drawBitmap(bitmapBlue,carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case green:
                        canvas.drawBitmap(bitmapGreen,carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case yellow:
                        canvas.drawBitmap(bitmapYellow,carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case red:
                        canvas.drawBitmap(bitmapRed,carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case purple:
                        canvas.drawBitmap(bitmapPurple,carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                    case orange:
                        canvas.drawBitmap(bitmapOrange,carteLeftAnchor+ j*carteTileSize, carteTopAnchor+ i*carteTileSize, null);
                        break;
                }
            }
        }
    }

    /**
     * Dessine les cercles de couleurs en-dessous du plateau, avec lesquels l'utilisateur peut interagir
     * @param canvas
     */
    public void paintChoice(Canvas canvas){
        switch (level) {
            case 0:
                choiceBlueX = 120;
                choiceBlueY = getHeight()-100;
                choiceRedX = choiceBlueX + 40*1;
                choiceRedY = getHeight() - 100;
                canvas.drawBitmap(choice[0], choiceBlueX, choiceBlueY, null);
                canvas.drawBitmap(choice[1], choiceRedX, choiceRedY, null);
                break;
            case 1:
                choiceBlueX = 120;
                choiceBlueY = getHeight()-100;
                choiceRedX = choiceBlueX + 40*1;
                choiceRedY = getHeight() - 100;
                choiceYellowX = choiceBlueX + 40*2;
                choiceYellowY = getHeight()-100;
                canvas.drawBitmap(choice[0], choiceBlueX, choiceBlueY, null);
                canvas.drawBitmap(choice[1], choiceRedX, choiceRedY, null);
                canvas.drawBitmap(choice[2], choiceYellowX, choiceYellowY, null);
                break;
            case 2:
                choiceBlueX = 80;
                choiceBlueY = getHeight()-100;
                choiceRedX = choiceBlueX + 40*1;
                choiceRedY = getHeight() - 100;
                choiceYellowX = choiceBlueX + 40*2;
                choiceYellowY = getHeight()-100;
                choiceGreenX = choiceBlueX + 40*3;
                choiceGreenY = getHeight() - 100;
                canvas.drawBitmap(choice[0], choiceBlueX, choiceBlueY, null);
                canvas.drawBitmap(choice[1], choiceRedX, choiceRedY, null);
                canvas.drawBitmap(choice[2], choiceYellowX, choiceYellowY, null);
                canvas.drawBitmap(choice[3], choiceGreenX, choiceGreenY, null);
                break;
            case 3:
                choiceBlueX = 40;
                choiceBlueY = getHeight()-100;
                choiceRedX = choiceBlueX + 40*1;
                choiceRedY = getHeight() - 100;
                choiceYellowX = choiceBlueX + 40*2;
                choiceYellowY = getHeight()-100;
                choiceGreenX = choiceBlueX + 40*3;
                choiceGreenY = getHeight() - 100;
                choicePurpleX = choiceBlueX + 40*4;
                choicePurpleY = getHeight() - 100;
                canvas.drawBitmap(choice[0], choiceBlueX, choiceBlueY, null);
                canvas.drawBitmap(choice[1], choiceRedX, choiceRedY, null);
                canvas.drawBitmap(choice[2], choiceYellowX, choiceYellowY, null);
                canvas.drawBitmap(choice[3], choiceGreenX, choiceGreenY, null);
                canvas.drawBitmap(choice[4], choicePurpleX, choicePurpleY, null);
                break;
            default:
                choiceBlueX = 40;
                choiceBlueY = getHeight()-100;
                choiceRedX = choiceBlueX + 40*1;
                choiceRedY = getHeight() - 100;
                choiceYellowX = choiceBlueX + 40*2;
                choiceYellowY = getHeight()-100;
                choiceGreenX = choiceBlueX + 40*3;
                choiceGreenY = getHeight() - 100;
                choicePurpleX = choiceBlueX + 40*4;
                choicePurpleY = getHeight() - 100;
                choiceOrangeX = choiceBlueX + 40*5;
                choiceOrangeY = getHeight() - 100;
                canvas.drawBitmap(choice[0], choiceBlueX, choiceBlueY, null);
                canvas.drawBitmap(choice[1], choiceRedX, choiceRedY, null);
                canvas.drawBitmap(choice[2], choiceYellowX, choiceYellowY, null);
                canvas.drawBitmap(choice[3], choiceGreenX, choiceGreenY, null);
                canvas.drawBitmap(choice[4], choicePurpleX, choicePurpleY, null);
                canvas.drawBitmap(choice[5], choiceOrangeX, choiceOrangeY, null);
                break;
        }
    }

    /**
     * Dessine le score actuel au-dessus du plateau de jeu
     * @param canvas
     */
    public void paintScore(Canvas canvas){
        posXWin=100;
        posYWin=100;

        Paint paintScore = new Paint();
        paintScore.setTextSize(40);
        paintScore.setColor(Color.BLACK);

        canvas.drawText(score + "/" + score_max, posXWin, posYWin, paintScore);
    }

    /**
     * Dessine le meilleur score du niveau
     * @param canvas
     */
    public void paintHighscore(Canvas canvas){
        Paint paintScore = new Paint();
        paintScore.setTextSize(10);
        paintScore.setColor(Color.BLACK);

        canvas.drawText("Highscore: " + getHighscore(), 220, 100, paintScore);
    }


    /**
     * Fonction principal de dessin qui dessine les bons éléments en fonction de l'état du jeu
     * @param canvas
     */
    private void nDraw(Canvas canvas) {
        canvas.drawRGB(255, 255, 255);
        if (state()==0) {
            paintScore(canvas);
            paintCarte(canvas);
            paintWin(canvas);
            paintHighscore(canvas);
            if(score< getHighscore()){
                saveScore();
            }
            firstClick=true;
        }else if(state()==1){
            paintScore(canvas);
            paintCarte(canvas);
            paintLost(canvas);
            paintHighscore(canvas);
            firstClick=true;
        } else {
            paintCarte(canvas);
            paintChoice(canvas);
            paintScore(canvas);
            paintHighscore(canvas);
        }
    }

    /**
     * Retourne l'état actuel du jeu (gagné, perdu, peut encore jouer)
     * @return 0:gagné, 1:perdu, 2:peut continuer à jouer
     */
    public int state() {
        boolean difference = false;
        for(int y=0; y<carteHeight; y++){
            for(int x=0; x<carteWidth; x++){
                if(carte[y][x]!=carte[0][0]){
                    difference = true;
                }
            }
        }

        if(!difference && score<=score_max) {
            return 0;
        }else if(score == score_max) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Récupère le meilleur score pour le niveau courant (par défaut le score maximum où l'on peut gagner)
     * @return meilleure score du niveau
     */
    public int getHighscore(){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int res = 0;
        switch(level) {
            case 0:
                res  = prefs.getInt("highscore0", score_max);
                break;
            case 1:
                res  = prefs.getInt("highscore1", score_max);
                break;
            case 2:
                res  = prefs.getInt("highscore2", score_max);
                break;
            case 3:
                res  = prefs.getInt("highscore3", score_max);
                break;
            default:
                res =  prefs.getInt("highscore4", score_max);
                break;
        }
        return res;
    }

    /**
     * Sauvegarde le score pour le niveau courant
     */
    public void saveScore() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        switch(level) {
            case 0:
                editor.putInt("highscore0", score);
                break;
            case 1:
                editor.putInt("highscore1", score);
                break;
            case 2:
                editor.putInt("highscore2", score);
                break;
            case 3:
                editor.putInt("highscore3", score);
                break;
            default:
                editor.putInt("highscore4", score);
                break;
        }
        editor.commit();
    }

    // callback sur le cycle de vie de la surfaceview
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Log.i("-> FCT <-", "surfaceChanged "+ width +" - "+ height);
        initparameters();
    }
    public void surfaceCreated(SurfaceHolder arg0) {
        //Log.i("-> FCT <-", "surfaceCreated");
    }
    public void surfaceDestroyed(SurfaceHolder arg0) {
        //Log.i("-> FCT <-", "surfaceDestroyed");
    }


    /**
     * On endort le thread, modifie le compteur d'animation, on bloque le canvas, on dessine puis on libère le canvas
     */
    public void run() {
        c = null;
        while (in) {
            try {
                cv_thread.sleep(40);
                try {
                    c = holder.lockCanvas(null);
                    nDraw(c);
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            } catch(Exception e) {
                Log.e("Run", "Exception");
            }
        }
    }

    /**
     * Fonction récursive qui va tester la couleur de chaque voisin et mettre à 10 les cases répondant aux critères
     * @param x position en x de la case à tester
     * @param y position en y de la case à tester
     */
    public void testVoisin(int x, int y){
        int value = carte[0][0];
        if (y<carteHeight-1) {
            if (carte[y+1][x]-value==0  && carte[y+1][x] != 10) {
                carte[y+1][x] = 10;
                testVoisin(x, y + 1);
            }
        }

        if(x<carteWidth-1) {
            if (carte[y][x+1]-value==0  && carte[y][x+1] != 10) {
                carte[y][x+1] = 10;
                testVoisin(x+1, y);
            }
        }

        if(y>0) {
            if (carte[y-1][x]-value==0  && carte[y-1][x] != 10) {
                carte[y-1][x] = 10;
                testVoisin(x, y-1);
            }
        }

        if(x>0){
            if (carte[y][x-1]-value==0 &&  carte[y][x-1] != 10) {
                carte[y][x-1] = 10;
                testVoisin(x-1, y);
            }
        }
    }

    /**
     * A utiliser après la méthode testVoisin
     * Change la couleur des cases dont la case vaut 10 par la couleur passée en paramètre
     * @param color code de la couleur dont on souhaite colorier
     */
    public void changeColor(int color){
        carte[0][0] = color;
        for(int y=0; y<carteHeight; y++){
            for(int x=0; x<carteWidth; x++){
                if(carte[y][x]==10){
                    carte[y][x]=color;
                }
            }
        }
    }

    /**
     * Gestion des interactions avec les cercles de couleur
     * @param color
     */
    public void clickCircle(int color){
        if(firstClick) firstClick=false;
        testVoisin(0,0);
        changeColor(color);
        paintCarte(c);
        score++;
    }

    // fonction permettant de recuperer les evenements tactiles

    /**
     * Gestion des évènements tactiles
     * @param event
     * @return
     */
    public boolean onTouchEvent (MotionEvent event) {
        //Récupération de la position de l'évènement tactile
        point_X = (int) event.getX();
        point_Y = (int) event.getY();

        //clique sur les choix de couleurs
        if(state()==2 && point_X > choiceBlueX && point_Y > choiceBlueY && point_X < choiceBlueX+choice[0].getWidth() && point_Y < choiceBlueY+choice[0].getHeight()) {
            clickCircle(0);
        }else if(state()==2 && point_X > choiceRedX && point_Y > choiceRedY && point_X < choiceRedX+choice[1].getWidth() && point_Y < choiceRedY+choice[1].getHeight()) {
            clickCircle(1);
        }else if(state()==2 && point_X > choiceYellowX && point_Y > choiceYellowY && point_X < choiceYellowX+choice[2].getWidth() && point_Y < choiceYellowY+choice[2].getHeight()) {
            clickCircle(2);
        }else if(state()==2 && point_X > choiceGreenX && point_Y > choiceGreenY && point_X < choiceGreenX+choice[3].getWidth() && point_Y < choiceGreenY+choice[3].getHeight()) {
            clickCircle(3);
        }else if(state()==2 && point_X > choicePurpleX && point_Y > choicePurpleY && point_X < choicePurpleX+choice[4].getWidth() && point_Y < choicePurpleY+choice[4].getHeight()) {
            clickCircle(4);
        } else if(state()==2 && point_X > choiceOrangeX && point_Y > choiceOrangeY && point_X < choiceOrangeX+choice[5].getWidth() && point_Y < choiceOrangeY+choice[5].getHeight()){
            clickCircle(5);

        //clique sur les logos win/lose
        }else if(state()<2 && point_X > posXWin && point_X < posXWin+win.getWidth() && point_Y > posYWin && point_Y < posYWin+win.getHeight()){
            score=0;
            initparameters();
            loadlevel();
        }
        return super.onTouchEvent(event);
    }

    /**
     * Sauvegarde l'état de la vue
     * @return
     */
    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt("stateToSave", this.stateToSave);
        bundle.putSerializable("map", carte);
        return bundle;
    }

    /**
     * Charge l'état de la vue précedemment sauvergardée
     * @param state
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.stateToSave = bundle.getInt("stateToSave");
            this.carte = (int[][])bundle.getSerializable("map");
            state = bundle.getParcelable("instanceState");
        }
        super.onRestoreInstanceState(state);
    }
}
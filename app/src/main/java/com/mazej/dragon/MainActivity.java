package com.mazej.dragon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // Frame
    private FrameLayout gameFrame;
    private int frameHeight, frameWidth, initialFrameWidth;
    private LinearLayout startLayout;
    private LinearLayout ovira;
    private ImageButton bt;

    private int screenWidth;
    private int screenHeight;

    // Image
    private ImageView dragon, gem, gold, bomb;
    private Drawable imageDragonRight, imageDragonLeft;

    // Size
    private int dragonSize;

    // Possition
    private float dragonX, dragonY;
    private float bombX, bombY;
    private float goldX, goldY;
    private float gemX, gemY;

    // Speed
    private int dragonSpeed;
    private int goldSpeed;
    private int gemSpeed;
    private int bombSpeed;

    // Score
    private TextView scoreLabel, highScoreLabel;
    private int score, highScore, timeCount;
    private SharedPreferences settings;

    // Class
    private Timer timer;
    private Handler handler = new Handler();
    private SoundPlayer soundPlayer;
    private MediaPlayer player;

    // Status
    private boolean start_flg = false;
    private boolean action_flg = false;
    private boolean gem_flg = false;

    // Adds
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sound
        player = MediaPlayer.create(MainActivity.this, R.raw.music2);
        player.start();
        player.setLooping(true);

        soundPlayer = new SoundPlayer(this);

        bt = (ImageButton) findViewById(R.id.share);
        ovira = findViewById(R.id.ovira);
        gameFrame = findViewById(R.id.gameFrame);
        startLayout = findViewById(R.id.startLayout);
        dragon = findViewById(R.id.dragon);
        bomb = findViewById(R.id.bomb);
        gold = findViewById(R.id.gold);
        gem = findViewById(R.id.gem);
        scoreLabel = findViewById(R.id.scoreLabel);
        highScoreLabel = findViewById(R.id.highScoreLabel);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody = "I've found out about this fun and addictive dungeon run game! You might check it out as well! Here is the download link: https://play.google.com/store/apps/details?id=com.mazej.dragon";
                String shareSub = "Dragon Dungeon";
                myIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(myIntent, "Share using"));
            }
        });

        imageDragonLeft = getResources().getDrawable(R.drawable.dragonbornleft);
        imageDragonRight = getResources().getDrawable(R.drawable.dragonbornright);

        // High Score
        settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        highScore = settings.getInt("HIGH_SCORE", 0);
        highScoreLabel.setText("High Score: " + highScore);

        // Get screen size
        WindowManager wm = getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point size = new Point();
        disp.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;

        // Speed
        dragonSpeed = Math.round(screenWidth / 100F);
        goldSpeed = Math.round(screenHeight / 100F);
        gemSpeed = Math.round(screenHeight / 80F);
        bombSpeed = Math.round(screenHeight / 60F);

        // Ads
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-4585950872793310/8421100755"); //ca-app-pub-3940256099942544/1033173712
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitial.loadAd(adRequest);

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitial.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    public void changePos() {

        // Add time count
        timeCount += 20;

        // Add gold speed
        goldY += goldSpeed;

        float goldCenterX = goldX + gold.getWidth() / 2;
        float goldCenterY = goldY + gold.getHeight() / 2;

        if (hitCheck(goldCenterX, goldCenterY)) {
            goldY = frameHeight + 100;
            score += 20;
            soundPlayer.playHitGold();
        }

        if (goldY > frameHeight) {
            goldY = -100;
            goldX = (float) Math.floor(Math.random() * (frameWidth - dragon.getWidth()));
        }

        gold.setX(goldX);
        gold.setY(goldY);

        // Gem
        if (!gem_flg && timeCount % 10000 == 0) {
            gem_flg = true;
            gemY = -gemSpeed;
            gemX = (float) Math.floor(Math.random() * (frameWidth - gem.getWidth()));
        }

        if (gem_flg) {
            gemY += gemSpeed;

            float gemCenterX = gemX + gem.getWidth() / 2;
            float gemCenterY = gemY + gem.getHeight() / 2;

            if (hitCheck(gemCenterX, gemCenterY)) {
                gemY = frameHeight + 30;
                score += 50;

                // Change frame width
                if (initialFrameWidth > frameWidth * 110 / 100) {
                    frameWidth = frameWidth * 110 / 100;
                    changeFrameWidth(frameWidth);
                }
                soundPlayer.playHitGem();
            }

            if (gemY > frameHeight) {
                gem_flg = false;
            }
            gem.setX(gemX);
            gem.setY(gemY);
        }

        // Bomb
        bombY += bombSpeed;

        float bombCenterX = bombX + bomb.getWidth() / 2;
        float bombCenterY = bombY + bomb.getHeight() / 2;

        if (hitCheck(bombCenterX, bombCenterY)) {
            bombY = frameHeight + 100;
            // Change framewidth
            frameWidth = frameWidth * 80 / 100;
            changeFrameWidth(frameWidth);
            soundPlayer.playHitBomb();
            if (frameWidth <= dragonSize) {
                gameOver();
            }
        }

        if (bombY > frameHeight) {
            bombY = -100;
            bombX = (float) Math.floor(Math.random() * (frameWidth - bomb.getWidth()));
        }

        bomb.setX(bombX);
        bomb.setY(bombY);

        // Move dragon
        if (action_flg) { // Touching
            dragonX += dragonSpeed;
            dragon.setImageDrawable(imageDragonRight);
        } else { // Releasing
            dragonX -= dragonSpeed;
            dragon.setImageDrawable(imageDragonLeft);
        }
        if (dragonX < 0) { // Check dragon position
            dragonX = 0;
            dragon.setImageDrawable(imageDragonRight);
        }

        if (frameWidth - dragonSize < dragonX) {
            dragonX = frameWidth - dragonSize;
            dragon.setImageDrawable(imageDragonLeft);
        }
        dragon.setX(dragonX);
        scoreLabel.setText("Curent Score: " + score);

        // Dificulty
        if (score >= 1000) {
            bombSpeed = Math.round(screenHeight / 45F);
            gameFrame.setBackground(getResources().getDrawable(R.drawable.bg2));
            imageDragonLeft = getResources().getDrawable(R.drawable.wizardleft2);
            imageDragonRight = getResources().getDrawable(R.drawable.wizardright2);
            ovira.setBackground(getResources().getDrawable(R.drawable.wall));
        }

        if (score >= 2000) {
            bombSpeed = Math.round(screenHeight / 40F);
            gameFrame.setBackground(getResources().getDrawable(R.drawable.bg3));
            dragonSpeed = Math.round(screenWidth / 80F);
            imageDragonLeft = getResources().getDrawable(R.drawable.wolfleft);
            imageDragonRight = getResources().getDrawable(R.drawable.wolfright);
            ovira.setBackground(getResources().getDrawable(R.drawable.pillars));
        }

        if (score >= 3000) {
            bombSpeed = Math.round(screenHeight / 35F);
            gameFrame.setBackground(getResources().getDrawable(R.drawable.bg4));
            dragonSpeed = Math.round(screenWidth / 70F);
            imageDragonLeft = getResources().getDrawable(R.drawable.dragonleft);
            imageDragonRight = getResources().getDrawable(R.drawable.dragonright);
            ovira.setBackground(getResources().getDrawable(R.drawable.rocks));
        }
    }

    public boolean hitCheck(float x, float y) {
        return dragonX <= x && x <= dragonX + dragonSize && dragonY <= y && y <= frameHeight;
    }

    public void changeFrameWidth(int frameWidth) {
        ViewGroup.LayoutParams params = gameFrame.getLayoutParams();
        params.width = frameWidth;
        gameFrame.setLayoutParams(params);
    }

    public void gameOver() {
        // Stop timer
        soundPlayer.playRoar();
        timer.cancel();
        timer = null;
        start_flg = false;

        // Back to normal
        gameFrame.setBackground(getResources().getDrawable(R.drawable.bg1));
        imageDragonLeft = getResources().getDrawable(R.drawable.dragonbornleft);
        imageDragonRight = getResources().getDrawable(R.drawable.dragonbornright);
        ovira.setBackground(getResources().getDrawable(R.drawable.wall2));
        bombSpeed = Math.round(screenHeight / 60F);
        dragonSpeed = Math.round(screenWidth / 100F);

        if (interstitial.isLoaded()) {
            interstitial.show();
        }

        // Before showing startLayout, sleep 1sec
        try {
            TimeUnit.SECONDS.sleep(1);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        changeFrameWidth(initialFrameWidth);

        startLayout.setVisibility(View.VISIBLE);
        dragon.setVisibility(View.INVISIBLE);
        gold.setVisibility(View.INVISIBLE);
        gem.setVisibility(View.INVISIBLE);
        bomb.setVisibility(View.INVISIBLE);

        // Update High Score
        if (score > highScore) {
            highScore = score;
            highScoreLabel.setText("High Score: " + highScore);

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("HIGH_SCORE", highScore);
            editor.apply();
        }
        score = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (start_flg) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                action_flg = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                action_flg = false;
            }
        }
        return true;
    }

    // Disable back button
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void startGame(View viev) {

        start_flg = true;
        startLayout.setVisibility(View.INVISIBLE);

        if (frameHeight == 0) {
            frameHeight = gameFrame.getHeight();
            frameWidth = gameFrame.getWidth();
            initialFrameWidth = frameWidth;

            dragonSize = dragon.getHeight();
            dragonX = dragon.getX();
            dragonY = dragon.getY();
        }

        frameWidth = initialFrameWidth;

        dragon.setX(0.0f);
        bomb.setY(3000.0f);
        gold.setY(3000.0f);
        gem.setY(3000.0f);

        bombY = bomb.getY();
        goldY = gold.getY();
        gemY = gem.getY();

        dragon.setVisibility(View.VISIBLE);
        bomb.setVisibility(View.VISIBLE);
        gold.setVisibility(View.VISIBLE);
        gem.setVisibility(View.VISIBLE);

        timeCount = 0;
        score = 0;
        scoreLabel.setText("Curent Score: 0");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (start_flg) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePos();
                        }
                    });
                }
            }
        }, 0, 20);
    }

    public void quitGame(View view) {

        // Player.stop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.stop();
        player.release();
    }

    protected void onResume() {
        super.onResume();
        player.start();
    }
}

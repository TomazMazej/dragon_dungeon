package com.mazej.dragon;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class SoundPlayer {

    private AudioAttributes audioAttributes;
    final int SOUND_POOL_MAX = 3;

    private static SoundPool soundPool;
    private static int hitgold;
    private static  int hitgem;
    private static int hitbomb;
    private static int roar;

    public SoundPlayer(Context context){

        //SoundPool is deprecated in API level 21
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            audioAttributes=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            soundPool=new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(SOUND_POOL_MAX)
                    .build();
        }else{
            soundPool = new SoundPool(SOUND_POOL_MAX, AudioManager.STREAM_MUSIC,0);
        }

        hitgold = soundPool.load(context, R.raw.gold ,1);
        hitgem = soundPool.load(context, R.raw.gem ,1);
        hitbomb = soundPool.load(context, R.raw.sword ,1);
        roar = soundPool.load(context, R.raw.dragonroar ,1);
    }

    public void playHitGold(){
        soundPool.play(hitgold, 1.0f, 1.0f, 1,0, 1.0f);
    }

    public void playHitGem(){
        soundPool.play(hitgem, 1.0f, 1.0f, 1,0, 1.0f);
    }

    public void playHitBomb(){
        soundPool.play(hitbomb, 1.0f, 1.0f, 1,0, 1.0f);
    }

    public void playRoar(){
        soundPool.play(roar, 1.0f, 1.0f, 1,0, 1.0f);
    }

}

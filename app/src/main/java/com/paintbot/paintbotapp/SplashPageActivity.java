package com.paintbot.paintbotapp;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class  SplashPageActivity extends AppCompatActivity {

    ViewGroup rootContainer;
    Scene preSplashScene;
    Scene splashScene;
    Transition transitionMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_page);

        rootContainer = findViewById(R.id.rootContainer);
        transitionMgr = TransitionInflater.from(this).inflateTransition(R.transition.transition);

        preSplashScene = Scene.getSceneForLayout(rootContainer,
                R.layout.presplash_scene, this);
        splashScene = Scene.getSceneForLayout(rootContainer,
                R.layout.activity_splash_page, this);

        preSplashScene.enter();

        TransitionManager.go(splashScene, transitionMgr);

        final AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        MediaPlayer mMediaPlayer;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.splashaudio);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(100,100);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //TODO have this send user to settings screen
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void signIn(View view) {
        Intent userProfileActivity = new Intent(this, SignInActivity.class);
        startActivity(userProfileActivity);
    }
}

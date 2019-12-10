package com.zzsoft.pcmtomp3.audiohelp;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;


import java.io.IOException;

public class MediaPlayerHelper {
    private static final String TAG = "MediaPlayerHelper";

    MediaPlayer mediaPlayer;


    public MediaPlayerHelper() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                EventBusData data = new EventBusData();
//                data.setAction(Constant.RESETBTNPLAYRECORD);
//                EventBus.getDefault().post(data);
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e(TAG, "onPrepared: " + mp.getDuration());
                mp.start();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "onError: " + what + "--------" + extra);
                return false;
            }
        });

    }

    public void startPlayAudio(String mp3Url){

        try {
            mediaPlayer.reset();

            mediaPlayer.setDataSource(mp3Url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopAudio(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
}

package com.android.samplesmartpen.utilities;

import android.media.MediaPlayer;
import android.os.Environment;

import java.io.IOException;

public class AudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    MediaPlayer mediaPlayer;

    public AudioPlayer() {

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void setAudio() {
        try {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/SamplePen/000001/" + String.format("%d_%d.mp3", Util.questionNumber, Const.SetID));
            mediaPlayer.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Seekto(int progress) {
        mediaPlayer.seekTo(progress);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.reset();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        mseekbar.setMax(mediaPlayer.getDuration());
//        mediaPlayer.start();
//        playCycle();
    }


}

package com.android.samplesmartpen.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.samplesmartpen.R;
import com.android.samplesmartpen.TestActivity;
import com.android.samplesmartpen.utilities.AudioPlayer;
import com.android.samplesmartpen.utilities.Const;
import com.android.samplesmartpen.utilities.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FillinFragment extends Fragment {

    private TextView mtvQuestion, mtvAnswer;
    private ImageView mivImageQuestion, mivXO;
    private VideoView mvvVideoQuestion;
    private SeekBar mseekbar;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;

    private AudioPlayer audioPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fill_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mtvQuestion = view.findViewById(R.id.tvQuestion);
        mtvAnswer = view.findViewById(R.id.tvAnswer);
        mivImageQuestion = view.findViewById(R.id.ivImageQuestion);
        mvvVideoQuestion = view.findViewById(R.id.vvVideoQuestion);
        mseekbar = view.findViewById(R.id.seekbar);
        mivXO = view.findViewById(R.id.ivXO);

        mediaPlayer = new MediaPlayer();
        if ((isAudioAvailable())) {
            mseekbar.setVisibility(View.VISIBLE);
        } else {
            mseekbar.setVisibility(View.GONE);
        }

        audioPlayer = new AudioPlayer();


        if (isVideoAvailable()) {
            mvvVideoQuestion.setVideoPath(Environment.getExternalStorageDirectory() + "/SamplePen/000001/" + String.format("%d_%d.mp4", Util.questionNumber, Const.SetID));
            mvvVideoQuestion.seekTo(100);
            mvvVideoQuestion.setVisibility(View.VISIBLE);
        } else
            mvvVideoQuestion.setVisibility(View.GONE);


        handler = new Handler();
        setImage();
        mtvQuestion.setText(String.format("%d.) %s", Util.falcontent.get(0).getFalQuestions().get(Util.questionNumber - 1).getQNo(),
                Util.falcontent.get(0).getFalQuestions().get(Util.questionNumber - 1).getQuest()));

        mivXO.setVisibility(View.GONE);
        ((TestActivity) getActivity()).setOnBundleSelected(new TestActivity.SelectedBundle() {
            @Override
            public void onBundleSelect(Bundle bundle) {
                if (bundle != null) {
                    mtvAnswer.setText(bundle.getString("answer", "0"));
                    if (Util.falcontent.get(0).getFalQuestions().get(Util.questionNumber - 1).getAnswer().equals(bundle.getString("answer", "0"))) {
                        mivXO.setImageResource(R.drawable.o_icon);
                    } else {
                        mivXO.setImageResource(R.drawable.x_icon);
                    }
                    mivXO.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void mediaSelected() {
                if (isAudioAvailable()) {
                    audioPlayer.setAudio();
                    setAudio();
                }
                if (isVideoAvailable()) {
                    setVideo();
                }
            }
        });

        mseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    audioPlayer.Seekto(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.reset();
        }

        if (mvvVideoQuestion.isPlaying()) {
            mvvVideoQuestion.stopPlayback();
            mvvVideoQuestion.suspend();
        }
    }

    private void playCycle() {
        mseekbar.setProgress(mediaPlayer.getCurrentPosition());

        if (mediaPlayer.isPlaying()) {
            runnable = this::playCycle;
            handler.postDelayed(runnable, 1000);
        }
    }

    private void setImage() {

        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/SamplePen/000001/", String.format("%d_%d.jpg", Util.questionNumber, Const.SetID));
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file));
            mivImageQuestion.setImageBitmap(b);
            mivImageQuestion.setVisibility(View.VISIBLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mivImageQuestion.setVisibility(View.GONE);
        }
    }

    private void setAudio() {

        try {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/SamplePen/000001/" + String.format("%d_%d.mp3", Util.questionNumber, Const.SetID));
            mediaPlayer.prepare();

            mediaPlayer.setOnPreparedListener(mp -> {
                mseekbar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                playCycle();
            });

            mediaPlayer.setOnCompletionListener(mp -> mediaPlayer.reset());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setVideo() {
        mvvVideoQuestion.start();
    }

    private boolean isAudioAvailable() {
        File file = new File(Environment.getExternalStorageDirectory() + "/SamplePen/000001/", String.format("%d_%d.mp3", Util.questionNumber, Const.SetID));
        return file.exists();
    }

    private boolean isVideoAvailable() {
        File file = new File(Environment.getExternalStorageDirectory() + "/SamplePen/000001/", String.format("%d_%d.mp4", Util.questionNumber, Const.SetID));
        return file.exists();
    }

}

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.samplesmartpen.R;
import com.android.samplesmartpen.TestActivity;
import com.android.samplesmartpen.utilities.Const;
import com.android.samplesmartpen.utilities.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MultipleChoiceFragment extends Fragment {

    private TextView mtvQuestion;
    private ImageView mivImageQuestion, mivXO;
    private VideoView mvvVideoQuestion;
    private SeekBar mSeekbar;
    private RadioGroup mrgOptions;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multiple_choice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mtvQuestion = view.findViewById(R.id.tvQuestion);
        mivImageQuestion = view.findViewById(R.id.ivImageQuestion);
        mivXO = view.findViewById(R.id.ivXO);
        mvvVideoQuestion = view.findViewById(R.id.vvVideoQuestion);
        mSeekbar = view.findViewById(R.id.seekbar);
        mrgOptions = view.findViewById(R.id.rgOptions);
        mediaPlayer = new MediaPlayer();

        addOptions();

        if ((isAudioAvailable())) {
            mSeekbar.setVisibility(View.VISIBLE);
        } else {
            mSeekbar.setVisibility(View.GONE);
        }

        if (isVideoAvailable()) {
            mvvVideoQuestion.setVisibility(View.VISIBLE);
            mvvVideoQuestion.setVideoPath(Environment.getExternalStorageDirectory() + "/SamplePen/000001/" + String.format("%d_%d.mp4", Util.questionNumber, Const.SetID));
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
                    int checked = bundle.getInt("answer");
                    mrgOptions.check(mrgOptions.getChildAt(checked - 1).getId());

                    if (Util.falcontent.get(0).getFalQuestions().get(Util.questionNumber - 1).getAnswer().equals(String.valueOf(checked))) {
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
                    setAudio();
                }
                if (isVideoAvailable()) {
                    setVideo();
                }
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mediaPlayer.seekTo(progress);
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

    private void addOptions() {
        int optionsize = Util.falcontent.get(0).getFalQuestions().get(Util.questionNumber - 1).getFalOptions().size();

        for (int i = 0; i < optionsize; i++) {
            RadioButton rbtn = new RadioButton(getContext());
            rbtn.setId(View.generateViewId());
            rbtn.setText(Util.falcontent.get(0).getFalQuestions().get(Util.questionNumber - 1).getFalOptions().get(i).getOption());
            mrgOptions.addView(rbtn);
        }
    }

    private void playCycle() {
        mSeekbar.setProgress(mediaPlayer.getCurrentPosition());

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
                mSeekbar.setMax(mediaPlayer.getDuration());
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

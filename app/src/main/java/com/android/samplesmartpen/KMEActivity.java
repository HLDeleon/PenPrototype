package com.android.samplesmartpen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;

import com.android.samplesmartpen.models.Content;
import com.android.samplesmartpen.models.Mark;
import com.android.samplesmartpen.parsers.SetHandler;
import com.android.samplesmartpen.parsers.XMLParser;
import com.android.samplesmartpen.renderer.SampleView;
import com.android.samplesmartpen.utilities.Const;
import com.android.samplesmartpen.utilities.Util;
import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.MimeType;
import com.myscript.iink.PointerEvent;
import com.myscript.iink.uireferenceimplementation.EditorView;
import com.myscript.iink.uireferenceimplementation.InputController;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import kr.neolab.sdk.ink.structure.Dot;
import kr.neolab.sdk.ink.structure.DotType;

public class KMEActivity extends AppCompatActivity {
    private static final String TAG = "KMEActivity";

    private ContentLoadingProgressBar progressBar;

    private ArrayList<Content> falContents;
    private ArrayList<PointerEvent> events;

    private Engine engine;
    private ContentPackage contentPackage;
    private ContentPart contentPart;
    private EditorView editorView;
    private Editor editor = null;
    private SampleView sampleView;

    private int currentSectionId = -1;
    private int currentOwnerId = -1;
    private int currentBookcodeId = -1;
    private int currentPagenumber = -1;
    private int currentContent = -1;
    private int currentQuestion = -1;

    private boolean isMatchingType = false;

    private HashMap<String, String> MapAnswers = new HashMap<>();
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kme);

        progressBar = findViewById(R.id.pb);
        progressBar.setVisibility(View.VISIBLE);

        sampleView = new SampleView(this);
        FrameLayout.LayoutParams para = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        para.gravity = Gravity.TOP;
        ((FrameLayout) findViewById(R.id.sampleview_frame)).addView(sampleView, 0, para);

        handler = new Handler();
        initializeMyScript();
        new getContentsTask().execute(Const.SetID);

        for (int i = 0; i < coordinatesx.length; i++) {
            sampleButtons(coordinatesx[i],coordinatesy[i]);
        }

    }

    float[] coordinatesx = {77.2f, 77.2f};
    float[] coordinatesy = {39.1f, 44.2f};
    float paper_offsetX, paper_offsetY, paper_width, paper_height, paper_scale, offsetX, offsetY;

    private void sampleButtons(float x, float y) {

        paper_offsetX = 0f;
        paper_offsetY = 0f;
        paper_width = 595f * 0.14880952f;
        paper_height = 838f * 0.14880952f;

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        Log.v("carlo", "Width:" + metrics.widthPixels);
        Log.v("carlo", "Height:" + metrics.heightPixels);

        float width_ratio = metrics.widthPixels / paper_width;
        float height_ratio = metrics.heightPixels / paper_height;

        paper_scale = Math.min(width_ratio, height_ratio);

        int docWidth = (int) (paper_width * paper_scale);
        int docHeight = (int) (paper_height * paper_scale);

        int mw = metrics.widthPixels - docWidth;
        int mh = metrics.heightPixels - docHeight;
        offsetX = mw / 2;
        offsetY = mh / 2;
        float ofx = offsetX / 2;
        float screen_offset_x = -paper_offsetX * paper_scale;
        float screen_offset_y = -paper_offsetY * paper_scale;

        Button test_button = new Button(getApplicationContext());
        test_button.setText("test");

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(100, 100); // Button width and button height.
        lp.leftMargin = (int) ((x - 20) * (paper_scale) + screen_offset_x + ofx); // your X coordinate.
        lp.topMargin = (int) (y * (paper_scale) + screen_offset_y + offsetY);  // your Y coordinate.

        RelativeLayout layout = findViewById(R.id.relative);
        layout.addView(test_button, lp);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.Broadcast.ACTION_PEN_DOT);
        filter.addAction(Const.Broadcast.ACTION_OFFLINE_STROKES);
        filter.addAction(Const.Broadcast.ACTION_WRITE_PAGE_CHANGED);

        filter.addAction("firmware_update");

        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editorView.close();
        if (contentPart != null) {
            contentPart.close();
            contentPart = null;
        }
        if (contentPackage != null) {
            contentPackage.close();
            contentPackage = null;
        }
        engine = null;

        unregisterReceiver(mBroadcastReceiver);
    }

    private void initializeMyScript() {
        editorView = findViewById(R.id.editor_view);

        engine = NeoSmartpenApplication.getEngine();

        Configuration conf = engine.getConfiguration();
        String confDir = "zip://" + getPackageCodePath() + "!/assets/conf";
        conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
        String tempDir = getFilesDir().getPath() + File.separator + "tmp";
        conf.setString("content-package.temp-folder", tempDir);

        editorView.setEngine(engine);

        editor = editorView.getEditor();
        editorView.setInputMode(InputController.INPUT_MODE_FORCE_PEN); // If using an active pen, put INPUT_MODE_AUTO here

        String packageName = "File1.iink";
        File file = new File(getFilesDir(), packageName);
        try {
            contentPackage = engine.createPackage(file);
            contentPart = contentPackage.createPart("Math"); // Choose type of content (possible values are: "Text Document", "Text", "Diagram", "Math", and "Drawing")

        } catch (IOException e) {
            Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
        }

        // wait for view size initialization before setting part
        editorView.post(() -> {

            if (editor != null) {
                editor.setPart(contentPart);
                editor.setTheme(".math { -myscript-pen-width: 0.1;" +
                        "-myscript-pen-brush: polyline}");
            }
        });

        events = new ArrayList<>();
    }

    /**
     * AsyncTask for retrieving Contents
     */
    private class getContentsTask extends AsyncTask<Integer, Content, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            falContents = new ArrayList<>();
        }

        @Override
        protected Integer doInBackground(Integer... setId) {
            try {
                XMLParser xmlParser = new XMLParser();
                SetHandler setHandler = new SetHandler();
                setHandler.setOnSetParsed(this::publishProgress);

                xmlParser.ParseXML(new URL(String.format(Const.PHP_SETS_LIST, setId[0])), setHandler);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Content... values) {
            super.onProgressUpdate(values);
            falContents.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            Util.falcontent = falContents;
            if (falContents.isEmpty()) {
                Util.showToast(KMEActivity.this, "An error has occurred when retrieving the contents.");
            } else {
                progressBar.setVisibility(View.GONE);
                Util.showToast(KMEActivity.this, "Data parsed successfully!");
            }
        }
    }

    /**
     * BroadcastReceiver for handling Pen broadcast
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case Const.Broadcast.ACTION_PEN_DOT:

                        String penAddress = intent.getStringExtra(Const.Broadcast.PEN_ADDRESS);
                        Dot dot = intent.getParcelableExtra(Const.Broadcast.EXTRA_DOT);
                        dot.color = Color.BLACK;

                        sampleView.addDot(penAddress, dot);

                        float x = (dot.x * 100);
                        float y = (dot.y * 100);

                        if (DotType.isPenActionDown(dot.getDotType())) {
                            isMatchingType = false;
                            checkQuestionCoordinate(dot.x, dot.y);
                            if (checkAnswerCoordinate(dot.x, dot.y)) {
                                stopTimer();
                            }
                            events.add(new PointerEvent().down(x, y));

                        } else if (DotType.isPenActionMove(dot.getDotType())) {
                            events.add(new PointerEvent().move(x, y));
                        } else {
                            events.add(new PointerEvent().up(x, y));
                            editor.pointerEvents(events.toArray(new PointerEvent[0]), false);
                            startTimer(dot.x, dot.y);
                        }
                        break;

                    case Const.Broadcast.ACTION_OFFLINE_STROKES:
                        break;

                    case Const.Broadcast.ACTION_WRITE_PAGE_CHANGED:

                        int sectionId = intent.getIntExtra(Const.Broadcast.EXTRA_SECTION_ID, -1);
                        int ownerId = intent.getIntExtra(Const.Broadcast.EXTRA_OWNER_ID, -1);
                        int noteId = intent.getIntExtra(Const.Broadcast.EXTRA_BOOKCODE_ID, -1);
                        int pageNum = intent.getIntExtra(Const.Broadcast.EXTRA_PAGE_NUMBER, -1);

                        currentSectionId = sectionId;
                        currentOwnerId = ownerId;
                        currentBookcodeId = noteId;
                        currentPagenumber = pageNum;

                        getCurrentContent();

                        sampleView.changePage(sectionId, ownerId, noteId, pageNum);
                        break;
                }
            }
        }
    };

    private void startTimer(float x, float y) {
        final String[] result = {""};

        handler.postDelayed(() -> {
            if (isMatchingType) {
                if (checkAnswerCoordinate(x, y)) {
                    Log.v("Carlo", myAnswer);
                    Log.v("Carlo", getAnswer(currentQuestion - 1));
                    if (myAnswer.equals(getAnswer(currentQuestion - 1))) {
                        Toast.makeText(this, "Correct", Toast.LENGTH_SHORT).show();
                        MapAnswers.put(String.valueOf(currentQuestion), myAnswer);
                    } else {
                        Toast.makeText(this, "Wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                if (editor != null) {
                    editor.waitForIdle();
                    try {
                        result[0] = editor.export_(null, MimeType.TEXT);
                        result[0] = result[0].replaceAll("[^0-9]", "");

                        if (!result[0].equals("") && currentQuestion != -1) {
                            MapAnswers.put(String.valueOf(currentQuestion), result[0]);
                            Log.v("Carlo", currentQuestion + ": " + result[0]);
                            Util.showToast(this, currentQuestion + ": " + result[0]);
                        } else {
                            Log.v("Carlo", currentQuestion + ": " + result[0]);
                            Util.showToast(this, "Sorry text not recognized.Please try again.");
                        }

                        events.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    editor.clear();
                }
            }
        }, 1000);
    }

    private void stopTimer() {
        handler.removeMessages(0);
    }

    private void getCurrentContent() {
        for (int i = 0; i < falContents.size(); i++) {
            if (falContents.get(i).getPages()[0] <= currentPagenumber + 1 && falContents.get(i).getPages()[1] >= currentPagenumber + 1) {
                currentContent = i;
            }
        }
    }

    private void checkQuestionCoordinate(float x, float y) {
        for (int i = 0; i < falContents.get(currentContent).getFalQuestions().size(); i++) {
            if (falContents.get(currentContent).getFalQuestions().get(i).getQnoCoor().contains(x, y)) {
                currentQuestion = falContents.get(currentContent).getFalQuestions().get(i).getQNo();
                isMatchingType = true;
            }
        }
    }

    String myAnswer;

    private boolean checkAnswerCoordinate(float x, float y) {
        for (int i = 0; i < falContents.get(currentContent).getFalQuestions().size(); i++) {
            for (int j = 0; j < falContents.get(currentContent).getFalQuestions().get(i).getFalOptions().size(); j++) {
                if (falContents.get(currentContent).getFalQuestions().get(i).getFalOptions().get(j).getOptionCoordinate().contains(x, y)) {

                    if (isMatchingType) {
                        myAnswer = falContents.get(currentContent).getFalQuestions().get(i).getAnswer();
                    } else {
                        currentQuestion = falContents.get(currentContent).getFalQuestions().get(i).getQNo();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private String getAnswer(int qnum) {
        return falContents.get(currentContent).getFalQuestions().get(qnum).getAnswer();
    }

    private Mark mark;
    private Bitmap temp_mark, Omark;
    private ArrayList<Mark> marks;
    private int totScore = 0;

    private void checkAnswer(String mAnswer, int qnum) {

        if (mAnswer != null && !mAnswer.isEmpty()) {
            for (int j = 0; j < falContents.get(currentContent).getFalQuestions().get(qnum).getFalOptions().size(); j++) {

                float CenterX = falContents.get(currentContent).getFalQuestions().get(qnum).getFalOptions().get(j).getOptionCoordinate().centerX();
                float CenterY = falContents.get(currentContent).getFalQuestions().get(qnum).getFalOptions().get(j).getOptionCoordinate().centerY();
                float l = falContents.get(currentContent).getFalQuestions().get(qnum).getFalOptions().get(j).getOptionCoordinate().left;
                float t = falContents.get(currentContent).getFalQuestions().get(qnum).getFalOptions().get(j).getOptionCoordinate().top;
                float r = falContents.get(currentContent).getFalQuestions().get(qnum).getFalOptions().get(j).getOptionCoordinate().right;
                float b = falContents.get(currentContent).getFalQuestions().get(qnum).getFalOptions().get(j).getOptionCoordinate().bottom;

                mark = new Mark();

                if (mAnswer.equals(getAnswer(qnum))) {
                    temp_mark = BitmapFactory.decodeResource(getResources(), R.drawable.correct);
                    mark.setCorrect(true);
                    totScore++;
                } else {
                    mark.setCorrect(false);
                    temp_mark = BitmapFactory.decodeResource(getResources(), R.drawable.wrong);
                }

                Omark = Bitmap.createBitmap(temp_mark);

                mark.setBitmap(Omark);
                mark.setCenterX(CenterX);
                mark.setCenterY(CenterY);
                mark.setLeft(l);
                mark.setRight(r);
                mark.setTop(t);
                mark.setBottom(b);

                marks.add(mark);
            }
            sampleView.addMarks(marks);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            marks = new ArrayList<>();
            totScore = 0;
            for (int i = 0; i < falContents.get(currentContent).getFalQuestions().size(); i++) {
                checkAnswer(MapAnswers.get(String.valueOf(i + 1)), i);
            }
            Toast.makeText(this, "Total Score: " + totScore, Toast.LENGTH_LONG).show();
        }
        return true;
    }
}

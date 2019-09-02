package com.android.samplesmartpen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.samplesmartpen.fragments.FillinFragment;
import com.android.samplesmartpen.fragments.InstructionsFragment;
import com.android.samplesmartpen.fragments.MultipleChoiceFragment;
import com.android.samplesmartpen.models.Content;
import com.android.samplesmartpen.parsers.SetHandler;
import com.android.samplesmartpen.parsers.XMLParser;
import com.android.samplesmartpen.renderer.SampleView;
import com.android.samplesmartpen.utilities.Const;
import com.android.samplesmartpen.utilities.Const.Broadcast;
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

@SuppressWarnings("ALL")
public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";

    private TextView mtvScore;
    private ProgressBar mpgbar;

    private SampleView sampleView;
    private Engine engine;
    private ContentPackage contentPackage;
    private ContentPart contentPart;
    private EditorView editorView;
    private Editor editor = null;
    private ArrayList<PointerEvent> events;

    private static ArrayList<Content> falContents;

    Handler handler = new Handler();
    Bundle bundle = new Bundle();
    private int currentIndex, totalscore = 0;
    private int type = 2;
    private int optionSelected = 0;

    private HashMap<String, String> MapAnswers = new HashMap<>();

    private int currentSectionId = -1;
    private int currentOwnerId = -1;
    private int currentBookcodeId = -1;
    private int currentPagenumber = -1;

    public interface SelectedBundle {
        void onBundleSelect(Bundle bundle);

        void mediaSelected();
    }

    SelectedBundle selectedBundle;

    public void setOnBundleSelected(SelectedBundle selectedBundle) {
        this.selectedBundle = selectedBundle;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mtvScore = findViewById(R.id.tvScore);
        mpgbar = findViewById(R.id.progressBar);
        totalscore = 0;

        sampleView = new SampleView(this);
        FrameLayout.LayoutParams para = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        para.gravity = Gravity.TOP;
        ((FrameLayout) findViewById(R.id.sampleview_frame)).addView(sampleView, 0, para);

        mtvScore.setVisibility(View.GONE);

        initializeMyScript();
        new getSetsTask().execute(Const.SetID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //TODO: Register Receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Broadcast.ACTION_PEN_DOT);
        filter.addAction(Broadcast.ACTION_OFFLINE_STROKES);
        filter.addAction(Broadcast.ACTION_WRITE_PAGE_CHANGED);


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

//            if (editorView.getRenderer() != null) {
//                editorView.setVisibility(View.VISIBLE);
//                editorView.getRenderer().setViewOffset(0, 0);
//                editorView.getRenderer().setViewScale(1);
//            }

            if (editor != null) {
                editor.setPart(contentPart);
                editor.setTheme(".math { -myscript-pen-width: 0.1}");
            }
        });
        events = new ArrayList<>();
    }

    int currentContent = 0;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case Broadcast.ACTION_PEN_DOT:

                        String penAddress = intent.getStringExtra(Broadcast.PEN_ADDRESS);
                        Dot dot = intent.getParcelableExtra(Broadcast.EXTRA_DOT);
                        dot.color = Color.BLACK;

                        sampleView.addDot(penAddress, dot);

                        float x = (dot.x * 100);
                        float y = (dot.y * 100);

                        if (DotType.isPenActionDown(dot.getDotType())) {

                            if (checkMedia(dot.x, dot.y)) {
//                            selectedBundle.mediaSelected();
                                totalscore = 0;
                                for (int i = 0; i < falContents.get(currentContent).getFalQuestions().size(); i++) {
                                    Log.v("carlo", (i + 1) + ": " + MapAnswers.get(String.valueOf(i + 1)));
                                    if (falContents.get(currentContent).getFalQuestions().get(i).getAnswer().equals(MapAnswers.get(String.valueOf(i + 1)))) {
                                        totalscore++;
                                    }
                                }
                                Log.v("carlo", "Total Score: " + totalscore);
                            }

                            if (checkQuestionNumber(dot.x, dot.y) && !checkMedia(dot.x, dot.y)) {
                                //                            initFragment(falContents.get(0).getFalQuestions().get(currentIndex).getQType());
                                Util.questionNumber = falContents.get(currentContent).getFalQuestions().get(currentIndex).getQNo();
                                Log.v("carlo", "Qnum: " + Util.questionNumber);

                                stopTimer();
                            }
                            events.add(new PointerEvent().down(x, y));

                        } else if (DotType.isPenActionMove(dot.getDotType())) {
                            events.add(new PointerEvent().move(x, y));
                        } else {
                            events.add(new PointerEvent().up(x, y));
                            editor.pointerEvents(events.toArray(new PointerEvent[0]), false);
                            events.clear();
                            if (checkQuestionNumber(dot.x, dot.y) && !checkMedia(dot.x, dot.y)) {
                                mpgbar.setVisibility(View.VISIBLE);
                                startTimer(dot.x, dot.y);
                            }
                        }
                        break;
                    case Broadcast.ACTION_OFFLINE_STROKES:

                        break;
                    case Broadcast.ACTION_WRITE_PAGE_CHANGED:
                        int sectionId = intent.getIntExtra(Broadcast.EXTRA_SECTION_ID, -1);
                        int ownerId = intent.getIntExtra(Broadcast.EXTRA_OWNER_ID, -1);
                        int noteId = intent.getIntExtra(Broadcast.EXTRA_BOOKCODE_ID, -1);
                        int pageNum = intent.getIntExtra(Broadcast.EXTRA_PAGE_NUMBER, -1);

                        currentSectionId = sectionId;
                        currentOwnerId = ownerId;
                        currentBookcodeId = noteId;
                        currentPagenumber = pageNum;

                        for (int i = 0; i < falContents.size(); i++) {
                            if (falContents.get(i).getPages()[0] <= currentPagenumber && falContents.get(i).getPages()[1] >= currentPagenumber) {
                                currentContent = currentPagenumber + 1;
                            }
                        }

                        sampleView.changePage(sectionId, ownerId, noteId, pageNum);
                        break;
                }
            }
        }
    };

    private void startTimer(final float x, final float y) {
        final String[] result = {""};

        handler.postDelayed(() -> {
            bundle.clear();
            mpgbar.setVisibility(View.GONE);
            switch (type) {
                case 1:
                    if (checkOption(x, y)) {
                        bundle.putInt("answer", optionSelected);

                        if (falContents.get(currentContent).getFalQuestions().get(Util.questionNumber - 1).getAnswer().equals(String.valueOf(optionSelected))) {
                            totalscore++;
                            mtvScore.setText(String.format("Total Score:%d/%d", totalscore, falContents.get(0).getFalQuestions().size()));
                        }

                        if (bundle != null)
                            selectedBundle.onBundleSelect(bundle);
                    }
                    break;
                case 2:
                    if (editor != null) {
                        editor.waitForIdle();
                        try {
                            result[0] = editor.export_(null, MimeType.TEXT);
                            result[0] = result[0].replaceAll("[^0-9]", "");
                            Log.v("carlo", "Result: " + result[0]);

                            if (!result[0].equals(""))
                                MapAnswers.put(Util.questionNumber + "", result[0]);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        editor.clear();
                    }
                    bundle.putString("answer", result[0]);

//                    if (falContents.get(0).getFalQuestions().get(Util.questionNumber - 1).getAnswer().equals(result[0])) {
//                        totalscore++;
//                        mtvScore.setText(String.format("Total Score:%d/%d", totalscore, falContents.get(0).getFalQuestions().size()));
//                    }
//
//                    if (bundle != null)
//                        selectedBundle.onBundleSelect(bundle);
                    break;
            }
        }, 2000);
    }

    private void stopTimer() {
        handler.removeMessages(0);
    }

    private class getSetsTask extends AsyncTask<Integer, Content, Integer> {

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
                Util.showToast(TestActivity.this, "An Error has occurred when retrieving the contents.");
            } else {
                mpgbar.setVisibility(View.GONE);
//                initFragment(0);
            }
        }
    }

    private boolean checkQuestionNumber(float x, float y) {

        for (int i = 0; i < falContents.get(currentContent).getFalQuestions().size(); i++) {
            for (int j = 0; j < falContents.get(currentContent).getFalQuestions().get(i).getFalOptions().size(); j++) {
                if (falContents.get(currentContent).getFalQuestions().get(i).getFalOptions().get(j).getOptionCoordinate().contains(x, y)) {
                    currentIndex = i;
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkMedia(float x, float y) {
        for (int i = 0; i < falContents.get(currentContent).getFalQuestions().size(); i++) {
            if (falContents.get(currentContent).getFalQuestions().get(i).getMediaCoor().contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOption(float x, float y) {
        for (int i = 0; i < falContents.get(currentContent).getFalQuestions().size(); i++) {
            for (int j = 0; j < falContents.get(currentContent).getFalQuestions().get(i).getFalOptions().size(); j++) {
                if (falContents.get(currentContent).getFalQuestions().get(i).getFalOptions().get(j).getOptionCoordinate().contains(x, y)) {
                    optionSelected = j + 1;
                    return true;
                }
            }
        }
        return false;
    }

    private void initFragment(int qtype) {
        type = qtype;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (qtype == 1) {
            mtvScore.setVisibility(View.VISIBLE);
            MultipleChoiceFragment multipleChoiceFragment = new MultipleChoiceFragment();
            transaction.replace(R.id.testfragment, multipleChoiceFragment);
        } else if (qtype == 2) {
            mtvScore.setVisibility(View.VISIBLE);
            FillinFragment fillinFragment = new FillinFragment();
            transaction.replace(R.id.testfragment, fillinFragment);
        } else {
            InstructionsFragment instructionsFragment = new InstructionsFragment();
            transaction.replace(R.id.testfragment, instructionsFragment);
        }

        transaction.commit();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            sampleView.saveImage(0);
        }
        return true;
    }
}

package com.android.samplesmartpen.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.android.samplesmartpen.models.Mark;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import kr.neolab.sdk.ink.structure.Dot;
import kr.neolab.sdk.ink.structure.DotType;
import kr.neolab.sdk.ink.structure.Stroke;
import kr.neolab.sdk.metadata.MetadataCtrl;


public class SampleView extends View {

    public enum ZoomFitType {
        FIT_SCREEN, FIT_WIDTH, FIT_HEIGHT
    }

    // paper background
    private Bitmap background = null, Omark;

    private Bitmap temp_mark;

    public Canvas canvas;

    // draw the strokes
    public ArrayList<Stroke> strokes = new ArrayList<Stroke>();

    //	private Stroke stroke = null;
    private HashMap<String, Stroke> mapStroke = new HashMap<>();
    // page details
    private int sectionId = 0, ownerId = 0, noteId = 0, pageId = 0;
    // View settings
    private float paper_scale = 11, offsetX = 0, offsetY = 0;
    private float paper_offsetX = 0, paper_offsetY = 0, paper_width = 0, paper_height = 0;

    private MetadataCtrl metadataCtrl = MetadataCtrl.getInstance();

    private ZoomFitType mZoomFitType = ZoomFitType.FIT_SCREEN;

    private ArrayList<Mark> marks;

    public SampleView(Context context) {
        super(context);
        marks = new ArrayList<>();
    }

    public void setPage(float width, float height, float dx, float dy, String backImagePath) {

        if (getWidth() <= 0 || getHeight() <= 0 || width <= 0 || height <= 0) {
            return;
        }

        paper_offsetX = dx;
        paper_offsetY = dy;
        paper_width = width;
        paper_height = height;

        float width_ratio = getWidth() / paper_width;
        float height_ratio = getHeight() / paper_height;

        if (mZoomFitType == ZoomFitType.FIT_SCREEN) {
            paper_scale = Math.min(width_ratio, height_ratio);
        } else if (mZoomFitType == ZoomFitType.FIT_WIDTH)
            paper_scale = width_ratio;
        else
            paper_scale = height_ratio;

        int docWidth = (int) (paper_width * paper_scale);
        int docHeight = (int) (paper_height * paper_scale);

        int mw = getWidth() - docWidth;
        int mh = getHeight() - docHeight;

        if (mZoomFitType == ZoomFitType.FIT_SCREEN) {
            offsetX = mw / 2;
            offsetY = mh / 2;
        } else {
            offsetX = 0;
            offsetY = 0;
        }

        Bitmap temp_pdf3 = BitmapFactory.decodeFile(backImagePath);
        background = Bitmap.createScaledBitmap(temp_pdf3, docWidth, docHeight, true);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.canvas = canvas;
        float screen_offset_x = -paper_offsetX * paper_scale;
        float screen_offset_y = -paper_offsetY * paper_scale;

        if (background == null) {
            canvas.drawColor(Color.WHITE);
        } else {
            int zoom_w = (int) (paper_width * paper_scale);
            int zoom_h = (int) (paper_height * paper_scale);
            float ofx = offsetX / 2;

            Rect source = new Rect(0, 0, background.getWidth(), background.getHeight());
            RectF target = new RectF(offsetX, offsetY, ofx + zoom_w, offsetY + zoom_h);
            RectF target2 = new RectF(offsetX + ofx, offsetY, offsetX + zoom_w, offsetY + zoom_h);
            canvas.drawBitmap(background, source, target, null);
            canvas.drawBitmap(background, source, target2, null);
        }

        if (strokes != null && strokes.size() > 0) {
            Renderer.draw(canvas, strokes.toArray(new Stroke[0]), paper_scale,
                    screen_offset_x + offsetX, screen_offset_y + offsetY, Stroke.STROKE_TYPE_NORMAL);
        }


        if (!marks.isEmpty()) {
            for (int i = 0; i < marks.size(); i++) {

                canvas.drawBitmap(marks.get(i).getBitmap(),
                        marks.get(i).getLeft() * (paper_scale) + screen_offset_x + offsetX,
                        marks.get(i).getTop() * (paper_scale) + screen_offset_y + offsetY,
                        null);
            }
        }
    }

    //TODO:

    public void addDot(String penAddress, Dot dot) {

        if (this.sectionId != dot.sectionId || this.ownerId != dot.ownerId || this.noteId != dot.noteId || this.pageId != dot.pageId) {
            strokes = new ArrayList<>();

            this.sectionId = dot.sectionId;
            this.ownerId = dot.ownerId;
            this.noteId = dot.noteId;
            this.pageId = dot.pageId;
        }

        if (DotType.isPenActionDown(dot.dotType) || mapStroke.get(penAddress) == null || mapStroke.get(penAddress).isReadOnly()) {
            mapStroke.put(penAddress, new Stroke(sectionId, ownerId, noteId, pageId, dot.color));
            strokes.add(mapStroke.get(penAddress));
        }

        mapStroke.get(penAddress).add(dot);

        invalidate();
    }

    public void addStrokes(String penAddress, Stroke[] strs) {
        strokes.addAll(Arrays.asList(strs));
        invalidate();
    }

    public void addMarks(ArrayList<Mark> marks) {
        this.marks = marks;
        invalidate();
    }

    public void changePage(int sectionId, int ownerId, int noteId, int pageId) {
        strokes.clear();
        marks.clear();

        float width = metadataCtrl.getPageWidth(noteId, pageId);
        float height = metadataCtrl.getPageHeight(noteId, pageId);

        float dx = metadataCtrl.getPageMarginLeft(noteId, pageId);
        float dy = metadataCtrl.getPageMarginRight(noteId, pageId);

//        String imagePath = String.format(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SamplePen/KME001_save/%04d.jpg", pageId + 1);
//
//        File file = new File(imagePath);
//
//        if (!file.exists()) {
//            imagePath = String.format(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SamplePen/KME001/%04d.jpg", pageId + 1);
//        }

        String imagePath = String.format(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SamplePen/KME001/%04d.jpg", pageId + 1);

        setPage(595f * 0.14880952F, 838f * 0.14880952F, 0f, 0f, imagePath);
//        setPage(getWidth(), getHeight(), dx, dy, imagePath);
        invalidate();
    }

    public void saveImage(int pageNum) {
        try {
            setDrawingCacheEnabled(true);
            Bitmap bitmap = getDrawingCache();
            File f = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory(), "/SamplePen/KME001_save/");
                if (!file.exists()) {
                    file.mkdirs();
                }
                f = new File(String.format(file.getAbsolutePath() + "/%04d.jpg", pageNum + 1));
            }
            FileOutputStream ostream = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, ostream);
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

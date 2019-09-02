package com.android.samplesmartpen.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

import com.android.samplesmartpen.utilities.Const;
import com.android.samplesmartpen.utilities.Const.Broadcast;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import kr.neolab.sdk.ink.structure.Dot;
import kr.neolab.sdk.ink.structure.DotType;
import kr.neolab.sdk.ink.structure.Stroke;
import kr.neolab.sdk.metadata.IMetadataCtrl;
import kr.neolab.sdk.metadata.MetadataCtrl;
import kr.neolab.sdk.metadata.structure.Symbol;
import kr.neolab.sdk.pen.IPenCtrl;
import kr.neolab.sdk.pen.PenCtrl;
import kr.neolab.sdk.pen.penmsg.IOfflineDataListener;
import kr.neolab.sdk.pen.penmsg.IPenDotListener;
import kr.neolab.sdk.util.NLog;

public class NeoSampleService extends Service {

    public static Boolean SAVE_QUE_DATA_PLAN_B = false;

    private IPenCtrl mPenCtrl;
    private Queue<Dot> mDotQueueForDB = null;
    private Queue<DotWithAddress> mDotQueueForBroadcast = null;
    private DotConsumerForDBThread mDBThread = null;
    private DotConsumerForBroadcastThread mBroadcastThread = null;

    private int curSectionId, curOwnerId, curBookcodeId, curPageNumber;

    //	private PageInfo currentPageInfo = null;
//	private String currentSaveTag = "";
//	private int currentPageNumber = -1;
    private boolean ready = false;

    private Dot checkSymbolDownDot = null;
    ArrayList<Symbol> checkSymbols = new ArrayList<>();

    IMetadataCtrl metadataCtrl;

    @Override
    public void onCreate() {
        super.onCreate();

        mPenCtrl = PenCtrl.getInstance();
        mPenCtrl.setDotListener(mPenReceiveDotListener);

        if (mPenCtrl.getOffLineDataListener() != null)
            mPenCtrl.setOffLineDataListener(null);

        mPenCtrl.setOffLineDataListener(mOfflineDataListener);

        metadataCtrl = MetadataCtrl.getInstance();

//        metadataCtrl.loadFiles(Const.SAMPLE_FOLDER_PATH);

        mDotQueueForDB = new ConcurrentLinkedQueue<>();
        mDotQueueForBroadcast = new ConcurrentLinkedQueue<>();

        mDBThread = new DotConsumerForDBThread(NeoSampleService.this);
        mDBThread.setDaemon(true);
        mDBThread.start();

        mBroadcastThread = new DotConsumerForBroadcastThread();
        mBroadcastThread.setDaemon(true);
        mBroadcastThread.start();

        NLog.d("Service Initialize complete");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NLog.d("onDestroy");

        if (mDBThread != null)
            mDBThread.interrupt();
        if (mBroadcastThread != null)
            mBroadcastThread.interrupt();
//		unRegisterBroadcastReceiver( );

//		android.os.Process.killProcess( android.os.Process.myPid() );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NLog.d("onStartCommand: " + startId);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        NLog.d("onBind - service binding");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        NLog.d("onRebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        NLog.d("onUnbind");
        return super.onUnbind(intent);
    }

    private void enqueueDotForDB(Dot dot) {
        mDotQueueForDB.offer(dot);

        synchronized (mDotQueueForDB) {
            mDotQueueForDB.notifyAll();
        }
    }

    private void enqueueDotForBroadcast(String macAddress, Dot dot) {

        mDotQueueForBroadcast.offer(new DotWithAddress(macAddress, dot));

        synchronized (mDotQueueForBroadcast) {
            mDotQueueForBroadcast.notifyAll();
        }
    }

    private void broadcastDot(String macAddress, Dot dot) {
        Intent intent = new Intent(Broadcast.ACTION_PEN_DOT);
        intent.putExtra(Broadcast.PEN_ADDRESS, macAddress);
        intent.putExtra(Broadcast.EXTRA_DOT, dot);
        sendBroadcast(intent);
    }

    private void sendBroadcastIfPageChanged(int sectionId, int ownerId, int bookcodeId, int pageNumber) {

        if (curSectionId != sectionId || curOwnerId != ownerId || curBookcodeId != bookcodeId) {
            curSectionId = sectionId;
            curOwnerId = ownerId;
            curBookcodeId = bookcodeId;

            curPageNumber = -1;
            curBookcodeId = bookcodeId;
        }

        if (curPageNumber != pageNumber) {
            curPageNumber = pageNumber;
//			currentPageInfo = ActionController.getInstance( this ).getPageInfo( sectionId, ownerId, bookcodeId, pageNumber);
            sendPageChangedBroadcast();
            ready = false;
            NLog.d("sendBroadcastIfPageChanged ready=" + ready);
        }
    }

    private void checkSymbol(Dot dot) {

        if (dot.penTipType != Stroke.PEN_TIP_TYPE_NORMAL)
            return;

        if (DotType.isPenActionDown(dot.dotType)) {
            checkSymbolDownDot = dot;
        } else if (DotType.isPenActionUp(dot.dotType)) {

            if (metadataCtrl != null && checkSymbolDownDot != null) {
                Symbol[] upSymbols = metadataCtrl.findApplicableSymbols(dot.noteId, dot.pageId, dot.x, dot.y);
                Symbol[] downSymbols = metadataCtrl.findApplicableSymbols(checkSymbolDownDot.noteId, checkSymbolDownDot.pageId, checkSymbolDownDot.x, checkSymbolDownDot.y);
                checkSymbolDownDot = null;
                checkSymbols.clear();

                if (upSymbols == null && downSymbols == null)
                    return;
                for (Symbol upSymbol : upSymbols) {
                    for (Symbol downSymbol : downSymbols) {
                        if (upSymbol.getId().equals(downSymbol.getId())) {
                            checkSymbols.add(upSymbol);
                            break;
                        }
                    }
                }

                for (Symbol symbol : checkSymbols) {
                    Intent intent = new Intent(Broadcast.ACTION_SYMBOL_ACTION);
                    intent.putExtra(Broadcast.EXTRA_SECTION_ID, curSectionId);
                    intent.putExtra(Broadcast.EXTRA_OWNER_ID, curOwnerId);
                    intent.putExtra(Broadcast.EXTRA_BOOKCODE_ID, curBookcodeId);
                    intent.putExtra(Broadcast.EXTRA_PAGE_NUMBER, curPageNumber);
                    intent.putExtra(Broadcast.EXTRA_SYMBOL_ID, symbol.getId());
                    sendBroadcast(intent);
                }
            }
        }
    }

    private IPenDotListener mPenReceiveDotListener = (macAddress, dot) -> {

        NLog.d("NeoSampleService onReceiveDot mac_address=" + macAddress + "dotType=" + dot.dotType + " ,pressure=" + dot.pressure + ",x=" + dot.getX() + ",y=" + dot.getY());

        if (DotType.isPenActionDown(dot.getDotType())) {
            sendBroadcastIfPageChanged(dot.sectionId, dot.ownerId, dot.noteId, dot.pageId);
        }

        enqueueDotForDB(dot);
        enqueueDotForBroadcast(macAddress, dot);
    };

    private IOfflineDataListener mOfflineDataListener = (macAddress, strokes, sectionId, ownerId, noteId) -> {
        // 도트가 0인 데이터 필터링
        ArrayList<Stroke> newArrayList = new ArrayList<Stroke>();
        for (Stroke s : strokes) {
            if (s.size() > 0) {
                NLog.d("onReceiveOfflineStrokes s sectionId=" + s.sectionId + " ownerId=" + s.ownerId + " noteId=" + s.noteId + " pageId=" + s.pageId);
                s.color = Color.BLACK;
                newArrayList.add(s);
            } else {
                NLog.e("onReceiveOfflineStrokes Dot size =0!!");
            }
        }
        Intent i = new Intent(Broadcast.ACTION_OFFLINE_STROKES);
        i.putExtra(Broadcast.PEN_ADDRESS, macAddress);
        i.putExtra(Broadcast.EXTRA_OFFLINE_STROKES, newArrayList.toArray(new Stroke[newArrayList.size()]));
        getApplicationContext().sendBroadcast(i);

        // DB 에 insert
    };

    private void sendPageChangedBroadcast() {

        NLog.i("Page Changed-> curSectionId:" + curSectionId + " curOwnerId:" + curOwnerId + " curBookcodeId:" + curBookcodeId + " curPageNumber:" + curPageNumber);

        Intent intent = new Intent(Broadcast.ACTION_WRITE_PAGE_CHANGED);
        intent.putExtra(Broadcast.EXTRA_SECTION_ID, curSectionId);
        intent.putExtra(Broadcast.EXTRA_OWNER_ID, curOwnerId);
        intent.putExtra(Broadcast.EXTRA_BOOKCODE_ID, curBookcodeId);
        intent.putExtra(Broadcast.EXTRA_PAGE_NUMBER, curPageNumber);

        sendBroadcast(intent);
    }

    private final class DotConsumerForDBThread extends Thread {
        private int dotCount = 0;
        private int eraserDotCount = 0;

        private int currentSectionId = -1;
        private int currentOwnerId = -1;
        private int currentBookcodeId = -1;
        private int currentPageId = -1;
        private ArrayList<Dot> dotArray = new ArrayList<>(100);

//		private ArrayList<Dot> eraserDotArray = new ArrayList<Dot>(100);

        DotConsumerForDBThread(Context context) {
            super();
        }

        @Override
        public void run() {

            setName(this.getClass().getSimpleName());

            while (true) {

                while (!mDotQueueForDB.isEmpty()) {
                    Dot dot = null;
//					if(ready)
//					{
                    dot = mDotQueueForDB.poll();
//					}
//					else
//					{
//						break;
//					}
                    if (dot != null)
                        insert(dot); // offline data 전송중이면 Symbol 무시
                }

                try {
                    synchronized (mDotQueueForDB) {
                        mDotQueueForDB.wait();
                    }
                } catch (InterruptedException e) {
//					e.printStackTrace();
                    NLog.d("DotConsumerThread Interrupted!!" + e);
                }
            }
        }

        void insert(Dot dot) {
            checkNotebookAndPageChange(dot);

            if (DotType.isPenActionDown(dot.dotType)) {
                checkNotebookAndPageChange(dot);
                if (dot.penTipType == Stroke.PEN_TIP_TYPE_NORMAL) {
                    dotArray.add(dot);
                    dotCount++;
                }
            }
            // dot middle, adding to stroke
            else if (DotType.isPenActionMove(dot.dotType)) {

                // 스트로크 시작 dot 가 없는데 move dot 이 들어오면
                // 시작 dot 을 추가
                if (dot.penTipType == Stroke.PEN_TIP_TYPE_NORMAL) {
                    if (dotCount == 0) {
                        Dot startDot = new Dot(dot.x, dot.y, dot.pressure, DotType.PEN_ACTION_DOWN.getValue(), dot.timestamp, dot.sectionId, dot.ownerId, dot.noteId, dot.pageId, dot.color, dot.penTipType, dot.tiltX, dot.tiltY, dot.twist);
                        insert(startDot);
                    }
                    dotArray.add(dot);
                    dotCount++;
                }
            }
            // dot end, insert to db and then flush data
            else if (DotType.isPenActionUp(dot.dotType)) {

                if (dot.penTipType == Stroke.PEN_TIP_TYPE_NORMAL) {
                    dotArray.add(dot);
                    dotCount++;
                    insertStrokeDotsArray(dotArray, false);
                }
            }
        }

//		private int currentSectionId = -1;
//		private int currentOwnerId = -1;
//		private int currentBookcodeId = -1;
//		private int currentPageId = -1;

        private void checkNotebookAndPageChange(Dot dot) {
            boolean changed = false;
            if (currentSectionId != dot.sectionId || currentOwnerId != dot.ownerId || currentBookcodeId != dot.noteId || currentPageId != dot.pageId) {
                currentSectionId = dot.sectionId;
                currentOwnerId = dot.ownerId;
                currentBookcodeId = dot.noteId;
                currentPageId = dot.pageId;
                changed = true;
            }

            if (changed) {
                if (dotArray.size() > 0) {
                    Dot lastDot = dotArray.get(dotArray.size() - 1);
                    //makeUpDot
                    Dot upDot = new Dot(lastDot.x, lastDot.y, lastDot.pressure, DotType.PEN_ACTION_UP.getValue(), lastDot.timestamp, lastDot.sectionId, lastDot.ownerId, lastDot.noteId, lastDot.pageId, lastDot.color, lastDot.penTipType, lastDot.tiltX, lastDot.tiltY, lastDot.twist);
                    dotArray.add(upDot);
                    insertStrokeDotsArray(dotArray, false);
                }
            }
        }

        private void insertStrokeDotsArray(ArrayList<Dot> dotArray, boolean isEraser) {
            Stroke s = null;
            for (Dot d : dotArray) {
                if (s == null) {
                    s = new Stroke(d.sectionId, d.ownerId, d.noteId, d.pageId, Color.BLACK);
                    s.penTipType = d.penTipType;
                    if (s.penTipType == Stroke.PEN_TIP_TYPE_NORMAL)
                        s.thickness = 1;
                }
                s.add(d);
            }
            dotArray.clear();
            if (isEraser)
                eraserDotCount = 0;
            else
                dotCount = 0;
            //DB Insert
//			mActionController.addStroke( s ,currentSaveTag);
        }

    }

    private final class DotConsumerForBroadcastThread extends Thread {

        @Override
        public void run() {

            setName(this.getClass().getSimpleName());

            do {

                while (!mDotQueueForBroadcast.isEmpty()) {
                    Dot dot = null;
                    DotWithAddress dotWithAddress = mDotQueueForBroadcast.poll();
                    dot = dotWithAddress.dot;

                    if (dot != null) {
                        broadcastDot(dotWithAddress.address, dot);
                    }
                }

                try {
                    synchronized (mDotQueueForBroadcast) {
                        mDotQueueForBroadcast.wait();
                    }

                } catch (InterruptedException e) {
                    NLog.d("DotConsumerThread Interrupted!!" + e);
                }

            } while (true);
        }
    }

    private class DotWithAddress {
        Dot dot;
        String address;

        DotWithAddress(String address, Dot dot) {
            this.dot = dot;
            this.address = address;
        }
    }
}

package com.android.samplesmartpen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.android.samplesmartpen.utilities.Const;
import com.android.samplesmartpen.utilities.PenClientCtrl;
import com.android.samplesmartpen.utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.neolab.sdk.pen.penmsg.PenMsgType;
import kr.neolab.sdk.util.NLog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_BT_PAIRING = 0x1;

    private SampleViewModel model;
    private PenClientCtrl penClientCtrl;

    public interface OnBackPressed {
        void onBackPressed();
    }

    OnBackPressed onBackPressed;

    public void setOnBackPressed(OnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        penClientCtrl = PenClientCtrl.getInstance(this);
        model = ViewModelProviders.of(this).get(SampleViewModel.class);
        model.getAddress().observe(this, s -> {
            //TODO: naa diri ang address sa bluetooth
            penClientCtrl.connect(s);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.Broadcast.ACTION_PEN_MESSAGE);

        registerReceiver(mPenMsgReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPenMsgReceiver);
    }

    @Override
    public void onBackPressed() {
        onBackPressed.onBackPressed();
    }

    private void handleMsg(String penAddress, int penMsgType, String content) {
        Log.d(TAG, "penAddress=" + penAddress + ",handleMsg : " + penMsgType);

        switch (penMsgType) {
            // Message of the attempt to connect a pen
            case PenMsgType.PEN_CONNECTION_TRY:
                Util.showToast(this, "try to connect.");
                break;

            // Pens when the connection is completed (state certification process is not yet in progress)
            case PenMsgType.PEN_CONNECTION_SUCCESS:
                Util.showToast(this, "connection is successful.");
                penClientCtrl.inputPassword("1234");
                break;

            case PenMsgType.PEN_AUTHORIZED:
                // OffLine Data set use
                penClientCtrl.setAllowOfflineData(true);
                Util.showToast(this, "connection is AUTHORIZED.");
                break;

            // Message when a connection attempt is unsuccessful pen
            case PenMsgType.PEN_CONNECTION_FAILURE:
                Util.showToast(this, "connection has failed.");
                break;


            case PenMsgType.PEN_CONNECTION_FAILURE_BTDUPLICATE:
                String connected_Appname = "";
                try {
                    JSONObject job = new JSONObject(content);

                    connected_Appname = job.getString("packageName");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Util.showToast(this, String.format("The pen is currently connected to %s app. If you want to proceed, please disconnect the pen from %s app.", connected_Appname, connected_Appname));
                break;

            // When you are connected and disconnected from the state pen
            case PenMsgType.PEN_DISCONNECTED:
                Util.showToast(this, "connection has been terminated.");
                break;

            // Pen transmits the state when the firmware update is processed.
            case PenMsgType.PEN_FW_UPGRADE_STATUS:
            case PenMsgType.PEN_FW_UPGRADE_SUCCESS:
            case PenMsgType.PEN_FW_UPGRADE_FAILURE:
            case PenMsgType.PEN_FW_UPGRADE_SUSPEND: {

            }
            break;

            // Offline Data List response of the pen
            case PenMsgType.OFFLINE_DATA_NOTE_LIST:

                try {
                    JSONArray list = new JSONArray(content);

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject jobj = list.getJSONObject(i);

                        int sectionId = jobj.getInt(Const.JsonTag.INT_SECTION_ID);
                        int ownerId = jobj.getInt(Const.JsonTag.INT_OWNER_ID);
                        int noteId = jobj.getInt(Const.JsonTag.INT_NOTE_ID);
                        NLog.d(TAG, "offline(" + (i + 1) + ") note => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // if you want to get offline data of pen, use this function.
                // you can call this function, after complete download.
                //
                break;

            // Messages for offline data transfer begins
            case PenMsgType.OFFLINE_DATA_SEND_START:

                break;

            // Offline data transfer completion
            case PenMsgType.OFFLINE_DATA_SEND_SUCCESS:

                if (penClientCtrl.getProtocolVersion() == 1)
//                    parseOfflineData(penAddress);


                    break;

                // Offline data transfer failure
            case PenMsgType.OFFLINE_DATA_SEND_FAILURE:

                break;

            // Progress of the data transfer process offline
            case PenMsgType.OFFLINE_DATA_SEND_STATUS: {
                try {
                    JSONObject job = new JSONObject(content);

                    int total = job.getInt(Const.JsonTag.INT_TOTAL_SIZE);
                    int received = job.getInt(Const.JsonTag.INT_RECEIVED_SIZE);

                    Log.d(TAG, "offline data send status => total : " + total + ", progress : " + received);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;

            // When the file transfer process of the download offline
            case PenMsgType.OFFLINE_DATA_FILE_CREATED: {
                try {
                    JSONObject job = new JSONObject(content);

                    int sectionId = job.getInt(Const.JsonTag.INT_SECTION_ID);
                    int ownerId = job.getInt(Const.JsonTag.INT_OWNER_ID);
                    int noteId = job.getInt(Const.JsonTag.INT_NOTE_ID);
                    int pageId = job.getInt(Const.JsonTag.INT_PAGE_ID);

                    String filePath = job.getString(Const.JsonTag.STRING_FILE_PATH);

                    Log.d(TAG, "offline data file created => sectionId : " + sectionId + ", ownerId : " + ownerId + ", noteId : " + noteId + ", pageId : " + pageId + " filePath : " + filePath);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;

        }
    }

    private BroadcastReceiver mPenMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(Const.Broadcast.ACTION_PEN_MESSAGE)) {
                String penAddress = intent.getStringExtra(Const.Broadcast.PEN_ADDRESS);
                int penMsgType = intent.getIntExtra(Const.Broadcast.MESSAGE_TYPE, 0);
                String content = intent.getStringExtra(Const.Broadcast.CONTENT);

                handleMsg(penAddress, penMsgType, content);
            }
        }
    };


}

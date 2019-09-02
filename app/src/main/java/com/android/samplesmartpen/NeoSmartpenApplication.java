package com.android.samplesmartpen;

import android.app.Application;

import com.android.samplesmartpen.utilities.PenClientCtrl;
import com.android.samplesmartpen.certificate.MyCertificate;
import com.myscript.iink.Engine;

public class NeoSmartpenApplication extends Application {

    private static Engine engine;

    @Override
    public void onCreate() {
        super.onCreate();
        PenClientCtrl.getInstance(getApplicationContext());
        PenClientCtrl.getInstance(getApplicationContext()).setContext(getApplicationContext());
        PenClientCtrl.getInstance(getApplicationContext()).registerBroadcastBTDuplicate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PenClientCtrl.getInstance(getApplicationContext()).unregisterBroadcastBTDuplicate();
    }

    public static synchronized Engine getEngine()
    {
        if (engine == null)
        {
            engine = Engine.create(MyCertificate.getBytes());
        }

        return engine;
    }
}

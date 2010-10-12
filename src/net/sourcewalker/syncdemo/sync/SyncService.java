package net.sourcewalker.syncdemo.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {

    private NumbersSyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        syncAdapter = new NumbersSyncAdapter(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return syncAdapter.getSyncAdapterBinder();
    }

}

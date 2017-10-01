package ro.softspot.copycat.service.clipboard;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import ro.softspot.copycat.ClipboardItem;
import ro.softspot.copycat.service.sync.SynchronizationService;
import ro.softspot.copycat.service.sync.SynchronizationService.ConnectionStatusListener;
import ro.softspot.copycat.service.sync.SynchronizationService.SynchronizationListener;

/**
 * Created by victor on 12/2/16.
 */

public class ClipboardMonitorService extends Service {
    public static final String TAG = "ClipboardMonitor";
    private final int DFLT_MAX_CLIPBOARD_ITEMS = 5;
    private ClipboardManager mClipboardManager;
    private ClipChangeListener mOnPrimaryClipChangedListener = new ClipChangeListener();

    private LocalBinder localBinder = new LocalBinder();
    private ClipboardMonitorListModel clipboardList;


    @Override
    public void onCreate() {
        super.onCreate();
        clipboardList = new ClipboardMonitorListModel(this, DFLT_MAX_CLIPBOARD_ITEMS);
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        registerForRemoteClipoardUpdate();
        Log.d(TAG, "started clipboard manager service " + this.hashCode());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        }

        Log.d(TAG, "stopping clipboard manager service " + this.hashCode());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public List<ClipboardItem> retrieveClipboardItems() {
        return clipboardList;
    }


    private void registerForRemoteClipoardUpdate() {
        SynchronizationService.getInstance(this).registerForUpdates(new SynchronizationListener() {
            @Override
            public void newItem(ClipboardItem item) {
                ClipData data = new ClipData("incoming||" + item.getSource(), new String[]{"text/plain"}, new ClipData.Item(item.getText()));
                mClipboardManager.setPrimaryClip(data);
            }
        });
    }


    /**
     * Clipboard event listener. Will call #onPrimaryClipChanged every time a user copies to
     * cliboard
     */
    private class ClipChangeListener implements ClipboardManager.OnPrimaryClipChangedListener {

        @Override
        public synchronized void onPrimaryClipChanged() {
            ClipData clip = mClipboardManager.getPrimaryClip();
            if (!clipboardList.addFirst(clip)) {
                return; // item is duplicate or null and shouldn't be added
            }

            CharSequence label = clip.getDescription().getLabel();
            if (label == null || !label.toString().startsWith("incoming")) {
                SynchronizationService.getInstance(ClipboardMonitorService.this).sync(clipboardList.getFirst());
            }

            broadcastClipboardItemAdded();
        }

    }

    private void broadcastClipboardItemAdded() {
        Intent intent = new Intent(TAG);
        sendBroadcast(intent);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ClipboardMonitorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ClipboardMonitorService.this;
        }
    }

}
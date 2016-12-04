package ro.softspot.copycat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ro.softspot.copycat.service.ClipboardMonitorService;

public class MainActivity extends AppCompatActivity {

    public static final String REFRESH_DATA_INTENT = "refresh_data";
    private String TAG = "Main";
    private ClipItemsListAdapter adapter;
    private ClipboardMonitorService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initClipContentList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, ClipboardMonitorService.class);
        Log.d(TAG, "Binding service");
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Unbinding service");
        unbindService(mConnection);
    }


    private void initClipContentList() {
        ListView list = (ListView) findViewById(R.id.clip_list);
        adapter = new ClipItemsListAdapter(this, new ArrayList<ClipboardItem>());
        list.setAdapter(adapter);
        list.setOnItemClickListener(mMessageClickedHandler);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ClipboardMonitorService.LocalBinder binder = (ClipboardMonitorService.LocalBinder) service;
            mService = binder.getService();
            Log.d(TAG, "connected to service " + className.getClassName());

            List<ClipboardItem> clipItems = mService.retrieveClipboardItems();
            adapter.clearItems();
            for (ClipboardItem item : clipItems) {
                adapter.add(item);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "disconnected from service " + arg0.getClassName());
        }
    };


    private AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            Log.d(TAG, "Clicked on position " + position + " and id " + id);
            SyncAlertDialog dialog = new SyncAlertDialog(MainActivity.this , adapter.getItem(position),adapter);
            dialog.show();
        }
    };
}

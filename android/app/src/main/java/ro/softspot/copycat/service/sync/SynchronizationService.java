package ro.softspot.copycat.service.sync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Manager;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import ro.softspot.copycat.ClipboardItem;

import static android.content.Context.MODE_PRIVATE;
import static com.android.volley.Request.Method.POST;

/**
 * Created by victor on 12/4/16.
 */

public class SynchronizationService {

    private static final String ENDPOINT_URL = "http://192.168.0.141:3000";
    private static final String TAG = "SyncService";
    private static SynchronizationService instance;
    private Manager manager;
    private Socket socket;

    private SynchronizationListener listener;


    private SynchronizationService(Context ctxt) {

        try {
            manager = new Manager(new URI(ENDPOINT_URL));

            // attempt to initialize channel from Login Holder
            SharedPreferences prefs = ctxt.getSharedPreferences("UserDetails", MODE_PRIVATE);
            final String currentAccessToken = prefs.getString("user", null);

            if (currentAccessToken != null) {
                socket = manager.socket("/" + currentAccessToken);
                socket.connect();
            } else {
                throw new IllegalStateException("Should not initialize Sync Service" +
                        " without succesful login and persistence of id ");
            }
        } catch (URISyntaxException e) {
            Log.e(TAG, "Cannot parse URL ", e);
        }

    }

    public void sync(final ClipboardItem item) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                socket.emit("message", item.getText(), Build.MODEL);
            }
        });
    }

    public void registerForUpdates(SynchronizationListener listener) {
        this.listener = listener;
        socket.off();
        socket.on("message",new NamespaceMsgListener());
    }


    public void newClient(final Context context, final String id) {
        try {
            JSONObject object = new JSONObject();
            object.put("id", id);
            RequestQueue queue = Volley.newRequestQueue(context);

            // Request a string response from the provided URL.
            JsonObjectRequest stringRequest = new JsonObjectRequest(POST, ENDPOINT_URL + "/channel", object,
                    new Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            socket.off();
                            socket.on("message",new NamespaceMsgListener());
                            Log.i(TAG, "Succesfully registered channel " + id);
                        }
                    },
                    new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                            alertDialog.setTitle("Error ");
                            alertDialog.setMessage("Cannot contact server !");
                            alertDialog.create().show();
                            Log.e(TAG, "Failed to register " + id + ".");
                        }
                    });


            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } catch (JSONException jse) {
            Log.e(TAG, "JSON Exception while building request for new client ", jse);
        }
    }


    private class NamespaceMsgListener implements Emitter.Listener {

        @Override
        public void call(final Object... args) {
            if (listener == null) {
                return;
            }
            if (args.length > 0) {
                Log.d(TAG, "Received message " + args[0] + ".");

                String message = args[0].toString();
                ClipboardItem item = new ClipboardItem(message);
                if (args.length > 1) {
                    item.setSource(args[1].toString());
                }
                listener.newItem(item);
            }
        }
    }

    public interface SynchronizationListener {
        public void newItem(ClipboardItem item);
    }


    public static SynchronizationService getInstance(Context ctxt) {

        if (instance == null) {
            instance = new SynchronizationService(ctxt);
        }
        return instance;
    }

}

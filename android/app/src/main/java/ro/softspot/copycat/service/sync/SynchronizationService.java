package ro.softspot.copycat.service.sync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.GraphRequest;
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
import ro.softspot.copycat.login.AlertDialogActivity;
import ro.softspot.copycat.login.LoginActivity;

import static android.content.Context.MODE_PRIVATE;
import static com.android.volley.Request.Method.POST;

/**
 * Created by victor on 12/4/16.
 */

public class SynchronizationService {

    private static final String ENDPOINT_URL = "https://infinite-springs-96814.herokuapp.com";
    private static final String TAG = "SyncService";
    private static SynchronizationService instance;
    private String email;
    private Manager manager;
    private Socket socket;

    private SynchronizationListener listener;
    private ConnectionStatusListener statusListener;


    private SynchronizationService(Context ctxt) {

        try {
            manager = new Manager(new URI(ENDPOINT_URL));
            SharedPreferences editor = ctxt.getSharedPreferences("UserInfo", MODE_PRIVATE);
            String email = editor.getString("user", "");
            this.email = email;
            Log.v(TAG, "Creating sync service with socket for nsp " + email);
            socket = manager.socket("/" + email);

            socket.on(Socket.EVENT_CONNECT, new ConnectedMsgListener());
            socket.on(Socket.EVENT_CONNECT_ERROR, new ErrorMsgListener());
            socket.on(Socket.EVENT_ERROR, new ErrorMsgListener());
            socket.on(Socket.EVENT_DISCONNECT, new ErrorMsgListener());

            socket.connect();



        } catch (URISyntaxException e) {
            Log.e(TAG, "Cannot parse URL ", e);
        }

    }

    public static SynchronizationService getInstance(Context ctxt) {

        if (instance == null) {
            instance = new SynchronizationService(ctxt);
        }
        return instance;
    }

    public void sync(final ClipboardItem item) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, " Sending clipped data :" + item.getText());
                socket.emit("message", item.getText(), Build.MODEL);
            }
        });
    }

    public void registerForUpdates(SynchronizationListener listener) {
        this.listener = listener;
        socket.off("message");
        socket.on("message", new NamespaceMsgListener());
    }

    public void newClient(final Context context) {
        try {
            JSONObject object = new JSONObject();
            object.put("id", this.email);
            RequestQueue queue = Volley.newRequestQueue(context);

            // Request a string response from the provided URL.
            JsonObjectRequest stringRequest = new JsonObjectRequest(POST, ENDPOINT_URL + "/channel", object,
                    new Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            socket.off("message");
                            socket.on("message", new NamespaceMsgListener());
                            Log.i(TAG, "Succesfully registered channel " + email);
                        }
                    },
                    new ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Intent i = new Intent(context, AlertDialogActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);
                            Log.e(TAG, "Failed to register " + email + ".");
                        }
                    });


            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } catch (JSONException jse) {
            Log.e(TAG, "JSON Exception while building request for new client ", jse);
        }
    }

    public void registerForConnectionStatus(ConnectionStatusListener connectionStatusListener) {
        this.statusListener = connectionStatusListener;
    }


    /****************************
     * Socket listeners
     * <p>
     * for message, connection and error
     */
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

    private class ConnectedMsgListener implements Emitter.Listener {
        @Override
        public void call(final Object... args) {
            if (statusListener != null) {
                statusListener.connected();
            }
        }
    }

    private class ErrorMsgListener implements Emitter.Listener {
        @Override
        public void call(final Object... args) {
            if (statusListener != null) {
                statusListener.disconnected();
            }

        }
    }

    public interface SynchronizationListener {
        public void newItem(ClipboardItem item);
    }

    public interface ConnectionStatusListener {
        void connected();

        void disconnected();
    }


}

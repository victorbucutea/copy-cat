package ro.softspot.copycat.service;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import ro.softspot.copycat.ClipboardItem;

/**
 * Created by victor on 12/4/16.
 */

public class SynchronizationService {


    private SynchronizationListener listener;

    public void sync(ClipboardItem item) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://192.168.0.1", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }

    public void newItem(String item){
        listener.newItem(new ClipboardItem("X"));
    }


    public void registerForUpdates(SynchronizationListener listener){
        this.listener = listener;
    }

    private interface SynchronizationListener {
        public void newItem(ClipboardItem item);
    }
}

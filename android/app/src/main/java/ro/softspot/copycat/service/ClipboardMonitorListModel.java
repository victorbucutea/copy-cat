package ro.softspot.copycat.service;

import android.app.Service;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import ro.softspot.copycat.ClipboardItem;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by victor on 12/4/16.
 */

public class ClipboardMonitorListModel extends LinkedList<ClipboardItem> {
    private static final String TAG = "ClipboardMonitorList";
    Set<Integer> uniqueStr = new HashSet<>();


    private final int maxSize;
    private final Service service;

    public ClipboardMonitorListModel(Service service, int maxSize) {
        this.maxSize = maxSize;
        this.service = service;
        initFromSharedPrefs();
    }

    public void addFirst(ClipData clip) {

        ClipData.Item itemAt = clip.getItemAt(0);
        if (itemAt == null) {
            return;
        }

        CharSequence text = itemAt.getText();
        if (text == null) {
            return;
        }

        String firstItem = text.toString();

        if (uniqueStr.add(firstItem.hashCode())) {

            addFirst(new ClipboardItem(firstItem));

            if (size() > maxSize) {
                removeLast();// ensure max size is not exceeded
            }

            storeInSharedPrefs();
        }
    }


    private void storeInSharedPrefs() {
        SharedPreferences.Editor editor = service.getSharedPreferences(TAG, MODE_PRIVATE).edit();
        int i = 0;
        for (ClipboardItem s : this) {
            i++;
            Log.d(TAG, "storing to prefs " + i + " - " + s);
            editor.putString("" + i, s.marshall());
        }
        editor.apply();
    }

    private void initFromSharedPrefs() {
        SharedPreferences sharedPrefs = service.getSharedPreferences(TAG, MODE_PRIVATE);

        for (int i = 1; i <= maxSize; i++) {
            String clipItem = sharedPrefs.getString("" + i, null);
            if (clipItem != null) {
                Log.d(TAG, "reading from prefs " + i + " - " + clipItem);
                add(ClipboardItem.unmarshall(clipItem));
            }
        }
    }
}

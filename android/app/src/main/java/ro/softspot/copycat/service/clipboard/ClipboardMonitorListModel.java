package ro.softspot.copycat.service.clipboard;

import android.app.Service;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.commons.lang3.SerializationException;

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

    public boolean addFirst(ClipData clip) {

        ClipData.Item itemAt = clip.getItemAt(0);
        if (itemAt == null) {
            return false;
        }

        CharSequence text = itemAt.getText();
        if (text == null) {
            return false;
        }

        Log.d(TAG, "recording clipped " + text);


        String firstItem = text.toString();

        if (uniqueStr.add(firstItem.hashCode())) {

            ClipboardItem item = new ClipboardItem(firstItem);

            if (clip.getDescription().getLabel() != null) {
                String label = clip.getDescription().getLabel().toString();
                if (label.startsWith("incoming")) {
                    String[] sources = label.split("\\|\\|");
                    item.setSource(sources[1]);
                }
            }

            addFirst(item);

            if (size() > maxSize) {
                removeLast();// ensure max size is not exceeded
            }

            storeInSharedPrefs();
            return true;
        }

        return false;
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
                try {
                    add(ClipboardItem.unmarshall(clipItem));
                } catch (SerializationException se) {
                    Log.e(TAG, "Error while deserializing a clipboardItem", se);
                }
            }
        }
    }

    public boolean isUnique(ClipData clip) {

        ClipData.Item itemAt = clip.getItemAt(0);
        if (itemAt == null) {
            return false;
        }

        CharSequence text = itemAt.getText();
        if (text == null) {
            return false;
        }

        String firstItem = text.toString();

        return !uniqueStr.contains(firstItem.hashCode());
    }
}

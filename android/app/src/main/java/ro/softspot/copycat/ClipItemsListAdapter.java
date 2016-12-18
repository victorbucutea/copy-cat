package ro.softspot.copycat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

/**
 * Created by victor on 12/3/16.
 */

public class ClipItemsListAdapter extends BaseAdapter {

    private final Context ctxt;
    private List<ClipboardItem> items;
    private final LayoutInflater inflater;

    public ClipItemsListAdapter(Context ctxt) {
        this.ctxt = ctxt;
        this.items = new ArrayList<>();
        inflater = (LayoutInflater) ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ClipboardItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    void add(ClipboardItem item) {
        items.add(item);
        notifyDataSetChanged();
    }

    void filter(String text) {
        List<ClipboardItem> tmpItems = new ArrayList<>(items);
        for (ClipboardItem item : tmpItems) {
            if (!containsIgnoreCase(item.getText(),text) && !containsIgnoreCase(item.getDescription(),text) ) {
               items.remove(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View vi = view;

        ClipboardItem item = items.get(position);

        if (vi == null) {
            vi = inflater.inflate(R.layout.row, null);
        }

        TextView text = (TextView) vi.findViewById(R.id.copiedText);
        text.setText(item.getText());

        TextView subtext = (TextView) vi.findViewById(R.id.copiedTextDesc);
        subtext.setText(item.getDescription());

        View syncOkFlag = vi.findViewById(R.id.sync_ok);

        if (item.isSynced()) {
            syncOkFlag.setVisibility(View.VISIBLE);
        } else {
            syncOkFlag.setVisibility(View.INVISIBLE);
        }


        if (!item.isDisplayed()) {
            vi.setVisibility(View.GONE);
        } else {
            vi.setVisibility(View.VISIBLE);
        }

        return vi;
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    public void markNotSynced() {
        for (ClipboardItem item : items) {
            item.setSynced(false);
        }
    }
}

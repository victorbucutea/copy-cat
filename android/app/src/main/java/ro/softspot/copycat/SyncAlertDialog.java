package ro.softspot.copycat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by victor on 12/4/16.
 */

public class SyncAlertDialog {
    private static final String TAG = "SyncAlertDialog";


    private AlertDialog.Builder alertDialog;

    public SyncAlertDialog(final MainActivity mainActivity, final ClipboardItem item, final ClipItemsListAdapter adapter) {
        alertDialog = new AlertDialog.Builder(mainActivity);
        alertDialog.setTitle("Sync ");
        alertDialog.setMessage("Edit text and press 'Send'");

        final EditText input = new EditText(mainActivity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText(item.getText());
        input.setBackgroundColor(mainActivity.getResources().getColor(R.color.lightGray));
        alertDialog.setView(input);
        input.setTextSize(14);
        input.setPadding(15,0,0,15);

        alertDialog.setPositiveButton("Send",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Sending " + input.getText() + " for validation");
                        adapter.markNotSynced();
                        item.setSynced(true);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(mainActivity, "Item synchronized with all devices ! ",Toast.LENGTH_SHORT).show();
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

    }

    public void show(){
        alertDialog.show();
    }

}


package ro.softspot.copycat;

import android.text.format.DateUtils;
import android.util.Base64;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by victor on 12/4/16.
 */

public class ClipboardItem implements Serializable {

    private String text;

    private boolean synced;

    private Date createdAt = new Date();

    private String description;

    public ClipboardItem(String firstItem) {
        this.text = firstItem;
        this.createdAt = new Date();
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return DateUtils.getRelativeTimeSpanString(createdAt.getTime()).toString();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String marshall() {
        byte[] serialize = SerializationUtils.serialize(this);
        return Base64.encodeToString(serialize, Base64.DEFAULT);
    }

    public static ClipboardItem unmarshall(String base64obj) {
        byte[] decode = Base64.decode(base64obj, Base64.DEFAULT);
        return (ClipboardItem) SerializationUtils.deserialize(decode);
    }


}

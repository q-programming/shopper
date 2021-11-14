package pl.qprogramming.shopper.watch.fragments.item;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.model.ListItem;
import pl.qprogramming.shopper.watch.model.ShoppingList;

import static pl.qprogramming.shopper.watch.util.HttpUtil.post;

public class ItemViewAdapter extends ArrayAdapter<ListItem> {
    private static final String TAG = ItemViewAdapter.class.getSimpleName();
    private final ShoppingList list;
    private final Context context;

    public ItemViewAdapter(Context context, ShoppingList list) {
        super(context, R.layout.fragment_item, list.getItems());
        this.context = context;
        list.getItems().add(0, ListItem.builder().name(list.getName()).build());
        list.getItems().add(ListItem.builder().name("").build());
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        val rowView = LayoutInflater.from(context).inflate(R.layout.fragment_item, null, true);
        val checkBox = (CheckBox) rowView.findViewById(R.id.item_checkbox);
        val itemName = (TextView) rowView.findViewById(R.id.item_name);
        //load item
        val item = list.getItems().get(position);
        if (position == 0 || position == list.getItems().size() - 1) {
            checkBox.setVisibility(View.GONE);
            itemName.setText(item.getName());
            itemName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            itemName.setTextAppearance(R.style.Header);
            itemName.setPadding(0, 10, 0, 10);
        } else {
            checkBox.setChecked(item.isDone());
            val quantity = item.getQuantity() > 0 ? NumberFormat.getInstance().format(item.getQuantity()) : "";
            val unit = item.getUnit() != null ? item.getUnit() : "";
            val itemStr = String.format(Locale.ENGLISH, "%s%s %s", quantity, unit, item.getName());
            itemName.setText(itemStr);
            checkBox.setOnClickListener(v -> toggle(item, position, checkBox));
            itemName.setOnClickListener(v -> toggle(item, position, checkBox));
        }
        return rowView;
    }

    private void toggle(ListItem item, int position, CheckBox checkBox) {
        context.sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        val url = MessageFormat.format(context.getString(R.string.toggle_item), list.getId());
        checkBox.setChecked(!item.isDone());
        post(context, url, item, response -> {
            val updatedItem = new Gson().fromJson(response.toString(), ListItem.class);
            list.getItems().set(position, updatedItem);
            checkBox.setChecked(updatedItem.isDone());
            context.sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
        }, error -> {
            Log.d(TAG, "Error while trying to toggle" + error.getCause());
            Toast.makeText(context, R.string.item_toggle_error, Toast.LENGTH_LONG).show();
            context.sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
        });
    }
}
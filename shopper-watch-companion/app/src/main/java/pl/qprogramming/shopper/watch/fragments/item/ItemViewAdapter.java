package pl.qprogramming.shopper.watch.fragments.item;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import androidx.wear.widget.WearableRecyclerView;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.model.ListItem;
import pl.qprogramming.shopper.watch.model.ShoppingList;

import static android.text.TextUtils.isEmpty;
import static pl.qprogramming.shopper.watch.util.HttpUtil.post;
import static pl.qprogramming.shopper.watch.util.Utils.getDescFontSize;
import static pl.qprogramming.shopper.watch.util.Utils.getFontSize;

public class ItemViewAdapter extends WearableRecyclerView.Adapter<ItemViewAdapter.ViewHolder> {
    private static final String TAG = ItemViewAdapter.class.getSimpleName();
    private final ShoppingList list;
    private final Context context;
    private final float size;
    private final float desc_size;

    public ItemViewAdapter(Context context, ShoppingList list) {
        this.context = context;
        this.list = list;
        size = getFontSize(context);
        desc_size = getDescFontSize(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        val item = list.getItems().get(position);
        holder.item = item;
        holder.checkBox.setChecked(item.isDone());
        val quantity = item.getQuantity() > 0 ? NumberFormat.getInstance().format(item.getQuantity()) : "";
        val unit = item.getUnit() != null ? item.getUnit() : "";
        val itemStr = String.format(Locale.ENGLISH, "%s%s %s", quantity, unit, item.getName());
        holder.itemName.setText(itemStr);
        holder.itemName.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        if (!isEmpty(item.getDescription())) {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(item.getDescription());
            holder.description.setTextSize(TypedValue.COMPLEX_UNIT_PX, desc_size);
        } else {
            holder.description.setVisibility(View.GONE);
        }
        holder.checkBox.setOnClickListener(v -> toggle(item, position, holder.checkBox));
        holder.itemName.setOnClickListener(v -> toggle(item, position, holder.checkBox));
    }

    @Override
    public int getItemCount() {
        return list.getItems().size();
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

    public static class ViewHolder extends WearableRecyclerView.ViewHolder {
        public final View mView;
        public final CheckBox checkBox;
        public final TextView itemName;
        public final TextView description;
        public ListItem item;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            checkBox = view.findViewById(R.id.item_checkbox);
            itemName = view.findViewById(R.id.item_name);
            description = view.findViewById(R.id.item_desc);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + itemName.getText() + "'";
        }
    }


}
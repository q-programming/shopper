package pl.qprogramming.shopper.watch.fragments.item;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.model.ListItem;
import pl.qprogramming.shopper.watch.model.ShoppingList;

public class ItemViewAdapter extends RecyclerView.Adapter<ItemViewAdapter.ViewHolder> {

    private final ShoppingList list;

    public ItemViewAdapter(ShoppingList list) {
        this.list = list;
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
    }

    @Override
    public int getItemCount() {
        return list.getItems().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final CheckBox checkBox;
        public final TextView itemName;
        public ListItem item;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            checkBox = view.findViewById(R.id.item_checkbox);
            itemName = view.findViewById(R.id.item_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + itemName.getText() + "'";
        }
    }
}
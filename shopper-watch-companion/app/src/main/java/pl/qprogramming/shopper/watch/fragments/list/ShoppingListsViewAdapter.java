package pl.qprogramming.shopper.watch.fragments.list;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableRecyclerView;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.fragments.item.ItemLayoutFragment;
import pl.qprogramming.shopper.watch.model.ShoppingList;

import static pl.qprogramming.shopper.watch.util.Utils.getFontSize;
import static pl.qprogramming.shopper.watch.util.Utils.navigateToFragment;

public class ShoppingListsViewAdapter extends WearableRecyclerView.Adapter<ShoppingListsViewAdapter.ViewHolder> {

    private final List<ShoppingList> lists;
    private final FragmentManager fManager;
    private final float size;

    public ShoppingListsViewAdapter(List<ShoppingList> items, FragmentActivity activity) {
        fManager = activity.getSupportFragmentManager();
        lists = items;
        size = getFontSize(activity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.list = lists.get(position);
        holder.listName.setText(lists.get(position).getName());
        holder.listName.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
        holder.mView.setOnClickListener(v -> {
            val listID = holder.list.getId();
            navigateToFragment(fManager, new ItemLayoutFragment(listID), "list_" + listID);
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView listName;
        public ShoppingList list;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            listName = view.findViewById(R.id.list_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + listName.getText() + "'";
        }
    }
}
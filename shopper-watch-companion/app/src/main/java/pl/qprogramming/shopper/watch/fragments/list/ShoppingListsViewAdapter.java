package pl.qprogramming.shopper.watch.fragments.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.fragments.item.ItemLayoutFragment;
import pl.qprogramming.shopper.watch.model.ShoppingList;

import static pl.qprogramming.shopper.watch.util.Utils.navigateToFragment;

public class ShoppingListsViewAdapter extends RecyclerView.Adapter<ShoppingListsViewAdapter.ViewHolder> {

    private final List<ShoppingList> lists;
    private final FragmentManager fManager;

    public ShoppingListsViewAdapter(List<ShoppingList> items, FragmentActivity activity) {
        fManager = activity.getSupportFragmentManager();
        lists = items;
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
        holder.mContentView.setText(lists.get(position).getName());
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
        public final TextView mContentView;
        public ShoppingList list;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.list_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
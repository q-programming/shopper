package pl.qprogramming.shopper.watch.fragments.item;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.fragments.list.ListLayoutFragment;
import pl.qprogramming.shopper.watch.model.ShoppingList;

import static pl.qprogramming.shopper.watch.util.HttpUtil.get;

/**
 * A fragment representing a list of Items.
 */
public class ItemLayoutFragment extends Fragment {

    private final long listID;
    private RecyclerView recyclerView;
    private static final String TAG = ListLayoutFragment.class.getSimpleName();

    public ItemLayoutFragment(long id) {
        this.listID = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_layout, container, false);
        Context context = view.getContext();
        recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        getItems();
        return view;
    }


    private void getItems() {
        requireContext().sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        String listUrl = getString(R.string.list_items) + listID;
        get(requireContext(), listUrl, lists -> {
            Log.d(TAG, lists.toString());
            val shoppingLists = new Gson().fromJson(lists.toString(), ShoppingList.class);
            recyclerView.setAdapter(new ItemViewAdapter(shoppingLists));
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
        }, error -> {
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
            Toast.makeText(requireContext(), "There were errors while trying to fetch lists", Toast.LENGTH_LONG).show();
        });
    }
}
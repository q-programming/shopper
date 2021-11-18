package pl.qprogramming.shopper.watch.fragments.item;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.fragment.app.Fragment;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.fragments.list.ListLayoutFragment;
import pl.qprogramming.shopper.watch.model.ShoppingList;
import pl.qprogramming.shopper.watch.util.CustomScrollingLayoutCallback;

import static pl.qprogramming.shopper.watch.util.HttpUtil.get;

/**
 * A fragment representing a list of Items.
 */
public class ItemLayoutFragment extends Fragment {

    private final long listID;
    private WearableRecyclerView recyclerView;
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
        recyclerView = (WearableRecyclerView) view;
        recyclerView.setEdgeItemsCenteringEnabled(true);
        recyclerView.setLayoutManager(new WearableLinearLayoutManager(context, new CustomScrollingLayoutCallback()));
        recyclerView.requestFocus();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getItems();
        val filter = new IntentFilter(EventType.WAKE_UP.getCode());
        requireContext().registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        try {
            requireContext().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Receiver not registered");
        }
        super.onStop();
    }

    private void getItems() {
        requireActivity().sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        String listUrl = getString(R.string.list_items) + listID;
        get(requireContext(), listUrl, lists -> {
            Log.d(TAG, lists.toString());
            val shoppingLists = new Gson().fromJson(lists.toString(), ShoppingList.class);
            recyclerView.setAdapter(new ItemViewAdapter(requireContext(), shoppingLists));
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
        }, error -> {
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
            Toast.makeText(requireContext(), R.string.items_error, Toast.LENGTH_LONG).show();
        });
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getItems();
        }
    };
}
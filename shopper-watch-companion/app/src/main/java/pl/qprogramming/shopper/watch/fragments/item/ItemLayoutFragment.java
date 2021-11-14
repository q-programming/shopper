package pl.qprogramming.shopper.watch.fragments.item;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.fragment.app.Fragment;
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
    private ItemViewAdapter adapter;
    private ListView listView;
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
        View view = inflater.inflate(R.layout.fragment_item_list_layout, container, false);
        listView = view.findViewById(R.id.item_list);
        listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getItems();
    }

    private void getItems() {
        requireActivity().sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        String listUrl = getString(R.string.list_items) + listID;
        get(requireContext(), listUrl, lists -> {
            Log.d(TAG, lists.toString());
            val shoppingLists = new Gson().fromJson(lists.toString(), ShoppingList.class);
            adapter = new ItemViewAdapter(requireContext(), shoppingLists);
            listView.setAdapter(adapter);
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
        }, error -> {
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
            Toast.makeText(requireContext(), R.string.items_error, Toast.LENGTH_LONG).show();
        });
    }
}
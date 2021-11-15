package pl.qprogramming.shopper.watch.fragments.list;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Arrays;

import androidx.fragment.app.Fragment;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.model.ShoppingList;
import pl.qprogramming.shopper.watch.util.CustomScrollingLayoutCallback;

import static pl.qprogramming.shopper.watch.util.HttpUtil.getArray;

/**
 * A fragment representing a list of Items.
 */
public class ListLayoutFragment extends Fragment {

    private static final String TAG = ListLayoutFragment.class.getSimpleName();
    private WearableRecyclerView recyclerView;

    public ListLayoutFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.settings_btn).setVisibility(View.VISIBLE);
        View view = inflater.inflate(R.layout.fragment_list_layout, container, false);
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
        getLists();
    }

    @Override
    public void onDestroyView() {
        requireActivity().findViewById(R.id.settings_btn).setVisibility(View.GONE);
        super.onDestroyView();
    }


    private void getLists() {
        requireActivity().sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        String listUrl = getString(R.string.list_mine);
        getArray(requireContext(), listUrl, lists -> {
            Log.d(TAG, lists.toString());
            val shoppingLists = new Gson().fromJson(lists.toString(), ShoppingList[].class);
            recyclerView.setAdapter(new ShoppingListsViewAdapter(Arrays.asList(shoppingLists), requireActivity()));
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
        }, error -> {
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
            Toast.makeText(requireContext(), R.string.list_error, Toast.LENGTH_LONG).show();
        });
    }
}
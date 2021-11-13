package pl.qprogramming.shopper.watch.fragments.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.config.Properties;
import pl.qprogramming.shopper.watch.fragments.register.RegisterFragment;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static pl.qprogramming.shopper.watch.util.HttpUtil.getArray;
import static pl.qprogramming.shopper.watch.util.HttpUtil.post;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class WelcomeFragment extends Fragment {
    private static final String TAG = WelcomeFragment.class.getSimpleName();
    private String email;

    public WelcomeFragment(String email) {
        this.email = email;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        val textView = (TextView) view.findViewById(R.id.email);
        view.findViewById(R.id.remove_account).setOnClickListener(v -> {
            val sp = getDefaultSharedPreferences(view.getContext());
            val spEdit = sp.edit();
            spEdit.remove(Properties.EMAIL);
            spEdit.remove(Properties.TOKEN);
            spEdit.apply();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .replace(R.id.activity_fragment_layout, new RegisterFragment())
                    .commit();

        });
        textView.setText(email);
        checkWhoAmI();

    }

    private void checkWhoAmI() {
        requireContext().sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        String url = getString(R.string.account_whoami);
        post(requireContext(), url, null,
                response -> getLists(),
                error -> {
                    requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
                    Toast.makeText(requireContext(), "Your device was not yet confirmed", Toast.LENGTH_LONG).show();
                });
    }

    private void getLists() {
        String listUrl = getString(R.string.list_mine);
        getArray(requireContext(), listUrl, lists -> {
            Log.d(TAG, lists.toString());
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
        }, error -> {
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
            Toast.makeText(requireContext(), "There were errors while trying to fetch lists", Toast.LENGTH_LONG).show();
        });
    }
}
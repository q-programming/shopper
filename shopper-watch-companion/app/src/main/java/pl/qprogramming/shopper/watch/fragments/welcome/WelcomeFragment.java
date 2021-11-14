package pl.qprogramming.shopper.watch.fragments.welcome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.Properties;
import pl.qprogramming.shopper.watch.fragments.register.RegisterFragment;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class WelcomeFragment extends Fragment {
    private final String email;

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
    }
}
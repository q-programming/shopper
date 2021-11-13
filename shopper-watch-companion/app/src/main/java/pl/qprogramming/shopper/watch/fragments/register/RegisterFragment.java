package pl.qprogramming.shopper.watch.fragments.register;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.Properties;
import pl.qprogramming.shopper.watch.fragments.welcome.WelcomeFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.prefs.Preferences;

import static android.text.TextUtils.isEmpty;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    public RegisterFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        val registerBtn = view.findViewById(R.id.register_button);
        val emailInput = (EditText) view.findViewById(R.id.email_address);
        emailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                val input = editable.toString();
                if (isValidEmail(input)) {
                    view.findViewById(R.id.email_error).setVisibility(View.GONE);
                    registerBtn.setEnabled(true);
                } else {
                    view.findViewById(R.id.email_error).setVisibility(View.VISIBLE);
                    registerBtn.setEnabled(false);
                }

            }
        });
        registerBtn.setOnClickListener(v -> {
            //TODO call activity to register
            val spEdit = getDefaultSharedPreferences(view.getContext()).edit();
            spEdit.putString(Properties.EMAIL, emailInput.getText().toString());
            spEdit.putString(Properties.TOKEN, "TOKEN!");
            spEdit.apply();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .replace(R.id.activity_fragment_layout, new WelcomeFragment())
                    .commit();
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
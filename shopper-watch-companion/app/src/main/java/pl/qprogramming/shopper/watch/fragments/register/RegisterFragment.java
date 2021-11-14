package pl.qprogramming.shopper.watch.fragments.register;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lombok.SneakyThrows;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.config.Properties;
import pl.qprogramming.shopper.watch.fragments.welcome.WelcomeFragment;
import pl.qprogramming.shopper.watch.model.Device;
import pl.qprogramming.shopper.watch.model.RegisterDevice;

import static android.text.TextUtils.isEmpty;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static pl.qprogramming.shopper.watch.util.HttpUtil.post;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private static final String TAG = RegisterFragment.class.getSimpleName();

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
        val info = (TextView) view.findViewById(R.id.register_info);
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
            val email = emailInput.getText().toString();
            info.setText(getString(R.string.please_wait));
            emailInput.setVisibility(View.GONE);
            registerBtn.setVisibility(View.GONE);
            register(email);
        });
    }

    @SneakyThrows
    private void register(String email) {
        String url = getString(R.string.new_device);
        val device = RegisterDevice.builder().email(email).name(Build.MODEL).build();
        requireContext().sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        post(requireContext(), url, device, response -> {
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
            Toast.makeText(requireContext(), "Successfully registered new device ", Toast.LENGTH_LONG).show();
            val newDevice = new Gson().fromJson(response.toString(), Device.class);
            val spEdit = getDefaultSharedPreferences(requireContext()).edit();
            spEdit.putString(Properties.EMAIL, email);
            spEdit.putString(Properties.TOKEN, newDevice.getPlainKey());
            spEdit.apply();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .replace(R.id.activity_fragment_layout, new WelcomeFragment(email))
                    .commit();
        }, error -> {
            requireContext().sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
            Toast.makeText(requireContext(), getString(R.string.register_error), Toast.LENGTH_LONG).show();
            Log.e(TAG, "error while trying to call " + error);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .replace(R.id.activity_fragment_layout, new RegisterFragment())
                    .commit();
        }, 10000);
    }


    public static boolean isValidEmail(CharSequence target) {
        return (!isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
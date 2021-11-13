package pl.qprogramming.shopper.watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;
import lombok.val;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.config.Properties;
import pl.qprogramming.shopper.watch.fragments.register.RegisterFragment;
import pl.qprogramming.shopper.watch.fragments.welcome.WelcomeFragment;

import static android.text.TextUtils.isEmpty;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AmbientModeSupport.AmbientController controller = AmbientModeSupport.attach(this);
        boolean isAmbient = controller.isAmbient();
        setContentView(R.layout.activity_main);
        setupLoader();
        val sp = getDefaultSharedPreferences(this);
        val email = sp.getString(Properties.EMAIL, null);
        val token = sp.getString(Properties.TOKEN, null);
        Fragment fragment;
        if (isEmpty(email) || isEmpty(token)) {
            fragment = new RegisterFragment();
        } else {
            fragment = new WelcomeFragment(email);
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_fragment_layout, fragment)
                .commit();
        val filter = new IntentFilter(EventType.LOADING_STARTED.getCode());
        filter.addAction(EventType.LOADING_FINISHED.getCode());
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Receiver not registered");
        }
        super.onStop();
    }

    private void setupLoader() {
        loader = findViewById(R.id.loader);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Refresh content!");
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            val event = EventType.getType(intent.getAction());
            switch (event) {
                case LOADING_STARTED:
                    loader.setVisibility(View.VISIBLE);
                    break;
                case LOADING_FINISHED:
                    loader.setVisibility(View.GONE);
                    break;
            }
        }
    };
}
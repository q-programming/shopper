package pl.qprogramming.shopper.watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.BoxInsetLayout;
import lombok.val;
import pl.qprogramming.shopper.watch.config.EventType;
import pl.qprogramming.shopper.watch.config.Properties;
import pl.qprogramming.shopper.watch.fragments.list.ListLayoutFragment;
import pl.qprogramming.shopper.watch.fragments.register.RegisterFragment;
import pl.qprogramming.shopper.watch.fragments.settings.SettingsFragment;
import pl.qprogramming.shopper.watch.fragments.welcome.WaitFragment;
import pl.qprogramming.shopper.watch.fragments.welcome.WelcomeFragment;

import static android.text.TextUtils.isEmpty;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static pl.qprogramming.shopper.watch.util.HttpUtil.post;
import static pl.qprogramming.shopper.watch.util.Utils.navigateToFragment;

public class MainActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar loader;
    private ImageButton settings;
    private BoxInsetLayout mainLayout;
    private AmbientModeSupport.AmbientController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        val sp = getDefaultSharedPreferences(this);
        val aod = sp.getBoolean(Properties.AOD, true);
        if (aod) {
            controller = AmbientModeSupport.attach(this);
        }
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.main_layout);
        val settings = findViewById(R.id.settings_btn);
        settings.setOnClickListener(v ->
                navigateToFragment(getSupportFragmentManager(), new SettingsFragment(), "settings"));
        setupViews();
        val email = sp.getString(Properties.EMAIL, null);
        val token = sp.getString(Properties.TOKEN, null);
        //if not yet registered, just go to register page
        if (isEmpty(email) || isEmpty(token)) {
            loader.setVisibility(View.GONE);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.activity_fragment_layout, new RegisterFragment())
                    .commit();
        } else {
            checkWhoAmI(email);
        }
        val filter = new IntentFilter(EventType.LOADING_STARTED.getCode());
        filter.addAction(EventType.LOADING_FINISHED.getCode());
        registerReceiver(receiver, filter);
    }

    /**
     * Verifies that email and token combination is valid , of something wrong with auth ,
     * force user back to welcome page where there is a way to remove device
     *
     * @param email account email
     */
    private void checkWhoAmI(String email) {
        sendBroadcast(new Intent(EventType.LOADING_STARTED.getCode()));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_fragment_layout, new WaitFragment())
                .commit();
        String url = getString(R.string.account_whoami);
        post(this, url, null,
                response -> getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.activity_fragment_layout, new ListLayoutFragment())
                        .commit(),
                error -> {
                    if (error.networkResponse.statusCode != 423) {
                        Toast.makeText(this, R.string.account_error, Toast.LENGTH_LONG).show();
                    }
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.activity_fragment_layout, new WelcomeFragment(email))
                            .commit();
                    sendBroadcast(new Intent(EventType.LOADING_FINISHED.getCode()));
                }, 10000);
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

    private void setupViews() {
        loader = findViewById(R.id.loader);
        settings = findViewById(R.id.settings_btn);
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new AmbientModeSupport.AmbientCallback() {
            @Override
            public void onEnterAmbient(Bundle ambientDetails) {
                super.onEnterAmbient(ambientDetails);
                mainLayout.setBackgroundColor(Color.BLACK);
                settings.setVisibility(View.GONE);
                loader.setVisibility(View.GONE);
            }

            @Override
            public void onUpdateAmbient() {
                super.onUpdateAmbient();
            }

            @Override
            public void onExitAmbient() {
                super.onExitAmbient();
                mainLayout.setBackgroundColor(getColor(R.color.qprograming_blue));
                sendBroadcast(new Intent(EventType.WAKE_UP.getCode()));
            }

            @Override
            public void onAmbientOffloadInvalidated() {
                super.onAmbientOffloadInvalidated();
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        val filter = new IntentFilter(EventType.LOADING_STARTED.getCode());
        filter.addAction(EventType.LOADING_FINISHED.getCode());
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            val event = EventType.getType(intent.getAction());
            switch (event) {
                case LOADING_STARTED:
                    if (controller == null || (controller != null && !controller.isAmbient())) {
                        loader.setVisibility(View.VISIBLE);
                    }
                    break;
                case LOADING_FINISHED:
                    loader.setVisibility(View.GONE);
                    break;
            }
        }
    };
}
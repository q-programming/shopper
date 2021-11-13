package pl.qprogramming.shopper.watch;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;
import lombok.val;
import pl.qprogramming.shopper.watch.config.Properties;
import pl.qprogramming.shopper.watch.fragments.register.RegisterFragment;
import pl.qprogramming.shopper.watch.fragments.welcome.WelcomeFragment;

import static android.text.TextUtils.isEmpty;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AmbientModeSupport.AmbientController controller = AmbientModeSupport.attach(this);
        boolean isAmbient = controller.isAmbient();
        setContentView(R.layout.activity_main);
        val sp = getDefaultSharedPreferences(this);
        val email = sp.getString(Properties.EMAIL, null);
        val token = sp.getString(Properties.TOKEN, null);
        Fragment fragment;
        if (isEmpty(email) || isEmpty(token)) {
            fragment = new RegisterFragment();
        } else {
            fragment = new WelcomeFragment();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_fragment_layout, fragment)
                .commit();
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
}
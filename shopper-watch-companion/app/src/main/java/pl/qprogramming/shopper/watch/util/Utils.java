package pl.qprogramming.shopper.watch.util;

import android.content.Context;

import java.util.Collection;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import lombok.val;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.FontSize;
import pl.qprogramming.shopper.watch.config.Properties;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class Utils {
    private Utils() {

    }
    /**
     * Checks if collection is null or empty
     *
     * @param coll collection to be tested
     * @return true if collection is null or empty
     */
    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    public static boolean isEmpty(String string) {
        return (string == null || string.isEmpty());
    }

    /**
     * Navigate to fragment using animations and adding name to stack
     *
     * @param fm       fragment maneger
     * @param fragment fragment to which navigation should go
     * @param name     name of new fragment to add to history stack
     */
    public static void navigateToFragment(FragmentManager fm, Fragment fragment, String name) {
        fm.beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                )
                .replace(R.id.activity_fragment_layout, fragment)
                .addToBackStack(name)
                .commit();
    }

    /**
     * Returns dimen value of font size taken from Shared Preferences
     * {@link android.util.TypedValue.COMPLEX_UNIT_PX}
     *
     * @param context required to get preferences and dimen value
     * @return float value that has to be set as
     */
    public static float getFontSize(Context context) {
        val dimen_sizes = new float[]{
                context.getResources().getDimension(R.dimen.small),
                context.getResources().getDimension(R.dimen.medium),
                context.getResources().getDimension(R.dimen.large),
        };
        val sp = getDefaultSharedPreferences(context);
        val fontSize = FontSize.getType(sp.getString(Properties.FONT, null));
        return dimen_sizes[fontSize.ordinal()];
    }
    public static float getDescFontSize(Context context) {
        val dimen_sizes = new float[]{
                context.getResources().getDimension(R.dimen.desc_small),
                context.getResources().getDimension(R.dimen.desc_medium),
                context.getResources().getDimension(R.dimen.desc_large),
        };
        val sp = getDefaultSharedPreferences(context);
        val fontSize = FontSize.getType(sp.getString(Properties.FONT, null));
        return dimen_sizes[fontSize.ordinal()];
    }

}

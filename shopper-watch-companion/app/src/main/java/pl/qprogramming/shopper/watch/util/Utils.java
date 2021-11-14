package pl.qprogramming.shopper.watch.util;

import java.util.Collection;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import pl.qprogramming.shopper.watch.R;

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
}

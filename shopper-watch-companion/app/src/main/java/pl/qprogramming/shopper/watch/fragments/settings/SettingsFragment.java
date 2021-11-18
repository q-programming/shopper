package pl.qprogramming.shopper.watch.fragments.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import lombok.val;
import pl.qprogramming.shopper.watch.BuildConfig;
import pl.qprogramming.shopper.watch.R;
import pl.qprogramming.shopper.watch.config.FontSize;
import pl.qprogramming.shopper.watch.config.Properties;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class SettingsFragment extends Fragment {

    private boolean aod;
    private FontSize fontSize;
    private TextView sizePreview;
    private Switch aodSwitch;
    private SharedPreferences sp;
    private String[] sizes;
    private float[] dimen_sizes;
    private Context context;

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getDefaultSharedPreferences(requireContext());
        context = requireContext();
        sizes = new String[]{
                context.getString(R.string.small),
                context.getString(R.string.medium),
                context.getString(R.string.large)
        };
        dimen_sizes = new float[]{
                context.getResources().getDimension(R.dimen.small),
                context.getResources().getDimension(R.dimen.medium),
                context.getResources().getDimension(R.dimen.large),
        };
        fontSize = FontSize.getType(sp.getString(Properties.FONT, null));
        aod = sp.getBoolean(Properties.AOD, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().findViewById(R.id.settings_btn).setVisibility(View.GONE);
        val approveBtn = requireActivity().findViewById(R.id.approve_btn);
        approveBtn.setVisibility(View.VISIBLE);
        approveBtn.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        sizePreview = view.findViewById(R.id.font_size_preview);
        aodSwitch = view.findViewById(R.id.aod);
        val versionTxt = (TextView) requireView().findViewById(R.id.version);
        versionTxt.setText(String.format("v%s", BuildConfig.VERSION_NAME));
        update();
        view.findViewById(R.id.font_size).setOnClickListener(v -> {
            val textSizes = sizes.clone();
            textSizes[fontSize.ordinal()] = textSizes[fontSize.ordinal()] + " \u2713";
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.font_size));
            builder.setItems(textSizes, (dialog, selected) -> {
                fontSize = FontSize.values()[selected];
                sp.edit()
                        .putString(Properties.FONT, fontSize.getSize())
                        .apply();
                update();
            });
            builder.show();
        });
        aodSwitch.setOnClickListener(v -> {
            aod = !aod;
            sp.edit()
                    .putBoolean(Properties.AOD, aod)
                    .apply();
            update();
        });
    }

    @Override
    public void onDestroyView() {
        requireActivity().findViewById(R.id.approve_btn).setVisibility(View.GONE);
        super.onDestroyView();
    }

    private void update() {
        sizePreview.setText(sizes[fontSize.ordinal()]);
        sizePreview.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen_sizes[fontSize.ordinal()]);
        aodSwitch.setChecked(aod);
    }
}
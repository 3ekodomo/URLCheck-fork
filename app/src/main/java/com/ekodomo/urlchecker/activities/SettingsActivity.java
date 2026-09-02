package com.ekodomo.urlchecker.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ekodomo.urlchecker.R;
import com.ekodomo.urlchecker.fragments.BrowserButtonsFragment;
import com.ekodomo.urlchecker.fragments.ResultCodeInjector;
import com.ekodomo.urlchecker.utilities.AndroidSettings;
import com.ekodomo.urlchecker.utilities.generics.GenericPref.BoolPref;
import com.ekodomo.urlchecker.utilities.generics.GenericPref.IntPref;
import com.ekodomo.urlchecker.utilities.methods.AndroidUtils;
import com.ekodomo.urlchecker.utilities.methods.Animations;
import com.ekodomo.urlchecker.utilities.methods.LocaleUtils;
import com.ekodomo.urlchecker.utilities.methods.PackageUtils;

import java.util.Objects;

/** An activity with general app-related settings */
public class SettingsActivity extends Activity {

    /** The width pref */
    public static IntPref WIDTH_PREF(Context cntx) {
        return new IntPref("width", WindowManager.LayoutParams.WRAP_CONTENT, cntx);
    }

    /** The sync process-text pref */
    public static BoolPref SYNC_PROCESSTEXT_PREF(Context cntx) {
        return new BoolPref("syncProcessText", true, cntx);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSettings.setTheme(this, false);
        LocaleUtils.setLocale(this);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.a_settings);
        AndroidUtils.configureUp(this);

        configureBrowserButtons();
        configureTheme();
        configureLocale();
        configureEdgesAndColors();
        Animations.ANIMATIONS(this).attachToSwitch(findViewById(R.id.animations));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SYNC_PROCESSTEXT_PREF(this).attachToSwitch(findViewById(R.id.processText));
        } else {
            findViewById(R.id.processText).setVisibility(View.GONE);
        }

        // if this was reloaded, some settings may have change, so reload previous one too
        if (AndroidSettings.wasReloaded(this)) AndroidSettings.markForReloading(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // press the 'back' button in the action bar to go back
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* ------------------- configure browser ------------------- */

    private final ResultCodeInjector resultCodeInjector = new ResultCodeInjector();
    private final BrowserButtonsFragment browserButtons = new BrowserButtonsFragment(this, resultCodeInjector);

    private void configureBrowserButtons() {
        browserButtons.onInitialize(findViewById(browserButtons.getLayoutId()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!resultCodeInjector.onActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    /* ------------------- theme ------------------- */

    /** init theme config */
    private void configureTheme() {
        // init dayNight spinner
        AndroidSettings.THEME_PREF(this).attachToSpinner(
                this.findViewById(R.id.theme),
                v -> AndroidSettings.reload(SettingsActivity.this)
        );

        // init width seekBar
        // 0      <-> WRAP_CONTENT
        // [1,99] <-> [1,99]
        // 100    <-> MATCH_PARENT
        WIDTH_PREF(this).attachToSeekBar(findViewById(R.id.width_value), findViewById(R.id.width_label),
                prefValue ->
                        prefValue == WindowManager.LayoutParams.WRAP_CONTENT ? Pair.create(0, getString(R.string.spin_dynamicWidth))
                                : prefValue == WindowManager.LayoutParams.MATCH_PARENT ? Pair.create(100, getString(R.string.spin_fullWidth))
                                : Pair.create(prefValue, prefValue + "%"),
                seekBarValue ->
                        seekBarValue == 0 ? WindowManager.LayoutParams.WRAP_CONTENT
                                : seekBarValue == 100 ? WindowManager.LayoutParams.MATCH_PARENT
                                : seekBarValue
        );

    }

    /* ------------------- edges and colors ------------------- */

    private void configureEdgesAndColors() {
        AndroidSettings.EDGES_PREF(this).attachToSpinner(
                this.findViewById(R.id.edges),
                v -> {
                    updateRoundedAmountVisibility();
                    AndroidSettings.reload(SettingsActivity.this);
                }
        );

        AndroidSettings.ROUNDED_AMOUNT_PREF(this).attachToSeekBar(
                findViewById(R.id.rounded_amount_value),
                findViewById(R.id.rounded_amount_label),
                prefValue -> Pair.create(prefValue, prefValue + "dp"),
                seekBarValue -> seekBarValue
        );

        AndroidSettings.SHOW_ELEMENT_BORDER_PREF(this).attachToSwitch(findViewById(R.id.show_element_borders));
        AndroidSettings.SHOW_INTERFACE_BORDER_PREF(this).attachToSwitch(findViewById(R.id.show_interface_border));

        AndroidSettings.ELEMENT_COLOR_PREF(this).attachToEditText(findViewById(R.id.element_color));
        AndroidSettings.BORDER_COLOR_PREF(this).attachToEditText(findViewById(R.id.border_color));

        updateRoundedAmountVisibility();
    }

    private void updateRoundedAmountVisibility() {
        boolean isRounded = AndroidSettings.EDGES_PREF(this).get() == AndroidSettings.Edges.ROUNDED;
        findViewById(R.id.rounded_amount_container).setVisibility(isRounded ? View.VISIBLE : View.GONE);
        findViewById(R.id.rounded_amount_value).setVisibility(isRounded ? View.VISIBLE : View.GONE);
    }

    /* ------------------- locale ------------------- */

    /** init locale spinner */
    private void configureLocale() {
        // init
        var pref = LocaleUtils.LOCALE_PREF(this);
        var spinner = this.<Spinner>findViewById(R.id.locale);

        // populate available
        var locales = LocaleUtils.getLocales(this);
        var adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locales
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // select current option
        for (int i = 0; i < locales.size(); i++) {
            if (Objects.equals(locales.get(i).tag, pref.get())) spinner.setSelection(i);
        }

        // add listener to auto-change it
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // set+notify if changed
                if (!Objects.equals(pref.get(), locales.get(i).tag)) {
                    pref.set(locales.get(i).tag);
                    AndroidSettings.reload(SettingsActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /* ------------------- tutorial ------------------- */

    public void openTutorial(View view) {
        PackageUtils.startActivity(new Intent(this, TutorialActivity.class), R.string.toast_noApp, this);
    }

    /* ------------------- backup ------------------- */

    public void openBackup(View view) {
        PackageUtils.startActivityForResult(new Intent(this, BackupActivity.class),
                AndroidSettings.registerForReloading(resultCodeInjector, this),
                R.string.toast_noApp,
                this
        );
    }

}

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
import android.content.SharedPreferences;
import android.widget.Spinner;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.regex.Pattern;
import android.content.SharedPreferences;
import com.ekodomo.urlchecker.utilities.AndroidSettings;


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

    private final SharedPreferences.OnSharedPreferenceChangeListener themeChangeListener = (sharedPreferences, key) -> {
        if ("dayNight".equals(key)) {
            AndroidSettings.reload(SettingsActivity.this);
        }
    };

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
    protected void onResume() {
        super.onResume();
        com.ekodomo.urlchecker.utilities.generics.GenericPref.getPrefs(this).registerOnSharedPreferenceChangeListener(themeChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        com.ekodomo.urlchecker.utilities.generics.GenericPref.getPrefs(this).unregisterOnSharedPreferenceChangeListener(themeChangeListener);
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
                null
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

        AndroidSettings.INTERFACE_EDGES_PREF(this).attachToSpinner(
                this.findViewById(R.id.overall_interface_edges),
                v -> AndroidSettings.reload(SettingsActivity.this)
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
    /* ------------------- customisation ------------------- */

    public void openImportExportCustomization(View view) {
        try {
            android.content.SharedPreferences prefs = com.ekodomo.urlchecker.utilities.generics.GenericPref.getPrefs(this);
            JSONObject customisation = new JSONObject();
            customisation.put("version", 1);

            AndroidSettings.Theme currentTheme = AndroidSettings.THEME_PREF(this).get();
            String themeStr = currentTheme == AndroidSettings.Theme.DEFAULT ? "system" : currentTheme.name().toLowerCase();
            customisation.put("theme", themeStr);

            JSONObject uiPrefs = new JSONObject();
            uiPrefs.put("edges", AndroidSettings.EDGES_PREF(this).get().name().toLowerCase());
            uiPrefs.put("borders", AndroidSettings.SHOW_ELEMENT_BORDER_PREF(this).get());
            customisation.put("ui_preferences", uiPrefs);

            JSONObject colors = new JSONObject();
            for (AndroidSettings.Theme t : AndroidSettings.Theme.values()) {
                if (t == AndroidSettings.Theme.DEFAULT) continue;
                String themeName = t.name().toLowerCase();
                JSONObject themeColors = new JSONObject();
                String eKey = "element_color_" + themeName;
                String bKey = "border_color_" + themeName;

                // Fetch from prefs, or use default if not present (optional, but let's only export saved ones or all defaults)
                // Let's export what's saved, or the default so it matches schema.
                String eColor = prefs.getString(eKey, null);
                if (eColor != null) {
                    themeColors.put("element", eColor);
                }
                String bColor = prefs.getString(bKey, null);
                if (bColor != null) {
                    themeColors.put("border", bColor);
                }

                if (themeColors.length() > 0) {
                    colors.put(themeName, themeColors);
                }
            }
            customisation.put("colors", colors);

            EditText input = new EditText(this);
            input.setSingleLine(false);
            input.setText(customisation.toString(2));

            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_exportImportTitle)
                    .setView(input)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        try {
                            JSONObject newCustomisation = new JSONObject(input.getText().toString());
                            android.content.SharedPreferences.Editor editor = prefs.edit();

                            if (newCustomisation.has("theme")) {
                                String tStr = newCustomisation.getString("theme");
                                if ("system".equalsIgnoreCase(tStr)) {
                                    editor.putInt("dayNight", AndroidSettings.Theme.DEFAULT.getId());
                                } else {
                                    try {
                                        AndroidSettings.Theme theme = AndroidSettings.Theme.valueOf(tStr.toUpperCase());
                                        editor.putInt("dayNight", theme.getId());
                                    } catch (IllegalArgumentException e) {
                                        Toast.makeText(this, "Invalid theme: " + tStr, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            if (newCustomisation.has("ui_preferences")) {
                                JSONObject uiPrefsObj = newCustomisation.getJSONObject("ui_preferences");
                                if (uiPrefsObj.has("edges")) {
                                    String edgesStr = uiPrefsObj.getString("edges").toUpperCase();
                                    try {
                                        AndroidSettings.Edges edges = AndroidSettings.Edges.valueOf(edgesStr);
                                        editor.putInt("edges", edges.getId());
                                    } catch (IllegalArgumentException e) {
                                        Toast.makeText(this, "Invalid edge style: " + edgesStr, Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if (uiPrefsObj.has("borders")) {
                                    editor.putBoolean("showElementBorder", uiPrefsObj.getBoolean("borders"));
                                }
                            }

                            if (newCustomisation.has("colors")) {
                                JSONObject colorsObj = newCustomisation.getJSONObject("colors");
                                Pattern colorPattern = Pattern.compile("^#(?:[0-9a-fA-F]{3}){1,2}$");
                                for (AndroidSettings.Theme t : AndroidSettings.Theme.values()) {
                                    if (t == AndroidSettings.Theme.DEFAULT) continue;
                                    String themeName = t.name().toLowerCase();
                                    if (colorsObj.has(themeName)) {
                                        JSONObject themeColors = colorsObj.getJSONObject(themeName);
                                        String eKey = "element_color_" + themeName;
                                        String bKey = "border_color_" + themeName;
                                        if (themeColors.has("element")) {
                                            String val = themeColors.getString("element");
                                            if (colorPattern.matcher(val).matches()) {
                                                editor.putString(eKey, val);
                                            } else {
                                                Toast.makeText(this, "Invalid color: " + val, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        if (themeColors.has("border")) {
                                            String val = themeColors.getString("border");
                                            if (colorPattern.matcher(val).matches()) {
                                                editor.putString(bKey, val);
                                            } else {
                                                Toast.makeText(this, "Invalid color: " + val, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                            }

                            editor.apply();
                            Toast.makeText(this, R.string.toast_customisationApplied, Toast.LENGTH_SHORT).show();
                            AndroidSettings.reload(this);
                        } catch (JSONException e) {
                            Toast.makeText(this, R.string.toast_invalidJson, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

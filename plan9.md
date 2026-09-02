1. **Fix elements background in Dark/AMOLED themes & add toggle to use theme default colors:**
   - Instead of storing colors per theme, the prompt literally says: "add toggle to enable and disable element and border colour set to default respectively to that theme(light, dark, device default, amoled)".
   - Also, "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
   - Let's create `USE_THEME_DEFAULT_COLORS_PREF` toggle.
   - When enabled, the App ignores custom `ELEMENT_COLOR_PREF` and uses `#ffffff` for light, `#333333` for dark, `#000000` for amoled.
   - Let's provide a "Reset to default" button that clears the saved preference value.
   - Wait, "customize it consider that value as default": Maybe it means that the default element colors (when custom colors toggle is disabled, or when custom colors haven't been specified) should be different per theme.
   - Let's modify `AndroidSettings.java`:
     ```java
     static StringPref ELEMENT_COLOR_PREF(Context cntx) {
         return new StringPref("elementColor", "", cntx); // empty string means use theme default
     }
     static StringPref BORDER_COLOR_PREF(Context cntx) {
         return new StringPref("borderColor", "", cntx);
     }
     static BoolPref USE_THEME_DEFAULT_COLORS_PREF(Context cntx) {
         return new BoolPref("useThemeDefaultColors", true, cntx);
     }
     ```
   - In `AndroidUtils.setCustomBackground(View, Context)`:
     - Check `USE_THEME_DEFAULT_COLORS_PREF`.
     - If true, or if `ELEMENT_COLOR_PREF` is empty/invalid, use the current theme's default element color:
       - Light: `#ffffff`
       - Dark: `#333333` (or similar)
       - AMOLED: `#000000`
     - Else, parse `ELEMENT_COLOR_PREF`.

2. **Add color preview and hex code validation**:
   - Add `<View>` next to the `EditText` in `activity_settings.xml`.
   - Update `SettingsActivity.java` to set up `TextWatcher`s on `element_color` and `border_color`.
   - If valid hex, change the `<View>`'s background color. If not, maybe show transparent.
   - To conform to "Unless user clicks reset settings", add a Reset button next to the `EditText`s that clears the `EditText`s.

3. **Add option to customize Material You accent color**:
   - Add toggle `MATERIAL_YOU_ACCENT_PREF`.
   - In `AndroidSettings.setTheme`, if toggle is ON and SDK >= 31, apply a dynamic Material You theme overlay, or just avoid setting `colorAccent` to `@color/app` (so the system default Material You colors shine through).
   - We will need a `res/values-v31/styles.xml` where we define Material You themes, OR just use `com.google.android.material.color.DynamicColors.applyToActivityIfAvailable(activity)` if the Material library is included (it isn't based on build.gradle).
   - Actually, since `Theme.DeviceDefault` on API 31+ has Material You colors built-in, overriding `colorAccent` prevents them from working. So we can conditionally use a style without `colorAccent` override.

1. **Fix elements background in Dark/Amoled themes & add toggle to use theme default colors:**
   - Add a `USE_THEME_DEFAULT_COLORS_PREF` toggle (default true).
   - In `AndroidSettings.java`:
     - Update `ELEMENT_COLOR_PREF` to return the stored hex or empty string. (Wait, let's keep it as is, default "#ffffff", but add logic to use theme default if `USE_THEME_DEFAULT_COLORS_PREF` is true).
     - Actually, "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
     - Let's have ONE toggle "Use Default Colors" (`USE_THEME_DEFAULT_COLORS_PREF`).
     - BUT the user can "reset settings". I should add a "Reset to default colors" button.
     - When they change the color in the EditText, the "Use Default Colors" toggle turns OFF automatically.
     - When the toggle is ON, the EditTexts are disabled and set to the theme's default color (Light: #ffffff, Dark: #424242 or #303030, Amoled: #000000. Border: #808080).
     - This way we support per-theme defaults dynamically.

2. **Validate the hex code and show preview color**:
   - In `activity_settings.xml`, wrap the `EditText` and a new `View` (for preview) inside a `LinearLayout`.
   - In `SettingsActivity.java`, attach a `TextWatcher` to `element_color` and `border_color` `EditText`s.
   - Use `Color.parseColor(text)` inside a `try-catch` block. If valid, set the preview `View`'s background color. If invalid, set it to transparent or a warning icon.

3. **Material You accent color**:
   - "add option to customize Material You accent color."
   - Add a toggle `MATERIAL_YOU_ACCENT_PREF` (default false).
   - Only show this toggle if `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` (API 31).
   - If enabled, in `AndroidSettings.setTheme()`, use a style that does NOT override `colorAccent`. We can create `styles-v31.xml` with styles like `ActivityThemeMaterialYou` that extend `android:Theme.DeviceDefault.DayNight` but don't specify `colorAccent`.
   - Or simply programmatically change the theme to `android.R.style.Theme_DeviceDefault_DayNight` (since it supports Material You out of the box without `colorAccent` overrides).

I will now request a review for this plan.

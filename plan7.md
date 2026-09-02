1. **Fix elements background in Dark/Amoled themes**:
   - Create a switch `USE_THEME_DEFAULT_COLORS_PREF` (default true).
   - In `AndroidSettings.java` and `SettingsActivity.java`, use this preference to manage colors.
   - If `USE_THEME_DEFAULT_COLORS_PREF` is ON:
     - Get current theme (Light, Dark, Amoled).
     - Light/Device Default (light): return `#ffffff` element, `#808080` border.
     - Dark/Device Default (dark): return `#303030` element, `#808080` border.
     - Amoled: return `#000000` element, `#808080` border.
     - (And disable/hide the custom hex color inputs).
   - If `USE_THEME_DEFAULT_COLORS_PREF` is OFF:
     - Allow user to customize using existing `ELEMENT_COLOR_PREF` and `BORDER_COLOR_PREF`.
     - When this toggle turns from ON to OFF (or when they change theme), we can initialize the inputs with the theme default. The user asked "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
     - OK, better design: each theme has its own saved custom color, but by default it acts as the theme default. BUT the user said "Unless user clicks reset settings."
     - This perfectly implies: Have 1 input for Element Color, 1 for Border Color. Next to them, add a "Reset to theme default" button.
     - When the theme is Dark, and `ELEMENT_COLOR_PREF` is still `#ffffff`, it should act as `#303030`. Actually, `ELEMENT_COLOR_PREF` has a default value `#ffffff`.
     - Let's change the preferences to save colors PER theme: `ELEMENT_COLOR_DARK_PREF`, `ELEMENT_COLOR_LIGHT_PREF`, `ELEMENT_COLOR_AMOLED_PREF`, `ELEMENT_COLOR_DEFAULT_PREF`. And similarly for border colors.
     - When a theme is selected, the color input shows the value for that theme.
     - Add a "Reset Colors" button that clears the preferences for the CURRENT theme, so it falls back to its default (Light `#ffffff`, Dark `#303030`, Amoled `#000000`).

2. **Validate the hex code and show preview color**:
   - Add a small `View` (color block) to the right of the `EditText` for color preview.
   - Attach a `TextWatcher`. Use `Color.parseColor(text)` to validate. If valid, set preview block color.

3. **Material You accent color**:
   - "add option to customize Material You accent color."
   - Create a toggle `MATERIAL_YOU_ACCENT_PREF`.
   - In `AndroidSettings.setTheme()`, if API >= 31 and toggle is ON, dynamically apply system accent colors by resolving `android.R.color.system_accent1_500` or using `android.R.style.Theme_DeviceDefault_DayNight`. (Wait, DeviceDefault automatically uses Material You on Android 12+! The problem is that the app overrides it: `<item name="colorAccent">@color/app</item>`).
   - If `MATERIAL_YOU_ACCENT_PREF` is ON, we don't apply the custom `ActivityThemeDark` etc., or we apply a variation that does NOT override `colorAccent`. We can create `styles-v31.xml` or just programmatically do something? No, it's easier to create new styles `ActivityThemeDarkMaterialYou` which don't override `colorAccent`, and choose them in `AndroidSettings.setTheme()` if the toggle is ON.

Plan:
1. Update `res/values/strings.xml`, `res/layout/activity_settings.xml`.
2. Add "Reset Colors" button (or a switch) in layout.
3. Update `AndroidSettings.java` with Theme-specific color preferences.
4. Update `SettingsActivity.java` to handle Hex validation, Color preview update, and Reset logic.
5. Create Material You toggle and styles.

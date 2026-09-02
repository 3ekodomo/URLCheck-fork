1. **Fix elements background in Dark/Amoled themes**:
   - `AndroidSettings.java` defines `ELEMENT_COLOR_PREF(Context)`.
   - Update `AndroidSettings` to store colors per-theme. Currently it returns `#ffffff`. Wait, the prompt states:
   - "add toggle to enable and disable element and border colour set to default respectively to that theme(light, dark, device default, amoled)"
   - "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
   - Okay, let's create a Boolean preference `USE_THEME_DEFAULT_COLORS_PREF` ("Use theme default colors").
   - By default it's `true`.
   - If `true`, the element and border colors are automatically determined by the selected `Theme`.
     - Light/Device Default (light): `#ffffff` element, `#808080` border.
     - Dark/Device Default (dark): `#333333` (or similar) element, `#808080` border.
     - Amoled: `#000000` element, `#808080` border.
   - Wait, if the user toggles this off, they can specify their own custom `elementColor` and `borderColor`.
   - BUT the prompt says "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
   - This means we shouldn't use a toggle for "Use theme default colors" in the way I described. It means: Each theme has its OWN default color. If the user picks a theme, they see the colors for that theme. They can customize them. If they click "reset settings", it resets to that theme's default.
   - So I need:
     - `ELEMENT_COLOR_LIGHT_PREF`, `ELEMENT_COLOR_DARK_PREF`, `ELEMENT_COLOR_AMOLED_PREF`, `ELEMENT_COLOR_DEFAULT_PREF`
     - Same for border colors.
     - Actually, "add toggle to enable and disable element and border colour set to default respectively to that theme" -> Yes, a toggle. Wait, the prompt says "enable and disable element and border colour set to default". So maybe just ONE toggle per color? Or a toggle to just "Use Default Colors"?
     - "add toggle to enable and disable element and border colour set to default respectively to that theme(light, dark, device default, amoled)"
     - This toggle will just be `USE_THEME_DEFAULT_COLORS`.
     - Let's add a `Switch` in `activity_settings.xml`: `@string/txt_use_theme_default_colors`.
     - When toggled, if ON, the Hex code text boxes are disabled/hidden, and the app uses predefined theme defaults.
     - BUT the prompt also says: "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
     - This implies: If they change the hex, the toggle turns OFF. Or if they click "Reset", it turns ON.
     - Okay, if the user customizes the color (types in the EditText), we turn OFF the "Use default" toggle. If they click "Reset" (or turn ON the toggle), it resets to the theme's default.

2. **Validate the hex code and show preview color**:
   - Add a small `View` next to the `EditText` for color preview.
   - In `SettingsActivity`, set a `TextWatcher` to validate `Color.parseColor(text)`. If valid, update the preview. If invalid, maybe show a red X or don't update.

3. **Material You accent color**:
   - "add option to customize Material You accent color."
   - Add a Switch: `USE_MATERIAL_YOU_ACCENT` (only visible/enabled on Android 12+).
   - If enabled, update the theme to use `DynamicColors` (from Material library) or override `colorAccent` programmatically, or apply a `ThemeOverlay` using `android:color/system_accent1_500` (or `android.R.color.system_accent1_500`). Wait, how to apply Material You?
   - `com.google.android.material:material` might not be in `build.gradle`. If not, I can just use `android.R.color.system_accent1_500` inside a dynamic theme overlay if API >= 31.

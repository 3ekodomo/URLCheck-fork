1. **Fix Dark and AMOLED themes element background color:**
   - In `AndroidUtils.setCustomBackground`, the code uses `AndroidSettings.ELEMENT_COLOR_PREF(context).get()` which defaults to `#ffffff`. If the theme is dark/amoled, this makes elements white, which breaks the dark theme.
   - We need to add a "Use theme default colors" toggle (`USE_THEME_DEFAULT_COLORS_PREF`, default true).
   - If this toggle is true:
     - For Light theme (or Device Default when in light mode), element color is `#ffffff` (or derived from theme) and border is `#808080`.
     - For Dark/AMOLED theme, element color should be dark (e.g. `#333333` or `#000000`). We can extract it from the theme or just hardcode suitable colors (like `#303030` for dark, `#000000` for amoled).
   - "add toggle to enable and disable element and border colour set to default respectively to that theme(light, dark, device default, amoled)" -> This is exactly what the toggle should do.
2. **Add toggle and custom color fields:**
   - In `SettingsActivity`, add a toggle switch "Use theme default element/border colors".
   - When the toggle is off, show the custom `element_color` and `border_color` fields.
   - When the toggle is on, hide them, or just ignore them.
   - "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
     - Wait, the user might mean: "if you customize the color in Dark theme, save it for Dark theme. If in Light theme, save for Light theme." So maybe store `ELEMENT_COLOR_DARK_PREF`, `ELEMENT_COLOR_LIGHT_PREF` etc.
     - OR, simply: The custom color you pick *becomes* the color used. "Unless user clicks reset settings." - meaning add a "Reset to default" button next to the custom color inputs?
     - Actually, "add toggle to enable and disable element and border colour set to default respectively to that theme" means there's ONE toggle for "Use Default Colors". If enabled, it automatically uses the correct default color for the current theme. If disabled, it uses the user-provided HEX.
     - BUT, the user also says: "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
     - This probably implies we don't need a toggle? Wait, "add toggle to enable and disable element and border colour set to default respectively to that theme(light, dark, device default, amoled)". Okay, so we DO need a toggle.
     - Maybe the toggle IS the "reset settings" part.
3. **Color preview and validation:**
   - Add a small `View` (color block) to the right of the `EditText` for hex code in `activity_settings.xml`.
   - In `SettingsActivity`, add a `TextWatcher` to `element_color` and `border_color` `EditText`s. Try parsing `Color.parseColor(text)`. If valid, update the background of the preview `View`. If invalid, show a placeholder or red outline.
4. **Material You Accent Color:**
   - Add a switch `MATERIAL_YOU_ACCENT_PREF`.
   - If enabled, the app should use Material You accent color (`@android:color/system_accent1_500` or similar for API 31+).
   - Since `ActivityThemeDark` defines `<item name="colorAccent">@color/app</item>`, we can programmatically override it in `AndroidSettings.setTheme()` using `ActivityThemeMaterialYou` if we define one, OR in API 31+ just use dynamic colors.

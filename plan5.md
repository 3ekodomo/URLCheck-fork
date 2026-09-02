1. Add toggle for default elements:
   - Add a switch `use_default_colors` to `SettingsActivity`.
   - Update `AndroidSettings` to expose this preference (`USE_DEFAULT_COLORS_PREF`).
   - If enabled, ignore custom colors and use the theme's default colors.
2. The user specifically says:
   - "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
   - Let's look closely at `AndroidSettings.java`. Wait, does it mean if they select dark/amoled theme, the default colors should change to something else, and if they click "reset settings" it goes back to default? But there's no "reset settings" button currently for colors. I need to add one. Wait, the user said "Unless user clicks reset settings". Oh! There is no "reset settings" currently. I can add a reset button next to the color preferences.
   - Wait, if "user selects the dark or amoled theme and customize it consider that value as default." This implies there's a default element/border color per theme.
   - For light theme, element is `#ffffff`, border `#808080`.
   - For dark/amoled theme, element is maybe `#333333` (or whatever dark is)?
   - "add toggle to enable and disable element and border colour set to default respectively to that theme(light, dark, device default, amoled)" -> A toggle called "Use theme default colors" which sets the colors dynamically based on the current theme.
3. Show color preview & Hex validation:
   - Next to the Hex EditText, show a small color block (e.g. `View` with `background` as a solid color).
   - Use `TextWatcher` to parse the color. If valid, update the preview block.
4. Material You Accent Color:
   - Add "Use Material You accent color" switch. (Only available/visible on Android 12+).
   - If enabled, we should use dynamic colors `DynamicColors.applyToActivityIfAvailable(this)`? No, the app has no `MaterialComponents` theme, it uses `DeviceDefault`. We can get the Material You accent color using `android.R.color.system_accent1_100` etc. Or just use `com.google.android.material:material`?

1. **Add option to toggle default element/border color per theme:**
   - In `AndroidSettings.java`, modify `ELEMENT_COLOR_PREF` and `BORDER_COLOR_PREF` to depend on the current theme (`Theme` setting).
   - Actually, wait, the instructions say: "add toggle to enable and disable element and border colour set to default respectively to that theme(light, dark, device default, amoled)".
   - It means there should be a switch/checkbox "Use custom colors" or "Use default theme colors". If this is enabled (or disabled), the `ELEMENT_COLOR_PREF` and `BORDER_COLOR_PREF` are ignored, and default colors are used for each theme.
   - Better yet, when the theme is selected, and "customize colors" is not checked, the system sets the element color and border color to default for that theme. Or we can just have a boolean pref `CUSTOM_COLORS_PREF`.
   - Oh, I need to check how to map colors based on the theme.
2. **Preview color and validate hex code:**
   - Add a color preview view next to the element color and border color `EditText`s in `activity_settings.xml`.
   - Update `SettingsActivity.java` and `GenericPref` to validate hex codes. If invalid, the preview color should show some invalid state or default color, or we should use a `TextWatcher` to show if valid.
   - Wait, `GenericPref.attachToEditText` is currently generic. I can add logic in `SettingsActivity.java` with a `TextWatcher` to update the preview view.
3. **Handle dark/amoled theme defaults:**
   - If user selects dark or amoled theme and customizes it, consider that value as default (unless reset).
   - Wait, the user specifically mentioned: "If user selects the dark or amoled theme and customize it consider that value as default. Unless user clicks reset settings."
4. **Material You accent color:**
   - "add option to customize Material You accent color."
   - In Android 12+, we can use `DynamicColors` to get Material You accent colors. Wait, "customize Material You accent color"? Maybe a way to enable/disable Material You accent?
   - Wait, the user says "add option to customize Material You accent color".

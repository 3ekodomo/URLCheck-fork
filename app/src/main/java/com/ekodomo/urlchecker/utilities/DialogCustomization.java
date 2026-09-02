package com.ekodomo.urlchecker.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

/**
 * Resolves and applies all visual customization used by URLCheck dialogs.
 *
 * Theme colors are intentionally resolved per effective theme. A color that has
 * not been customized for the current theme always falls back to that theme's
 * native default, so changing theme never carries the previous theme's color.
 */
public final class DialogCustomization {

    private static final float DEFAULT_ELEMENT_RADIUS_DP = 20f;
    private static final int DEFAULT_OVERALL_RADIUS_DP = 28;
    private static final int MAX_OVERALL_RADIUS_DP = 64;

    private DialogCustomization() {
    }

    public static int getElementColor(Context context) {
        return parseColor(AndroidSettings.ELEMENT_COLOR_PREF(context).get(),
                AndroidSettings.getDefaultElementColor(context));
    }

    public static int getElementBorderColor(Context context) {
        return parseColor(AndroidSettings.BORDER_COLOR_PREF(context).get(),
                AndroidSettings.getDefaultBorderColor(context));
    }

    public static int getOverallBorderColor(Context context) {
        return parseColor(AndroidSettings.OVERALL_BORDER_COLOR_PREF(context).get(),
                AndroidSettings.getDefaultBorderColor(context));
    }

    /**
     * Applies the element background. This is the only place where the element
     * color is used; it is deliberately not reused as the dialog/window color.
     */
    public static void applyElement(View view, Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(getElementColor(context));

        switch (AndroidSettings.EDGES_PREF(context).get()) {
            case SHARP:
                drawable.setCornerRadius(0f);
                break;
            case ROUNDED:
                drawable.setCornerRadius(dp(context, Math.max(0,
                        AndroidSettings.ROUNDED_AMOUNT_PREF(context).get())));
                break;
            case DEFAULT:
            default:
                drawable.setCornerRadius(dp(context, DEFAULT_ELEMENT_RADIUS_DP));
                break;
        }

        if (AndroidSettings.SHOW_ELEMENT_BORDER_PREF(context).get()) {
            drawable.setStroke(dpInt(context, 1.5f), getElementBorderColor(context));
        }

        view.setBackground(drawable);
    }

    /**
     * Applies the overall dialog/window appearance.
     *
     * The dialog surface comes from the current Android theme, never from the
     * element-color preference. This prevents an element customization from
     * accidentally recoloring the complete dialog.
     */
    public static void applyOverall(Activity activity) {
        View surface = activity.findViewById(com.ekodomo.urlchecker.R.id.overall_interface_surface);
        View borderOverlay = activity.findViewById(com.ekodomo.urlchecker.R.id.overall_interface_border_overlay);
        if (surface == null) return;

        AndroidSettings.OverallEdges edge = AndroidSettings.OVERALL_EDGES_PREF(activity).get();
        boolean borderEnabled = AndroidSettings.SHOW_OVERALL_INTERFACE_BORDER_PREF(activity).get();

        boolean customSurface = edge != AndroidSettings.OverallEdges.DEFAULT || borderEnabled;
        if (!customSurface) {
            activity.getWindow().setBackgroundDrawable(null);
            surface.setBackground(null);
            if (borderOverlay != null) {
                borderOverlay.setBackground(null);
                borderOverlay.setVisibility(View.GONE);
            }
            return;
        }

        // The window and content surface are transparent. The content can then
        // use the full area without covering the custom border.
        activity.getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
        );

        GradientDrawable surfaceDrawable = new GradientDrawable();
        surfaceDrawable.setShape(GradientDrawable.RECTANGLE);
        surfaceDrawable.setColor(resolveThemeBackground(activity));

        if (edge == AndroidSettings.OverallEdges.SHARP) {
            surfaceDrawable.setCornerRadius(0f);
        } else {
            int radius = AndroidSettings.OVERALL_ROUNDED_AMOUNT_PREF(activity).get();
            radius = Math.max(0, Math.min(MAX_OVERALL_RADIUS_DP, radius));
            surfaceDrawable.setCornerRadius(dp(activity, radius));
        }

        surface.setBackground(surfaceDrawable);

        // IMPORTANT: the border is drawn in a separate top layer. The ScrollView
        // and module views fill the parent, so drawing the stroke on the parent
        // background caused the top/bottom sides to be painted over.
        if (borderOverlay != null) {
            if (borderEnabled) {
                GradientDrawable borderDrawable = new GradientDrawable();
                borderDrawable.setShape(GradientDrawable.RECTANGLE);
                borderDrawable.setColor(Color.TRANSPARENT);

                if (edge == AndroidSettings.OverallEdges.SHARP) {
                    borderDrawable.setCornerRadius(0f);
                } else {
                    int radius = AndroidSettings.OVERALL_ROUNDED_AMOUNT_PREF(activity).get();
                    radius = Math.max(0, Math.min(MAX_OVERALL_RADIUS_DP, radius));
                    borderDrawable.setCornerRadius(dp(activity, radius));
                }

                int width = Math.max(1, Math.min(10,
                        AndroidSettings.OVERALL_BORDER_WIDTH_PREF(activity).get()));
                borderDrawable.setStroke(dpInt(activity, width),
                        getOverallBorderColor(activity));

                borderOverlay.setBackground(borderDrawable);
                borderOverlay.setVisibility(View.VISIBLE);
                borderOverlay.setClickable(false);
                borderOverlay.setFocusable(false);
            } else {
                borderOverlay.setBackground(null);
                borderOverlay.setVisibility(View.GONE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            surface.setClipToOutline(edge != AndroidSettings.OverallEdges.SHARP);
        }
    }

    /** Re-applies customization to an existing element view. */
    public static void refreshElement(View view, Context context) {
        if (view != null) applyElement(view, context);
    }

    private static int resolveThemeBackground(Context context) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.colorBackground, value, true)) {
            if (value.resourceId != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return context.getResources().getColor(value.resourceId, context.getTheme());
                }
                return context.getResources().getColor(value.resourceId);
            }
            if (value.type >= TypedValue.TYPE_FIRST_INT && value.type <= TypedValue.TYPE_LAST_INT) {
                return value.data;
            }
        }
        return Color.parseColor(AndroidSettings.getDefaultElementColor(context));
    }

    private static int parseColor(String value, String fallback) {
        try {
            return Color.parseColor(value);
        } catch (Exception ignored) {
            return Color.parseColor(fallback);
        }
    }

    private static float dp(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    private static int dpInt(Context context, float value) {
        return Math.max(1, Math.round(dp(context, value)));
    }
}

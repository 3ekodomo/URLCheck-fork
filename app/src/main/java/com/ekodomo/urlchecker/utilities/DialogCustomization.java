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
        if (surface == null) return;

        AndroidSettings.OverallEdges edge = AndroidSettings.OVERALL_EDGES_PREF(activity).get();
        boolean borderEnabled = AndroidSettings.SHOW_OVERALL_INTERFACE_BORDER_PREF(activity).get();

        // Default means "do not replace the platform dialog surface". This keeps
        // the original Android dialog appearance unless the user explicitly
        // chooses an overall edge or border.
        if (edge == AndroidSettings.OverallEdges.DEFAULT && !borderEnabled) {
            activity.getWindow().setBackgroundDrawable(null);
            surface.setBackground(null);
            surface.setPadding(0, 0, 0, 0);
            return;
        }

        // The custom surface owns the complete border. The window itself is
        // transparent so Android's dialog background cannot add an inset border
        // on only two sides or clip the top/bottom strokes.
        activity.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(resolveThemeBackground(activity));

        if (edge == AndroidSettings.OverallEdges.SHARP) {
            drawable.setCornerRadius(0f);
        } else {
            int radius = AndroidSettings.OVERALL_ROUNDED_AMOUNT_PREF(activity).get();
            radius = Math.max(0, Math.min(MAX_OVERALL_RADIUS_DP, radius));
            drawable.setCornerRadius(dp(activity, radius));
        }

        if (borderEnabled) {
            int width = Math.max(1, Math.min(10, AndroidSettings.OVERALL_BORDER_WIDTH_PREF(activity).get()));
            drawable.setStroke(dpInt(activity, width), getOverallBorderColor(activity));
        }

        surface.setBackground(drawable);

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

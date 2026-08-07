package com.guardpoint.android.util;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.guardpoint.android.R;

public final class ErrorNotification {

    private static final int DURATION_DEFAULT = Snackbar.LENGTH_LONG;
    private static final int DURATION_SHORT = Snackbar.LENGTH_SHORT;

    private ErrorNotification() {
    }

    public static void showError(@NonNull View rootView, @NonNull String message) {
        show(rootView, message, DURATION_DEFAULT);
    }

    public static void showErrorShort(@NonNull View rootView, @NonNull String message) {
        show(rootView, message, DURATION_SHORT);
    }

    private static void show(@NonNull View rootView, @NonNull String message, int duration) {
        Snackbar snackbar = Snackbar.make(rootView, message, duration);
        applyErrorStyle(snackbar, rootView);
        snackbar.show();
    }

    private static void applyErrorStyle(Snackbar snackbar, View rootView) {
        int textColor = ContextCompat.getColor(rootView.getContext(), R.color.on_error_container);
        int bgColor = ContextCompat.getColor(rootView.getContext(), R.color.error_container);

        snackbar.setTextColor(textColor);
        snackbar.setBackgroundTint(bgColor);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);

        Drawable icon = ContextCompat.getDrawable(rootView.getContext(), R.drawable.ic_alert);
        if (icon != null) {
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            icon.setTint(textColor);
            View snackbarView = snackbar.getView();
            TextView textView = snackbarView.findViewById(
                    com.google.android.material.R.id.snackbar_text);
            if (textView != null) {
                textView.setCompoundDrawables(icon, null, null, null);
                textView.setCompoundDrawablePadding(
                        (int) (8 * rootView.getContext().getResources().getDisplayMetrics().density));
            }
        }
    }
}

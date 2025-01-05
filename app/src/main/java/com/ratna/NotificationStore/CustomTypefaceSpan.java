package com.ratna.NotificationStore;

import android.graphics.Typeface;
import android.text.style.TypefaceSpan;
import android.graphics.Paint;

public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface typeface;

    public CustomTypefaceSpan(String family, Typeface typeface) {
        super(family);
        this.typeface = typeface;
    }

    public void updateDrawState(Paint paint) {
        applyCustomTypeface(paint);
    }

    public void updateMeasureState(Paint paint) {
        applyCustomTypeface(paint);
    }

    private void applyCustomTypeface(Paint paint) {
        if (typeface != null) {
            paint.setTypeface(typeface);
        }
    }
}

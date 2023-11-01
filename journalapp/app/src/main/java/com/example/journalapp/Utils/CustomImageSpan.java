package com.example.journalapp.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.style.ImageSpan;

public class CustomImageSpan extends ImageSpan {
    private String mSource;

    public CustomImageSpan(Context context, Bitmap b, String source) {
        super(context, b);
        mSource = source;
    }

    @Override
    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }
}

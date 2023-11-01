package com.example.journalapp.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.style.ImageSpan;

/**
 * Provides additional functionality to store and retrieve the source URI of the image
 * (original does not allow a 3rd String parameter to indicate the source Uri)
 */
public class CustomImageSpan extends ImageSpan {
    private String mSource;

    /**
     * Constructor for the class
     * @param context application context
     * @param b Bitmap representation of the image
     * @param source The source URI of the image
     */

    public CustomImageSpan(Context context, Bitmap b, String source) {
        super(context, b);
        mSource = source;
    }

    /**
     * Overrides getSource method to return the source URI of the image
     * @return
     */
    @Override
    public String getSource() {
        return mSource;
    }

    /**
     * Sets the source URI for the image
     * @param source
     */
    public void setSource(String source) {
        mSource = source;
    }
}

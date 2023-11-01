package com.example.journalapp.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for custom classes pertaining to dealing with converting file types to HTML Strings for
 * database sending and retrieval
 */
public class NoteMediaHandler {

    /**
     * Custom implementation of Html.toHtml that supports imageSpans
     * @param spannableContent
     * @return
     */
    public static String spannableToHtml(Spannable spannableContent){

        // retrieve a copy of the input Spannable content
        SpannableStringBuilder spannable = new SpannableStringBuilder(spannableContent);

        // retrieve all imageSpans
        ImageSpan[] imageSpans = spannable.getSpans(0, spannable.length(), ImageSpan.class);

        // iterate over each imagespan and replace it with corresponding HTML tags
        for (ImageSpan imageSpan : imageSpans){
            int start = spannable.getSpanStart(imageSpan);
            int end = spannable.getSpanEnd(imageSpan);
            String imageUri = imageSpan.getSource();

            // Check if the imageUri is null or empty
            if (imageUri == null || imageUri.isEmpty()) {
                Log.w("spannableToHtml", "Encountered ImageSpan with null or empty source.");
                continue;
            }

            String imgTag = "<img src=\"" + imageUri + "\">";
            spannable.replace(start, end, imgTag); // replace imageSpan with tags
        }

        return Html.toHtml(spannable); // return converted html string

    }

    /**
     * Custom implementation of Html.fromHtml that supports imageSpans
     * Converts HTML strings containing <img> tags to corresponding Spannables
     * Usage: Spannable result = YourUtilityClass.htmlToSpannable(this, yourHtmlString);
     * @param context
     * @param html
     * @return Spannable representation of the provided HTML String
     * @throws FileNotFoundException
     */
    public static Spannable htmlToSpannable(Context context, String html) throws FileNotFoundException{
        Spannable spannable = (Spannable) Html.fromHtml(html);
        Log.d("ConversionCheck", "Initial Spannable: " + spannable.toString());

        // Pattern to match <img> tags and extract src value
        Pattern pattern = Pattern.compile("<img src=\"(.*?)\">");
        Matcher matcher = pattern.matcher(html);

        // Remove existing ImageSpans from the Spannable
        ImageSpan[] existingImageSpans = spannable.getSpans(0, spannable.length(), ImageSpan.class);
        for (ImageSpan span : existingImageSpans) {
            spannable.removeSpan(span);
        }

        int lastEndPosition = 0; // keep track of last position where imageSpan was inserted

        // iterate over each <img> tag found in the HTML string
        while (matcher.find()) {
            Log.d("ImageRetrieval", "Found a match: " + matcher.group());


            String imageUriStr = matcher.group(1);
            Uri imageUri = Uri.parse(imageUriStr);
            Log.d("ImageRetrieval", "Retrieving image with URI: " + imageUri.toString());

            // Check if the file exists
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    Log.d("FileCheck", "File exists using content URI.");
                    inputStream.close();
                } else {
                    Log.e("FileCheck", "File doesn't exist using content URI.");
                }
            } catch (FileNotFoundException e) {
                Log.e("FileCheck", "File not found using content URI.", e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            context.grantUriPermission(context.getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION); // scoped storage

            // Decode the image from provided URI into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));

            // if Bitmap was decoded:
            if (bitmap != null) {
                Log.d("ImageRetrieval", "Bitmap decoded with dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());

                // Calculate the dimensions of the image to fit the screen width (same as NewNoteActivity.insertImageIntoText)
                int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                int originalWidth = bitmap.getWidth();
                int originalHeight = bitmap.getHeight();
                int scaledHeight = (int) ((float) originalHeight * ((float) screenWidth / originalWidth));
                bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, scaledHeight, true);

                // Create new imageSpan using scaled bitmap and image source Uri
                ImageSpan imageSpan = new CustomImageSpan(context, bitmap, imageUri.toString()); // Use the bitmap to create the ImageSpan
                Log.d("ImageRetrieval", "ImageSpan created with source: " + imageSpan.getSource());

                // Define the placeholder for the image in the spannable content (might have to be changed in future for different filetypes)
                String placeholder = "\uFFFC"; // This represents the Object Replacement Character (used for objects like images in text)

                // find pos of image placeholder and when found, replace with imagespan
                int start = spannable.toString().indexOf(placeholder, lastEndPosition);
                if (start != -1) {
                    int end = start + placeholder.length();
                    spannable.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    lastEndPosition = end; // Update the last end position
                    Log.d("ImageRetrieval", "Replacing placeholder from position: " + start + " to " + end);
                } else {
                    Log.e("ImageRetrieval", "Failed to decode bitmap from URI: " + imageUri.toString());
                }
            } else{
                Log.e("ImageRetrieval", "Placeholder not found in spannable.");
            }
        }

        return spannable;
    }
}


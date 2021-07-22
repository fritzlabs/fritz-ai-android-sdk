package ai.fritz.aistudio.controllers;

import android.text.TextUtils;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PreviewSizeController {
    private static final String TAG = PreviewSizeController.class.getSimpleName();
    private static Size lowerThreshold = new Size(400, 400);
    private static Size upperThreshold = new Size(1000, 1000);
    private int viewHeight;
    private int viewWidth;

    public PreviewSizeController(int viewHeight, int viewWidth) {
        this.viewHeight = viewHeight;
        this.viewWidth = viewWidth;
    }

    public Size calculateOptimalSize(final Size[] possibleDisplays) {
        // Throw an error if no displays are possible
        if (possibleDisplays.length < 1) {
            throw new RuntimeException("No possible display sizes found");
        }

        final double screenRatio = ((double) viewHeight) / viewWidth;
        List<Size> validSizes = new ArrayList<>(); // Proper size

        // Filters display sizes that are outside the bounds
        for (final Size option : possibleDisplays) {
            if ((option.getWidth() >= lowerThreshold.getWidth() && option.getHeight() >= lowerThreshold.getHeight())
                    && (option.getWidth() <= upperThreshold.getWidth() && option.getHeight() <= upperThreshold.getHeight())) {
                validSizes.add(option);
            }
        }

        Log.d(TAG, "Valid preview sizes: [" + TextUtils.join(", ", possibleDisplays) + "]");

        // Chooses the display size with the close screen ratio
        if (validSizes.size() > 0) {
            Size optimalSize = validSizes.get(0);
            for (final Size option: validSizes) {
                double currentRatio = ((double) option.getWidth()) / option.getHeight();
                double optimalRatio = ((double) optimalSize.getWidth()) / optimalSize.getHeight();
                if (Math.abs(currentRatio - screenRatio) <= Math.abs(optimalRatio - screenRatio)) {
                    optimalSize = option;
                }
            }
            Log.d(TAG, "Optimal Dimensions: " + optimalSize.getWidth() + "x" + optimalSize.getHeight());
            return optimalSize;
        }

        // Chooses the largest size if no displays are valid.
        return possibleDisplays[0];
    }


    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(final Size lhs, final Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }
}

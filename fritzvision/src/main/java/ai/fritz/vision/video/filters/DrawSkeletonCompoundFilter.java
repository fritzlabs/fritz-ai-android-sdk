package ai.fritz.vision.video.filters;

import android.graphics.Bitmap;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.FritzVisionPoseResult;
import ai.fritz.vision.video.FritzVisionImageFilter;

/**
 * Filter for drawing human pose skeletons on people.
 */
public class DrawSkeletonCompoundFilter extends FritzVisionImageFilter {

    private FritzVisionPosePredictor predictor;

    public DrawSkeletonCompoundFilter(FritzVisionPosePredictor predictor) {
        this.predictor = predictor;
    }

    @Override
    public FilterCompositionMode getCompositionMode() {
        return FilterCompositionMode.COMPOUND_WITH_PREVIOUS_OUTPUT;
    }

    @Override
    public FritzVisionImage processImage(FritzVisionImage image) {
        FritzVisionPoseResult poseResult = predictor.predict(image);
        Bitmap skeletons = image.overlaySkeletons(poseResult.getPoses());
        return FritzVisionImage.fromBitmap(skeletons);
    }
}

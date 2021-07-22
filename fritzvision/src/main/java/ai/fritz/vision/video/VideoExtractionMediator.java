package ai.fritz.vision.video;

import ai.fritz.vision.ByteImage;
import ai.fritz.vision.FritzVisionImage;

class VideoExtractionMediator extends VideoDecodeMediator<FritzVisionImage> {

    private int currentFrameCount = 0;

    VideoExtractionMediator(int targetFrameCount, FritzVisionImageFilter[] filters) {
        super(targetFrameCount, filters);
    }

    @Override
    void transferData(ByteImage image, long timestampUs) {
        FritzVisionImage visionImage = FritzVisionImage.applyingFilters(image, filters);
        currentFrameCount++;
        if (progressCallback != null) {
            progressCallback.onProgress(visionImage);
            if (currentFrameCount == targetFrameCount) {
                release();
                progressCallback.onComplete();
            }
        }
    }

    @Override
    void transferData(byte[] data, long timestampUs) {
        throw new IllegalStateException("Not configured for decoding raw byte data.");
    }
}

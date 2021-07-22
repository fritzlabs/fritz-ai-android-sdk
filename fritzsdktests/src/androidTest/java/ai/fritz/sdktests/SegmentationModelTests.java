package ai.fritz.sdktests;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.imagesegmentation.BlendMode;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictorOptions;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationResult;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test prediction on all image segmentation models
 */
@RunWith(AndroidJUnit4.class)
public class SegmentationModelTests extends BaseFritzTest {
    private static final String TAG = SegmentationModelTests.class.getSimpleName();

    @Test
    public void testHairSegmentationModel() {

        for (ModelVariant variant : ModelVariant.values()) {
            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getHairSegmentationOnDeviceModel(variant);
            FritzVisionSegmentationResult segmentResult = validatePrediction(TestingAsset.GIRL, onDeviceModel);
            Bitmap alphaMask = segmentResult.buildSingleClassMask(MaskClass.HAIR);
            assertNotNull(alphaMask);
        }
    }

    @Test
    public void testPeopleSegmentationModel() {
        for (ModelVariant variant : ModelVariant.values()) {
            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getPeopleSegmentationOnDeviceModel(variant);
            FritzVisionSegmentationResult segmentResult = validatePrediction(TestingAsset.PERSON, onDeviceModel);
            Bitmap alphaMask = segmentResult.buildSingleClassMask(MaskClass.PERSON, 180, .8f, .7f);
            assertNotNull(alphaMask);
        }
    }

    @Test
    public void testPetSegmentationModel() {
        for (ModelVariant variant : ModelVariant.values()) {
            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getPetSegmentationOnDeviceModel(variant);
            FritzVisionSegmentationResult segmentResult = validatePrediction(TestingAsset.CAT, onDeviceModel);
            Bitmap alphaMask = segmentResult.buildSingleClassMask(MaskClass.PET);
            assertNotNull(alphaMask);
        }
    }

    @Test
    public void testSkySegmentationModel() {
        for (ModelVariant variant : ModelVariant.values()) {
            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(variant);
            FritzVisionSegmentationResult segmentResult = validatePrediction(TestingAsset.SKY, onDeviceModel);
            Bitmap alphaMask = segmentResult.buildSingleClassMask(MaskClass.SKY);
            assertNotNull(alphaMask);
        }
    }

    @Test
    public void testLivingRoomSegmentationModel() {
        for (ModelVariant variant : ModelVariant.values()) {
            //TODO: Fix this
            if (variant == ModelVariant.ACCURATE) {
                continue;
            }
            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(variant);
            FritzVisionSegmentationResult segmentResult = validatePrediction(TestingAsset.LIVING_ROOM, onDeviceModel);
            Bitmap alphaMask = segmentResult.buildSingleClassMask(MaskClass.WINDOW);
            assertNotNull(alphaMask);
        }
    }

    @Test
    public void testOutdoorSegmentationModel() {
        for (ModelVariant variant : ModelVariant.values()) {
            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(variant);
            FritzVisionSegmentationResult segmentResult = validatePrediction(TestingAsset.OUTDOOR, onDeviceModel);
            Bitmap alphaMask = segmentResult.buildSingleClassMask(MaskClass.BUILDING_EDIFICE);
            assertNotNull(alphaMask);
        }
    }

    @Test
    public void testCreateMaskedBitmapNone() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.SKY);
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.FAST);
        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap noneMask = segmentResult.buildSingleClassMask(MaskClass.NONE);
        assertNotNull(noneMask);
        predictor.close();
    }

    @Test
    public void testCreateMaskedBitmap() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.SKY);
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap skyMaskedBitmap = segmentResult.buildSingleClassMask(MaskClass.SKY);
        assertNotNull(skyMaskedBitmap);
        predictor.close();
    }

    @Test
    public void testBuildSingleClassMask() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.SKY);
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap skyMask = segmentResult.buildSingleClassMask(MaskClass.SKY);
        assertNotNull(skyMask);
        predictor.close();
    }

    @Test
    public void testBuildingBlurredSingleClassMask() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.GIRL);
        FritzVisionSegmentationPredictorOptions options = new FritzVisionSegmentationPredictorOptions();
        options.confidenceThreshold = .3f;

        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getHairSegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, options);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap maskBitmap = segmentResult.buildSingleClassMask(MaskClass.HAIR, 255, 1, options.confidenceThreshold, Color.RED, 25f);
        assertNotNull(maskBitmap);
        predictor.close();
    }

    @Test
    public void testBuildMultiClassMask() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.LIVING_ROOM);
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getLivingRoomSegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap livingRoomMask = segmentResult.buildMultiClassMask();
        assertNotNull(livingRoomMask);
        predictor.close();
    }

    @Test
    public void testBuildBlurredMultiClassMask() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.PERSON);
        FritzVisionSegmentationPredictorOptions options = new FritzVisionSegmentationPredictorOptions();
        options.confidenceThreshold = .3f;

        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getPeopleSegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap peopleMask = segmentResult.buildMultiClassMask(255, 1, options.confidenceThreshold, 25f);
        assertNotNull(peopleMask);
        predictor.close();
    }

    @Test
    public void testBuildSingleClassMaskNone() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CLIMBING);
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap noneMask = segmentResult.buildSingleClassMask(MaskClass.NONE, 255, .5f, .5f);
        assertNotNull(noneMask);
        predictor.close();
    }

    @Test
    public void testOverlayMask() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.CLIMBING);
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        Bitmap skyMask = segmentResult.buildSingleClassMask(MaskClass.SKY, 255, .5f, .5f);
        assertNotNull(skyMask);
        predictor.close();

        Bitmap skyMaskOverlay = testImage.overlay(skyMask);
        assertNotNull(skyMaskOverlay);

        Bitmap orginalBitmap = testImage.buildOrientedBitmap();
        assertEquals(orginalBitmap.getWidth(), skyMaskOverlay.getWidth());
        assertEquals(orginalBitmap.getHeight(), skyMaskOverlay.getHeight());
    }

    @Test
    public void testCropMask() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.PERSON);
        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getPeopleSegmentationOnDeviceModel(ModelVariant.ACCURATE);

        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel);
        FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
        assertNotNull(segmentResult);
        predictor.close();

        Bitmap maskBitmap = segmentResult.buildSingleClassMask(MaskClass.PERSON, 255, .5f, .5f);
        Bitmap croppedMask = testImage.mask(maskBitmap, false);

        assertNotNull(croppedMask);

        Bitmap orginalBitmap = testImage.buildOrientedBitmap();
        assertEquals(orginalBitmap.getWidth(), croppedMask.getWidth());
        assertEquals(orginalBitmap.getHeight(), croppedMask.getHeight());

        // Cut out the extra transparent pixels
        Bitmap croppedMaskNoTrim = testImage.mask(maskBitmap);
        Bitmap croppedMaskTrimmed = testImage.mask(maskBitmap, true);
        assertNotNull(croppedMaskTrimmed);

        // Trimmed dimensions
        assertNotEquals(croppedMaskTrimmed.getHeight(), croppedMaskNoTrim.getHeight());
        assertNotEquals(croppedMaskTrimmed.getWidth(), croppedMaskNoTrim.getWidth());
    }

    @Test
    public void testBlending() {
        for (BlendMode blendMode : BlendMode.values()) {
            FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.GIRL);
            FritzVisionSegmentationPredictorOptions options = new FritzVisionSegmentationPredictorOptions();
            options.confidenceThreshold = .3f;

            SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getHairSegmentationOnDeviceModel(ModelVariant.FAST);

            FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, options);
            FritzVisionSegmentationResult segmentResult = predictor.predict(testImage);
            Bitmap maskBitmap = segmentResult.buildSingleClassMask(MaskClass.HAIR, 255, 1, options.confidenceThreshold, Color.RED);
            Bitmap blendedBitmap = testImage.blend(maskBitmap, blendMode);
            assertNotNull(blendedBitmap);
            predictor.close();
        }
    }

    private FritzVisionSegmentationResult validatePrediction(TestingAsset testingAsset, SegmentationOnDeviceModel onDeviceModel) {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, testingAsset);
        FritzVisionSegmentationPredictorOptions options = new FritzVisionSegmentationPredictorOptions();
        options.confidenceThreshold = .3f;
        FritzVisionSegmentationPredictor predictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, options);

        FritzVisionSegmentationResult segmentResult = null;
        segmentResult = predictor.predict(testImage);
        Bitmap alphaMask = segmentResult.buildMultiClassMask();
        assertNotNull(alphaMask);
        assertNotNull(segmentResult);
        predictor.close();

        return segmentResult;
    }
}

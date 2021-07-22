package ai.fritz.sdktests.styletransfer;

import android.graphics.Bitmap;
import android.util.Size;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.sdktests.BaseFritzTest;
import ai.fritz.sdktests.TestingAsset;
import ai.fritz.sdktests.TestingAssetHelper;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictorOptions;
import ai.fritz.vision.styletransfer.FritzVisionStyleResult;
import ai.fritz.vision.styletransfer.PaintingStyleModels;
import ai.fritz.vision.styletransfer.PatternStyleModels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test prediction on all style transfer models.
 */
@RunWith(AndroidJUnit4.class)
public class StyleTransferModelTests extends BaseFritzTest {

    @Test
    public void testPaintingStyleModels() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.LIVING_ROOM);
        PaintingStyleModels paintingStyleModels = FritzVisionModels.getPaintingStyleModels();
        FritzOnDeviceModel[] onDeviceModels = paintingStyleModels.getAll();

        for(FritzOnDeviceModel onDeviceModel: onDeviceModels) {
            FritzVisionStylePredictor predictor = FritzVision.StyleTransfer.getPredictor(onDeviceModel);
            FritzVisionStyleResult styleResult = predictor.predict(testImage);
            assertNotNull(styleResult);
            assertNotNull(styleResult.toBitmap());
            predictor.close();
        }
    }

    @Test
    public void testPatternStyleModels() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.LIVING_ROOM);
        PatternStyleModels patternStyleModels = FritzVisionModels.getPatternStyleModels();
        FritzOnDeviceModel[] onDeviceModels = patternStyleModels.getAll();

        for(FritzOnDeviceModel onDeviceModel: onDeviceModels) {
            FritzVisionStylePredictor predictor = FritzVision.StyleTransfer.getPredictor(onDeviceModel);
            FritzVisionStyleResult styleResult = predictor.predict(testImage);
            assertNotNull(styleResult);
            assertNotNull(styleResult.toBitmap());
            predictor.close();
        }
    }

    @Test
    public void testStyleTransferModelNoResize() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        PaintingStyleModels paintingStyleModels = FritzVisionModels.getPaintingStyleModels();

        FritzOnDeviceModel[] onDeviceModels = paintingStyleModels.getAll();
        FritzVisionStylePredictor predictor = FritzVision.StyleTransfer.getPredictor(onDeviceModels[0]);
        FritzVisionStyleResult styleResult = predictor.predict(testImage);
        Bitmap bitmap = styleResult.toBitmap();
        assertEquals(bitmap.getHeight(), 400);
        assertEquals(bitmap.getWidth(), 300);
        predictor.close();
    }

    @Test
    public void testStyleTransferModelResize() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.FAMILY);
        PaintingStyleModels paintingStyleModels = FritzVisionModels.getPaintingStyleModels();

        FritzOnDeviceModel[] onDeviceModels = paintingStyleModels.getAll();
        FritzVisionStylePredictorOptions options = new FritzVisionStylePredictorOptions();
        options.resize = true;
        FritzVisionStylePredictor predictor = FritzVision.StyleTransfer.getPredictor(onDeviceModels[0], options);
        FritzVisionStyleResult styleResult = predictor.predict(testImage);
        Bitmap bitmap = styleResult.toBitmap();
        assertEquals(bitmap.getHeight(), testImage.getHeight());
        assertEquals(bitmap.getWidth(), testImage.getWidth());
        predictor.close();
    }

    @Test
    public void testCustomStyleTransferModel() {
        FritzVisionImage testImage = TestingAssetHelper.getVisionImageForAsset(appContext, TestingAsset.LIVING_ROOM);

        // Use a model with a different input / output size.
        CustomStyleTransferModel onDeviceModel = new CustomStyleTransferModel();

        FritzVisionStylePredictorOptions options = new FritzVisionStylePredictorOptions();

        FritzVisionStylePredictor predictor = FritzVision.StyleTransfer.getPredictor(onDeviceModel, options);
        FritzVisionStyleResult styleResult = predictor.predict(testImage);
        Bitmap bitmap = styleResult.toBitmap();
        assertEquals(bitmap.getHeight(), 200);
        assertEquals(bitmap.getWidth(), 100);
        predictor.close();
    }
}

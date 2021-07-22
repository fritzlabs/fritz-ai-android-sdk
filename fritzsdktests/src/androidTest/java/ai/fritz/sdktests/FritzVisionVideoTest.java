package ai.fritz.sdktests;

import android.graphics.Bitmap;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.imagesegmentation.BlendMode;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.poseestimation.FritzVisionPosePredictor;
import ai.fritz.vision.poseestimation.PoseOnDeviceModel;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.video.ExportVideoOptions;
import ai.fritz.vision.video.FritzVisionImageFilter;
import ai.fritz.vision.video.FritzVisionVideo;
import ai.fritz.vision.video.FrameProcessingOptions;
import ai.fritz.vision.video.filters.DrawBoxesCompoundFilter;
import ai.fritz.vision.video.filters.DrawSkeletonCompoundFilter;
import ai.fritz.vision.video.filters.StylizeImageCompoundFilter;
import ai.fritz.vision.video.filters.imagesegmentation.MaskBlendCompoundFilter;
import ai.fritz.vision.video.filters.imagesegmentation.MaskCutOutOverlayFilter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class FritzVisionVideoTest extends BaseFritzTest {
    // VIDEO INFORMATION:
    // Frame rate: 60fps
    // Sample rate: 44100 Hz
    // Total duration: ~5s
    // Total frames: 308

    private static final int TIMEOUT_SECONDS = 60;
    private SegmentationOnDeviceModel hairModel;
    private SegmentationOnDeviceModel peopleModel;
    private ObjectDetectionOnDeviceModel objectModel;
    private PoseOnDeviceModel poseModel;
    private FritzOnDeviceModel styleModel;

    private String testFile;
    private FritzVisionSegmentationPredictor hairPredictor;
    private FritzVisionSegmentationPredictor peoplePredictor;
    private FritzVisionObjectPredictor objectPredictor;
    private FritzVisionPosePredictor posePredictor;
    private FritzVisionStylePredictor stylePredictor;

    @Override
    public void setup() {
        super.setup();
        testFile = TestingAssetHelper.getFilePathForAsset(appContext, TestingAsset.STEVEN_VIDEO);
        peopleModel = FritzVisionModels.getPeopleSegmentationOnDeviceModel(ModelVariant.FAST);
        objectModel = FritzVisionModels.getObjectDetectionOnDeviceModel();
        poseModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.FAST);
        styleModel = FritzVisionModels.getPaintingStyleModels().getStarryNight();
        hairModel = FritzVisionModels.getHairSegmentationOnDeviceModel(ModelVariant.FAST);

        hairPredictor = FritzVision.ImageSegmentation.getPredictor(hairModel);
        peoplePredictor = FritzVision.ImageSegmentation.getPredictor(peopleModel);
        objectPredictor = FritzVision.ObjectDetection.getPredictor(objectModel);
        posePredictor = FritzVision.PoseEstimation.getPredictor(poseModel);
        stylePredictor = FritzVision.StyleTransfer.getPredictor(styleModel);
    }

    @Test
    public void testTotalFrames() {
        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        assertEquals(308, fritzVideo.getTotalFrameCount());
    }

    @Test
    public void testFrameRate() {
        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        assertEquals(60, fritzVideo.getFrameRate());
    }

    @Test
    public void testFrameNoFilters() {
        final CountDownLatch latch = new CountDownLatch(1);

        FrameProcessingOptions options = new FrameProcessingOptions();
        options.numFrames = 1;
        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
    }

    @Test
    public void testFrameNoFiltersOffset() {
        final CountDownLatch latch = new CountDownLatch(1);

        FrameProcessingOptions options = new FrameProcessingOptions();
        options.numFrames = 1;
        options.startingFrameOffset = 250;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);

        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
    }

    @Test
    public void testFrameHair() {
        final CountDownLatch latch = new CountDownLatch(1);

        FrameProcessingOptions options = new FrameProcessingOptions();
        options.numFrames = 1;
        FritzVisionVideo fritzVideo = new FritzVisionVideo(
                testFile,
                new MaskBlendCompoundFilter(hairPredictor, MaskClass.HAIR, BlendMode.SOFT_LIGHT)
        );
        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
    }

    @Test
    public void testFrameObjectDetection() {
        final CountDownLatch latch = new CountDownLatch(1);

        FrameProcessingOptions options = new FrameProcessingOptions();
        options.numFrames = 1;
        FritzVisionVideo fritzVideo = new FritzVisionVideo(
                testFile,
                new DrawBoxesCompoundFilter(objectPredictor)
        );
        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
    }

    @Test
    public void testFramePose() {
        final CountDownLatch latch = new CountDownLatch(1);

        FrameProcessingOptions options = new FrameProcessingOptions();
        options.numFrames = 1;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(
                testFile,
                new DrawSkeletonCompoundFilter(posePredictor)
        );
        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
    }

    @Test
    public void testFrameBuildAndOverlay() {
        final CountDownLatch latch = new CountDownLatch(1);

        FritzVisionImageFilter[] filters = new FritzVisionImageFilter[]{
                new StylizeImageCompoundFilter(stylePredictor),
                new MaskCutOutOverlayFilter(peoplePredictor, MaskClass.PERSON)
        };

        FrameProcessingOptions options = new FrameProcessingOptions();
        options.numFrames = 1;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile, filters);
        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
    }

    @Test
    public void testFrameInterval() {
        final CountDownLatch latch = new CountDownLatch(1);

        List<Bitmap> frames = new ArrayList<>();

        FrameProcessingOptions options = new FrameProcessingOptions();
        options.numFrames = 5;
        options.frameInterval = 10;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
                frames.add(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
        assertEquals(5, frames.size());
    }

    @Test
    public void testFrameIntervalOffset() {
        final CountDownLatch latch = new CountDownLatch(1);

        List<Bitmap> frames = new ArrayList<>();
        FrameProcessingOptions options = new FrameProcessingOptions(10, 5, 250);
        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);

        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                Bitmap image = response.buildOrientedBitmap();
                assertNotNull(image);
                frames.add(image);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }
        assertEquals(5, frames.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFrameInvalidMinFrames() {
        FrameProcessingOptions options = new FrameProcessingOptions(5, 1, 305);
        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);

        // Sum of start frame and interval is not less than the number of frames in the video
        fritzVideo.getFrames(options, new FritzVisionVideo.FrameProgressCallback() {
            @Override
            public void onProgress(FritzVisionImage response) {
                fail("Extraction should not start");
            }

            @Override
            public void onComplete() {
                fail("Extraction should not start");
            }
        });
    }

    @Test
    public void testExport() {
        final CountDownLatch latch = new CountDownLatch(1);

        File exportFile = null;
        try {
            exportFile = File.createTempFile("exportTest", null);
        } catch (IOException e) {
            fail("Unable to create file");
        }
        assertNotNull(exportFile);
        assertEquals(0, exportFile.length());

        final int[] progressCount = {0};
        ExportVideoOptions options = new ExportVideoOptions();
        options.numFrames = 25;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        fritzVideo.export(exportFile.getAbsolutePath(), options, new FritzVisionVideo.ExportProgressCallback() {
            @Override
            public void onProgress(Float response) {
                assertTrue(response >= 0 && response <= 1);
                progressCount[0]++;
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }

        // Exactly the given number of frames were processed
        assertEquals(25, progressCount[0]);
        assertTrue(exportFile.length() > 0);
        exportFile.deleteOnExit();
    }

    @Test
    public void testExportWithOffsetStride() {
        final CountDownLatch latch = new CountDownLatch(1);

        File exportFile = null;
        try {
            exportFile = File.createTempFile("exportTestOutOfBounds", null);
        } catch (IOException e) {
            fail("Unable to create file");
        }
        assertNotNull(exportFile);
        assertEquals(0, exportFile.length());

        final int[] progressCount = {0};

        ExportVideoOptions options = new ExportVideoOptions();
        options.frameInterval = 13;
        options.startingFrameOffset = 71;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        fritzVideo.export(exportFile.getAbsolutePath(), options, new FritzVisionVideo.ExportProgressCallback() {
            @Override
            public void onProgress(Float response) {
                assertTrue(response >= 0 && response <= 1);
                progressCount[0]++;
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }

        // As many frames as possible were processed given the start frame and interval
        assertEquals(18, progressCount[0]);
        assertTrue(exportFile.length() > 0);
        exportFile.deleteOnExit();
    }

    @Test
    @Ignore
    public void testExportWithAudio() {
        final CountDownLatch latch = new CountDownLatch(1);

        File exportFile = null;
        try {
            exportFile = File.createTempFile("exportTestWithAudio", null);
        } catch (IOException e) {
            fail("Unable to create file");
        }
        assertNotNull(exportFile);
        assertEquals(0, exportFile.length());

        // Enable audio
        ExportVideoOptions options = new ExportVideoOptions();
        options.frameInterval = 2;
        options.copyAudio = true;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        fritzVideo.export(exportFile.getAbsolutePath(), options, new FritzVisionVideo.ExportProgressCallback() {
            @Override
            public void onProgress(Float response) {
                assertTrue(response >= 0 && response <= 1);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted!");
        }

        assertTrue(exportFile.length() > 0);
        exportFile.deleteOnExit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportInvalidBounds() {

        File exportFile = null;
        try {
            exportFile = File.createTempFile("exportTestInvalidBounds", null);
        } catch (IOException e) {
            fail("Unable to create file");
        }
        assertNotNull(exportFile);
        assertEquals(0, exportFile.length());

        // A video can not process a negative amount of frames
        ExportVideoOptions options = new ExportVideoOptions();
        options.numFrames = -1;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        fritzVideo.export(exportFile.getAbsolutePath(), options, new FritzVisionVideo.ExportProgressCallback() {
            @Override
            public void onProgress(Float response) {
                fail("Export should not start");
            }

            @Override
            public void onComplete() {
                fail("Export should not start");
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExportInvalidFrameRate() {

        File exportFile = null;
        try {
            exportFile = File.createTempFile("exportTestInvalidFrameRate", null);
        } catch (IOException e) {
            fail("Unable to create file");
        }
        assertNotNull(exportFile);
        assertEquals(0, exportFile.length());

        // A video can not have a negative frame rate scale
        ExportVideoOptions options = new ExportVideoOptions();
        options.frameRateScale = -1;

        FritzVisionVideo fritzVideo = new FritzVisionVideo(testFile);
        fritzVideo.export(exportFile.getAbsolutePath(), options, new FritzVisionVideo.ExportProgressCallback() {
            @Override
            public void onProgress(Float response) {
                fail("Export should not start");
            }

            @Override
            public void onComplete() {
                fail("Export should not start");
            }
        });
    }
}

# FritzSDK Change Log

`Fritz` follows [Semantic Versioning](http://semver.org/)

---

## [6.0.4]
- Fix image lableing output normalization..

## [6.0.3]
- Fix issue with no internet connectivity.

## [6.0.1]

- Pose Estimation Skeletons no longer need connected parts for drawing.

## [6.0.0]

- Remove TFM library.
- Merge the Fritz TFL interpreter with the core library.
- Refactored core code to use Kotlin.
- Moved session data into a manager class
- Added in ability to record annotations from object detection predictors
- Added in ability to record annotations from pose estimation predictors
- Added in ability to record annotations from image segmentation predictors
- Added in ability to record annotations from image labeling predictors
- Fixed issue with landscape mode orientation being mirrored on the front facing camera
- Removed deprecated ImageRotation methods in favor of getting the ImageOrientation from the camera view
- Fix pose estimation post-processing for custom pose models.

```diff
-        ImageRotation imageRotation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
+        ImageOrientation orientation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);


-        FritzVisionImage fritzVisionImage = FritzVisionImage.fromMediaImage(image, imageRotation);
+        FritzVisionImage fritzVisionImage = FritzVisionImage.fromMediaImage(image, imageOrientation);
```

- Change skeleton for Pose estimation to include a label (used in the Data Collection System)

```diff
-        Skeleton humanSkeleton = new Skeleton(PART_NAMES, CONNECTED_PART_NAMES, POSE_CHAIN);
+        Skeleton humanSkeleton = new Skeleton("Human", PART_NAMES, CONNECTED_PART_NAMES, POSE_CHAIN);
```

## [6.0.0 (beta.9)]

- Fix pose estimation post-processing for custom pose models.

## [6.0.0 (beta.2)]

- Remove TFM library.
- Merge the Fritz TFL interpreter with the core library.
- Refactored core code to use Kotlin.
- Moved session data into a manager class
- Added in ability to record annotations from object detection and pose estimation predictors.
- Fixed issue with landscape mode orientation being mirrored on the front facing camera.
- Removed deprecated ImageRotation methods in favor of getting the ImageOrientation from the camera view.

```diff
-        ImageRotation imageRotation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
+        ImageOrientation orientation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);


-        FritzVisionImage fritzVisionImage = FritzVisionImage.fromMediaImage(image, imageRotation);
+        FritzVisionImage fritzVisionImage = FritzVisionImage.fromMediaImage(image, imageOrientation);
```

- Change skeleton for Pose estimation to include a label (used in the Data Collection System)

```diff
-        Skeleton humanSkeleton = new Skeleton(PART_NAMES, CONNECTED_PART_NAMES, POSE_CHAIN);
+        Skeleton humanSkeleton = new Skeleton("Human", PART_NAMES, CONNECTED_PART_NAMES, POSE_CHAIN);
```

## [5.1.2]

- Add null check on custom model service

## [5.1.0]

- Adding support for encrypted models.
- Get orientation depending on camera facing direction. (note: specifying an image rotation when initializing `FritzVisionImage` is deprecated.)

```diff
-        ImageRotation imageRotation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
+        ImageOrientation orientation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);


-        FritzVisionImage fritzVisionImage = FritzVisionImage.fromMediaImage(image, imageRotation);
+        FritzVisionImage fritzVisionImage = FritzVisionImage.fromMediaImage(image, imageOrientation);
```

## [5.0.1]

- change method name for fetching a human pose model to `FritzVisionModels.getHumanPoseEstimationManagedModel(ModelVariant.FAST)`.

## [5.0.0]

**Changes:**

- Updated image segmentation models to run on the GPU resulting in significant speedups for inference times.
- Added the `FritzVisionVideo` API
- Removed the dependency on fritz-vision from the model dependencies.
- Added ability to initialize `FritzOnDeviceModel` with a json file stored in the assets folder.
- Updated Pose Estimation API to work for custom models.
- Moved pretrained model declarations in separate packages to `FritzVisionModels` defined in "ai.fritz:vision"

**Migrating from 4.x.x to 5.x.x:**

Initializing pretrained models (must be used with model dependencies >= 3.0.0)

In your app/build.gradle:

```diff
dependencies {
-   implementation "ai.fritz:vision:4.2.2"
+   implementation "ai.fritz:vision:5.0.0-beta.3"

-   implementation "ai.fritz:vision-sky-segmentation-model-fast:2.0.0"
+   implementation "ai.fritz:vision-sky-segmentation-model-fast:3.0.0-beta.1"
}
```

Code Changes:

Image Segmentation (each model has 3 variants)

```diff
-        SkySegmentationOnDeviceModelFast onDeviceModel = new SkySegmentationOnDeviceModelFast();
+        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.FAST);
-        SkySegmentationOnDeviceModelSmall onDeviceModel = new SkySegmentationOnDeviceModelSmall();
+        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.SMALL);
-        SkySegmentationOnDeviceModelSmall onDeviceModel = new SkySegmentationOnDeviceModelAccurate();
+        SegmentationOnDeviceModel onDeviceModel = FritzVisionModels.getSkySegmentationOnDeviceModel(ModelVariant.ACCURATE)

-        FritzManagedModel managedModel = new SkySegmentationManagedModelFast();
+        SegmentationManagedModel managedModel = FritzVisionModels.getSkySegmentationManagedModel(ModelVariant.FAST);
```

Pose Estimation (each model has 3 variants)

```diff
-        FritzOnDeviceModel onDeviceModel = new PoseEstimationOnDeviceModelSmall();
+        PoseOnDeviceModel onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.SMALL);
-        FritzOnDeviceModel onDeviceModel = new PoseEstimationOnDeviceModelFast();
+        PoseOnDeviceModel onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.FAST);
-        FritzOnDeviceModel onDeviceModel = new PoseEstimationOnDeviceModelAccurate();
+        PoseOnDeviceModel onDeviceModel = FritzVisionModels.getHumanPoseEstimationOnDeviceModel(ModelVariant.ACCURATE);

-        FritzManagedModel managedModel = new PoseEstimationManagedModelFast();
+        PoseManagedModel managedModel = FritzVisionModels.getHumanPoseEstimationManagedModel(ModelVariant.FAST);
```

Style Transfer (no model variants)

```diff
-        FritzOnDeviceModel[] paintingStyles = PaintingStyles.getAll();
-        FritzOnDeviceModel starryNightModel = PaintingStyles.STARRY_NIGHT;
-        FritzOnDeviceModel[] patternStyles = PatternStyles.getAll();
-        FritzOnDeviceModel comicModel = PatternStyles.COMIC;
+        PaintingStyleModels paintingModels = FritzVisionModels.getPaintingStyleModels();
+        FritzOnDeviceModel[] styles = paintingModels.getAll();
+        FritzOnDeviceModel starryNightModel = paintingModels.getStarryNight();
+        PatternStyleModels patternModels = FritzVisionModels.getPatternStyleModels();
+        FritzOnDeviceModel[] styles = patternModels.getAll();
+        FritzOnDeviceModel comicModel = patternModels.getComic();
```

Image Labeling (no model variants)

```diff
-        FritzOnDeviceModel onDeviceModel = new ImageLabelOnDeviceModelFast();
+        LabelingOnDeviceModel onDeviceModel = FritzVisionModels.getImageLabelingOnDeviceModel();

-        FritzManagedModel managedModel = new ImageLabelOnDeviceModelFast();
+        LabelingManagedModel managedModel = FritzVisionModels.getLabelingManagedModel();
```

Object Detection (no model variants)

```diff
-        FritzOnDeviceModel onDeviceModel = new ObjectDetectionOnDeviceModel();
+        ObjectDetectionOnDeviceModel onDeviceModel = FritzVisionModels.getObjectDetectionOnDeviceModel();

-        FritzManagedModel managedModel = new ObjectDetectionManagedModel();
+        ObjectDetectionManagedModel managedModel = FritzVisionModels.getObjectDetectionManagedModel();
```

## [4.2.4]

1. ByteImage no longer takes in a buffer
2. Decoding and Encoding video (beta)

## [4.2.3]

1. ByteImage can be passed to FritzVisionImage which reduces unnecessary conversions.

## [4.2.2]

1. Fix up bug with scrambled images.

## [4.0.0]

In the latest release, we've several improvements listed below.

This repo on github moving forward will be deprecated in favor of hosting our SDK in a new maven repository:

Change:

maven {
url 'https://raw.github.com/fritzlabs/fritz-repository/master'
}

To:

maven {
url "https://fritz.mycloudrepo.io/public/repositories/android"
}

Changes

- Adding support for model variants (fast, accurate, small) so you can build the perfect experience for your users.
  - Fast models are optimized for runtime performance with an accuracy tradeoff. This should be used in cases where model predictions need to happen quickly (e.g video processing, live preview, etc). This comes with a tradeoff in accuracy.
  - Accurate models are optimized to display the best model prediction with a speed tradeoff. This should be used in cases where you're dealing with still images (i.e photo editing)
  - Small models are optimized for model size at the cost of accuracy. This should be used in cases where developers are cautious of bloating their apps with models.
- Models now have their own versioning system separate from the SDK and follow semantic versioning. This enables Fritz to release new versions of models without changing any existing user experiences.
- Removing deprecated methods for the result classes.
- 2x speed improvement for image processing with Renderscript.
- Adding TFL support for CPU threads, GPU Delegate, and NNAPI
- Improved rendering on Surface views
- Improve segmentation blend mode (hair coloring)

Migrating from 3.x.x to 4.x.x

**Core**

- Image rotation is now an enum.

```
// Old version
int imgRotation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, imgRotation);

// Change
ImageRotation imageRotation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
FritzVisionImage visionImage = FritzVisionImage.fromBitmap(bitmap, imageRotation);
```

**FritzVision**

- Add RenderScript support to your app

```
// In your app/build.gradle

android {
    defaultConfig {
        renderscriptTargetApi 21
        renderscriptSupportModeEnabled true
    }
}
```

- For any of the predictor options, you can now declare option in the following way:

```
// Old
FritzVisionSegmentPredictorOptions options = FritzVisionSegmentPredictorOptions.Builder()
    .targetConfidenceScore(.3f);
    .build();

// New
FritzVisionSegmentPredictorOptions options = FritzVisionSegmentPredictorOptions();
options.confidenceThreshold = .3f;
```

**Image Segmentation**

- Renaming Classes ("Segment" -> "Segmentation"):

  - FritzVisionSegmentPredictor -> FritzVisionSegmentationPredictor
  - FritzVisionSegmentResult -> FritzVisionSegmentationResult
  - FritzVisionSegmentPredictorOptions -> FritzVisionSegmentationPredictorOptions
  - MaskType -> MaskClass

- Model dependencies:
  - The libraries for models are now on separate versions, allowing for individual updates and releases on when new improvements are made. As of the release, all models are now currently on version 1.0.0.
  - Sky Segmentation:
    - Fast Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-sky-segmentation-model-fast:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new SkySegmentationManagedModelFast();
        ```
  - Pet Segmentation
    - Fast Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-pet-segmentation-model-fast:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new PetSegmentationManagedModelFast();
        ```
  - Hair Segmentation
    - Fast Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-hair-segmentation-model-fast:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new HairSegmentationManagedModelFast();
        ```
  - Living Room Segmentation
    - Fast Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-living-room-segmentation-model-fast:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new LivingRoomSegmentationManagedModelFast();
        ```
  - Outdoor Segmentation
    - Fast Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-outdoor-segmentation-model-fast:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new OutdoorSegmentationManagedModelFast();
        ```
  - People Segmentation
    - Fast Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-people-segmentation-model-fast:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new PeopleSegmentationManagedModelFast();
        ```
    - Accurate Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-people-segmentation-model-accurate:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new PeopleSegmentationManagedModelAccurate();
        ```
    - Small Variant
      - Including it on device (in app/build.gradle):
        ```
          implementation "ai.fritz:vision-people-segmentation-model-small:1.0.0"
        ```
      - Downloading it OTA:
        ```
          FritzManagedModel managedModel = new PeopleSegmentationManagedModelSmall();
        ```
- Blend Mode:

Alpha value is specified on the created mask. The class `BlendModeType` is removed.

```
// Old
BlendMode blendMode = BlendModeType.SOFT_LIGHT.create();
Bitmap maskBitmap = hairResult.buildSingleClassMask(MaskType.HAIR, blendMode.getAlpha(), 1, options.getTargetConfidenceThreshold(), maskColor);
Bitmap blendedBitmap = visionImage.blend(maskBitmap, blendMode);

// New
BlendMode blendMode = BlendMode.SOFT_LIGHT;
Bitmap maskBitmap = hairResult.buildSingleClassMask(MaskClass.HAIR, 180, 1, options.confidenceThreshol, maskColor);
Bitmap blendedBitmap = visionImage.blend(maskBitmap, blendMode);
```

## [3.0.0]

In the latest release, we've several improvements listed below. For the full API documentation, please visit: https://docs.fritz.ai/android/3.0.0/reference/packages.html.

## Changes:

1. Simplify dependencies
2. Allow for lazy loading models with a FritzManagedModel class.
3. Saved on-device models stored as a FritzOnDeviceModel
4. Download models by tags (configured in the webapp)
5. Model tags + metadata

## To Migrate from 2.x.x to 3.0.0

**Module renaming - In your app/build.gradle file, change these module names**

**2.x.x**

```
dependencies {
    // Image Labeling
    implementation "ai.fritz:vision-label:2.x.x"

    // Object Detection
    implementation "ai.fritz:vision-object:2.x.x"

    // Style Transfer
    implementation "ai.fritz:vision-style-paintings:2.x.x"

    // Image Segmentation
    implementation "ai.fritz:vision-people-segment:2.x.x"
    implementation "ai.fritz:vision-living-room-segment:2.x.x"
    implementation "ai.fritz:vision-outdoor-segment:2.x.x"
}
```

**3.x.x**

```
dependencies {
    // Image Labeling
    implementation "ai.fritz:vision-image-label-model:3.x.x"

    // Object Detection
    implementation "ai.fritz:vision-object-detection-model:3.x.x"

    // Style Transfer
    implementation "ai.fritz:vision-style-painting-models:3.x.x"

    // Image Segmentation
    implementation "ai.fritz:vision-people-segmentation-model:3.x.x"
    implementation "ai.fritz:vision-living-room-segmentation-model:3.x.x"
    implementation "ai.fritz:vision-outdoor-segmentation-model:3.x.x"
}
```

Several dependencies have been removed and the functionality is now in FritzVision

- ai.fritz:style-base
- ai.fritz:image-segmentation

**Using FritzManagedModel and FritzOnDeviceModel**

In order to provide lazy loading models, we've created 2 separate classes to define models loaded
into Vision predictors and Custom Model intepreters: FritzManagedModel and FritzOnDeviceModel.

Why we made this change?

- Decrease initial app size through lazy loading - Allow developers to manage their app size and download models over the air.
- Simplify the dependency chain - Allow developers to use only the FritzCore + FritzVision dependency in order to get started.
- Use Custom Models with the Vision API- Developers can use custom models with the existing Vision API by plugging it into an existing predictor (e.g ObjectDetection). We provide model training templates that you can use on your own training data.

**2.x.x** - You would define a predictor like so:

```
FritzVisionObjectPredictor objectPredictor = new FritzVisionObjectPredictor();
FritzVisionObjectResult objectResult = objectPredictor.predict(fritzVisionImage);
List<FritzVisionObject> visionObjects = objectResult.getVisionObjects();
```

**3.x.x** - You have 2 options of including a model for on-device inference:

1.  **Include it directly in your app build.** This increases your app size but your users will be able to access
    the model immediately once they download it from the app store.

        **Using a Vision Predictor with a FritzOnDeviceModel:**
        ```
        FritzOnDeviceModel onDeviceModel = new ObjectDetectionOnDeviceModel();
        FritzVisionObjectPredictor predictor = FritzVision.ObjectDetection.getPredictor();
        ```

        **Using a Custom Model with a FritzOnDeviceModel:**
        ```
        String modelPath = "<PATH TO YOUR MODEL FILE STORED IN THE ASSETS FOLDER>";
        String modelId = "<YOUR MODEL ID>";
        int modelVersion = 1;
        FritzOnDeviceModel onDeviceModel = new FritzOnDeviceModel(modelPath, modelId, modelVersion);
        FritzTFLiteInterpreter tflite = new FritzTFLiteInterpreter(onDeviceModel);
        ```

2.  **Lazy load the model the first time the app launches.** This reduces your initial app size when your users install it from the store, but you will have to handle the experience before the model is loaded onto the device.

    **Lazy loading a Vision Predictor:**

    ```
    // Global predictor variable
    FritzVisionObjectPredictor predictor;

    // Load your predictor
    FritzManagedModel managedModel = new ObjectDetectionManagedModel();
    FritzVision.ObjectDetection.loadPredictor(managedModel, new PredictorStatusListener<FritzVisionObjectPredictor>() {
        @Override
        public void onPredictorReady(FritzVisionObjectPredictor objectPredictor) {
            predictor = objectPredictor;
        }
    });

    // Manage access to specific features and check if the predictor is ready to use.
    if(predictor != null) {
        predictor.predict(...);
    }
    ```

    **Lazy loading a Custom Model**

    ```
    FritzManagedModel managedModel = new FritzManagedModel("<YOUR MODEL ID>");
    FritzModelManager modelManager = new FritzModelManager(managedModel);
    modelManager.loadModel(new ModelReadyListener() {
        @Override
        public void onModelReady(FritzOnDeviceModel onDeviceModel) {
            tflite = new FritzTFLiteInterpreter(onDeviceModel);
            Log.d(TAG, "Interpreter is now ready to use");
        }
    });
    ```

**Vision API changes**

**2.x.x** - Initialize the predictor directly

```
FritzVisionObjectPredictor objectPredictor = new FritzVisionObjectPredictor(options);
FritzVisionStylePredictor stylePredictor = new FritzVisionStylePredictor(options);
FritzVisionLabelPredictor labelPredictor = new FritzVisionLabelPredictor(options);
FritzVisionSegmentPredictor segmentPredictor = new FritzVisionSegmentPredictor(options);
```

**3.x.x** - Accessing Vision Predictors with a loaded model (a class that extends FritzOnDeviceModel) to use immediately.

```
FritzVisionObjectPredictor objectPredictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel, options);
FritzVisionStylePredictor stylePredictor = FritzVision.StyleTransfer.getPredictor(onDeviceModel, options);
FritzVisionLabelPredictor labelPredictor = FritzVision.ImageLabeling.getPredictor(onDeviceModel, options);
FritzVisionSegmentPredictor segmentPredictor = FritzVision.ImageSegmentation.getPredictor(onDeviceModel, options);
```

## New Features in 3.0.0

- [Pose Estimation](https://docs.fritz.ai/develop/vision/pose-estimation/android.html)
- [Tag + Metadata](https://docs.fritz.ai/develop/custom-models/tag-based/android.html)
- [Custom Models with the Vision API](https://docs.fritz.ai/develop/vision/style-transfer/android.html#how-to-customize)

## [2.0.0]

In the latest release, we've made it easier to access and draw your prediction results to a canvas. For the full API documentation, please visit: https://docs.fritz.ai/android/2.0.0/reference/packages.html.

Changes:

1. Create result classes for each predictor
2. Simplify SDK initialization
3. Rename a couple of modules (vision-label-model -> vision-label)
4. Move Bitmap helpers to BitmapUtils class
5. Use a CustomModel class instead of ModelSettings
6. Rename model management service to FritzCustomModelService
7. Use app context provided during Fritz.configure
8. Ability to create separate interpreters for predictors

#### To Migrate from 1.x.x to 2.0.0

There are several breaking changes from this release:

- Module renaming - In your app/build.gradle file, change these module names

Previous module names

```
dependencies {
    // Image Labeling
    implementation "ai.fritz:vision-label-model:1.x.x"

    // Object Detection
    implementation "ai.fritz:vision-object-model:1.x.x"

    // Image Segmentation
    implementation "ai.fritz:vision-people-segment-model:1.x.x"
    implementation "ai.fritz:vision-living-room-segment-model:1.x.x"
    implementation "ai.fritz:vision-outdoor-segment-model:${sdk_version}:1.x.x"
}
```

to

```
dependencies {
    // Image Labeling
    implementation "ai.fritz:vision-label:1.x.x"

    // Object Detection
    implementation "ai.fritz:vision-object:1.x.x"

    // Image Segmentation
    implementation "ai.fritz:vision-people-segment:1.x.x"
    implementation "ai.fritz:vision-living-room-segment:1.x.x"
    implementation "ai.fritz:vision-outdoor-segment:${sdk_version}:1.x.x"
}
```

- All FritzVisionPredictor's predict method now methods now return a `FritzVisionResult` object.

Previously for object detection

```
List<FritzVisionObject> visionObjects = objectPredictor.predict(fritzVisionImage);
```

To

```
FritzVisionObjectResult objectResult = objectPredictor.predict(fritzVisionImage);
List<FritzVisionObject> visionObjects = objectResult.getVisionObjects();
```

These classes have helper classes such as `drawBoundingBoxes` and `drawVisionImage` for your convenience.

Please refer to the documentation to see the appropriate changes for each feature: https://docs.fritz.ai/

- FritzVisionImage methods to manipulate bitmaps (scale, centerCrop, resize, etc) are now accessed in BitmapUtils.

Previously

```
Bitmap resizedBitmap = FritzVisionImage.resize(image.getBitmap(), INPUT_SIZE, INPUT_SIZE);
```

To:

```
Bitmap resizedBitmap = BitmapUtils.resize(image.getBitmap(), INPUT_SIZE, INPUT_SIZE);
```

- For custom model management, change the Job Service name in AndroidManifest.xml

Previously

```
<service
    android:name="ai.fritz.core.FritzJob"
    android:exported="true"
    android:permission="android.permission.BIND_JOB_SERVICE" />
```

To

```
<service
    android:name="ai.fritz.core.FritzCustomModelService"
    android:exported="true"
    android:permission="android.permission.BIND_JOB_SERVICE" />
```

- For those of you using custom models, ModelSettings is now deprecated in favor of a CustomModel class.

Previously

```

FritzTFLiteInterpreter.create(new ModelSettings.Builder()
    .modelId("modelId")
    .modelPath("mnist.tflite")
    .modelVersion(1).build());
)
```

To:

```

FritzTFLiteInterpreter.create(new MnistCustomModel());
)
```

To download the class for your model, please visit the webapp and under Custom Model > Your Model > SDK Instructions.

- Removed singletons for predictors that developers can allocate separate interpreters.

Previously

```
    FritzVisionObjectPredictor predictor = FritzVisionObjectPredictor.getInstance(this, options);
```

To:

```
    FritzVisionObjectPredictor predictor = new FritzVisionObjectPredictor();
```

## [1.4.0](https://github.com/fritzlabs/swift-framework/releases/tag/1.4.0)

1. Allowing the ability to add a custom style transfer model

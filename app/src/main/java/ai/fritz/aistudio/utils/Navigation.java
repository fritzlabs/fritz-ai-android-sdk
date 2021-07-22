package ai.fritz.aistudio.utils;

import android.content.Context;
import android.content.Intent;

import ai.fritz.aistudio.activities.CustomTFLiteActivity;
import ai.fritz.aistudio.activities.HairSegmentationActivity;
import ai.fritz.aistudio.activities.ImageLabelingActivity;
import ai.fritz.aistudio.activities.ObjectDetectionActivity;
import ai.fritz.aistudio.activities.PeopleSegmentationActivity;
import ai.fritz.aistudio.activities.PetSegmentationActivity;
import ai.fritz.aistudio.activities.PoseEstimationActivity;
import ai.fritz.aistudio.activities.SkySegmentationActivity;
import ai.fritz.aistudio.activities.StyleTransferActivity;
import ai.fritz.aistudio.activities.debug.BackgroundReplacementActivity;

/**
 * Navigation is a helper class for common links throughout the app.
 */
public class Navigation {

    public static void goToTFLite(Context context) {
        Intent tflite = new Intent(context, CustomTFLiteActivity.class);
        context.startActivity(tflite);
    }

    public static void goToLabelingActivity(Context context) {
        Intent labelActivity = new Intent(context, ImageLabelingActivity.class);
        context.startActivity(labelActivity);
    }

    public static void goToStyleTransfer(Context context) {
        Intent styleActivity = new Intent(context, StyleTransferActivity.class);
        context.startActivity(styleActivity);
    }

    public static void goToPeopleSegmentation(Context context) {
        Intent imgSegActivity = new Intent(context, PeopleSegmentationActivity.class);
        context.startActivity(imgSegActivity);
    }

    public static void goToObjectDetection(Context context) {
        Intent objectDetection = new Intent(context, ObjectDetectionActivity.class);
        context.startActivity(objectDetection);
    }

    public static void goToPoseEstimation(Context context) {
        Intent poseEstimation = new Intent(context, PoseEstimationActivity.class);
        context.startActivity(poseEstimation);
    }

    public static void goToHairSegmentation(Context context) {
        Intent hairActivity = new Intent(context, HairSegmentationActivity.class);
        context.startActivity(hairActivity);
    }

    public static void goToPetSegmentation(Context context) {
        Intent petSegmentation = new Intent(context, PetSegmentationActivity.class);
        context.startActivity(petSegmentation);
    }

    public static void goToSkySegmentation(Context context) {
        Intent petSegmentation = new Intent(context, SkySegmentationActivity.class);
        context.startActivity(petSegmentation);
    }

    public static void goToBackgroundReplacement(Context context) {
        Intent fullCameraActivity = new Intent(context, BackgroundReplacementActivity.class);
        context.startActivity(fullCameraActivity);
    }
}

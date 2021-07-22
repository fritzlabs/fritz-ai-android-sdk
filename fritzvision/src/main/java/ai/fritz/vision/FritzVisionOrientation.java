package ai.fritz.vision;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.Surface;

import java.util.HashMap;
import java.util.Map;

/**
 * FritzVisionOrientation determines the rotation for the input image the orientation of the camera sensor and the device rotation.
 */
public class FritzVisionOrientation {

    private static final Map<Integer, Integer> ORIENTATIONS = new HashMap<>();

    static {
        ORIENTATIONS.put(Surface.ROTATION_0, 90);
        ORIENTATIONS.put(Surface.ROTATION_90, 0);
        ORIENTATIONS.put(Surface.ROTATION_180, 270);
        ORIENTATIONS.put(Surface.ROTATION_270, 180);
    }

    private static final String TAG = FritzVisionOrientation.class.getSimpleName();

    /**
     * Using the device camera orientation and the device rotation, determine the orientation that should be applied to the image.
     *
     * @param activity
     * @param cameraId
     * @return the rotation angle that should be applied to the image.
     */
    public static ImageOrientation getImageOrientationFromCamera(Activity activity, String cameraId) {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        int deviceRotationFromDisplay = activity.getWindowManager().getDefaultDisplay().getRotation();
        int deviceRotation = ORIENTATIONS.get(deviceRotationFromDisplay);

        try {
            CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);

            return getOrientationByDeviceRotation(deviceRotation, lensFacing);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Unable to access the camera " + cameraId + ":" + e.getMessage());
        }

        return ImageOrientation.UP;
    }

    /**
     * Get the image orientation from the device rotation and camera lens direction.
     * <p>
     * This assumes that there's only a front and back camera on the device.
     *
     * @param deviceRotation - the device rotation (divisible by 90)
     * @param lensFacing     - the direction of the camera lens.
     * @return an orientation to apply to the image.
     */
    public static ImageOrientation getOrientationByDeviceRotation(int deviceRotation, int lensFacing) {
        boolean shouldMirror = lensFacing == CameraCharacteristics.LENS_FACING_FRONT;
        // Landscape (90 degrees counter-clockwise from "upright")
        if (deviceRotation == 0) {
            return ImageOrientation.getOrientationFromName(ImageOrientation.UP.name(), shouldMirror);
        }

        // Portrait Upright (device orientation 0 degrees)
        if (deviceRotation == 90) {
            return ImageOrientation.getOrientationFromName(ImageOrientation.RIGHT.name(), shouldMirror);
        }

        // Landscape (90 degrees clockwise from "upright")
        if (deviceRotation == 180) {
            return ImageOrientation.getOrientationFromName(ImageOrientation.DOWN.name(), shouldMirror);
        }

        // Portrait Upside-down
        if (deviceRotation == 270) {
            return ImageOrientation.getOrientationFromName(ImageOrientation.LEFT.name(), shouldMirror);
        }

        return ImageOrientation.UP;
    }
}

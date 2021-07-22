package ai.fritz.visionCV;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Size;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

import ai.fritz.vision.CameraRotation;
import ai.fritz.visionCV.rigidpose.RigidPoseResult;

public class FritzCVImage {


    private static final int THICKNESS = 5;
    private static final int RADIUS = 5;

    /**
     * Convert a matrix to a FritzCVImage.
     *
     * @return a FritzCVImage object.
     */
    public static FritzCVImage fromBitmap(Bitmap bitmap) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);
        return new FritzCVImage(rgba, 0);
    }

    public static FritzCVImage fromBitmap(Bitmap bitmap, int rotation) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);
        return new FritzCVImage(rgba, rotation);
    }

    public static FritzCVImage fromMatrix(Mat rgba) {
        return new FritzCVImage(rgba, 0);
    }

    public static FritzCVImage fromMatrix(Mat rgba, int rotation) {
        return new FritzCVImage(rgba, rotation);
    }

    public static FritzCVImage fromMediaImage(Image image, int rotation) {
        Mat rgb = convertToRGB(image);
        return new FritzCVImage(rgb, rotation);
    }

    private static Mat convertToRGB(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        Mat mYuv = new Mat(image.getHeight() + image.getHeight() / 2, image.getWidth(), CvType.CV_8UC1);
        mYuv.put(0, 0, nv21);
        Mat mRGB = new Mat();
        Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGB_NV21, 3);
        return mRGB;
    }

    private Mat rgb;
    private int rotation;

    private FritzCVImage(Mat rgb, int rotation) {
        this.rgb = rgb;
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    /**
     * Get the rotated bitmap.
     *
     * @return the rotated bitmap
     */
    public Mat rotate() {
        Mat dst = new Mat();
        switch (rotation) {
            case 90:
                Core.rotate(rgb, dst, Core.ROTATE_90_CLOCKWISE);
                return dst;
            case 180:
                Core.rotate(rgb, dst, Core.ROTATE_180);
                return dst;
            case 270:
                Core.rotate(rgb, dst, Core.ROTATE_90_COUNTERCLOCKWISE);
                return dst;
            default: {
                return rgb;
            }
        }
    }

    /**
     * Get the matrix before any rotation is applied.
     *
     * @return
     */
    public Mat getMatrix() {
        return rgb;
    }

    public Size getRotatedBitmapDimensions() {
        if (rotation == CameraRotation.DEGREES_90.getDegrees() || rotation == CameraRotation.DEGREES_270.getDegrees()) {
            return new Size(rgb.rows(), rgb.cols());
        }

        return new Size(rgb.cols(), rgb.rows());
    }

    public Mat drawPose(Scalar color, RigidPoseResult result) {
        Mat canvas = rotate();
        Point[] keypoints = result.getScaledKeypoints(new Size(canvas.cols(), canvas.rows()));

        int i = 0;
        for (Point point : keypoints) {
            Imgproc.circle(canvas, point, RADIUS, color, THICKNESS);
            Imgproc.putText(
                    canvas,                          // Matrix obj of the image
                    String.valueOf(i),          // Text to be added
                    point,               // point
                    Core.FONT_HERSHEY_PLAIN,      // front face
                    3,                               // front scale
                    new Scalar(0, 0, 0),             // Scalar object for color
                    5                                // Thickness
            );
            i++;
        }

        return canvas;
    }
}

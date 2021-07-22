package ai.fritz.sdktests;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.ImageOrientation;
import ai.fritz.vision.ImageRotation;

public class TestingAssetHelper {

    public static FritzVisionImage getVisionImageForAsset(Context context, TestingAsset asset) {
        return getVisionImageForAsset(context, asset, false);
    }

    public static FritzVisionImage getVisionImageForAsset(Context context, TestingAsset asset, boolean mirror) {
        if (mirror) {
            ImageOrientation orientation = ImageOrientation.UP_MIRRORED;
            return FritzVisionImage.fromBitmap(getBitmapForAsset(context, asset), orientation);
        }
        return FritzVisionImage.fromBitmap(getBitmapForAsset(context, asset));
    }

    public static Bitmap getBitmapForAsset(Context context, TestingAsset asset) {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(asset.getPath());
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bitmap;
    }

    public static String getFilePathForAsset(Context context, TestingAsset asset) {
        AssetManager assetManager = context.getAssets();

        try {
            InputStream initialStream = assetManager.open(asset.getPath());
            byte[] buffer = new byte[initialStream.available()];
            initialStream.read(buffer);

            File targetFile = File.createTempFile("test", null);
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            return targetFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}

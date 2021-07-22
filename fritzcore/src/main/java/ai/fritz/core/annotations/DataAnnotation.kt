package ai.fritz.core.annotations

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class KeypointAnnotation(var id: Int, var label: String, var x: Float, var y: Float, var visibility: Boolean) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject();
        jsonObject.put("id", id)
        jsonObject.put("label", label)
        jsonObject.put("x", x)
        jsonObject.put("y", y)
        jsonObject.put("visibility", visibility)
        return jsonObject;
    }
}

class BoundingBoxAnnotation(var xmin: Float, var ymin: Float, var width: Float, var height: Float) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject();
        jsonObject.put("xmin", xmin)
        jsonObject.put("ymin", ymin)
        jsonObject.put("width", width)
        jsonObject.put("height", height)
        return jsonObject;
    }
}

class SegmentationAnnotation(var mask: Array<IntArray>) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject();
        jsonObject.put("mask", JSONArray(mask))
        return jsonObject;
    }
}

class DataAnnotation(var label: String, var keypoints: List<KeypointAnnotation> = emptyList(), var bbox: BoundingBoxAnnotation? = null, var segmentation: SegmentationAnnotation? = null, var isImageLabel: Boolean = false) {
    fun toJson(): JSONObject {
        val jsonObject = JSONObject();

        var keypointsJson: JSONArray? = null;
        keypointsJson = JSONArray();
        for (keypoint in keypoints) {
            keypointsJson!!.put(keypoint.toJson())
        }

        jsonObject.put("keypoints", keypointsJson)
        jsonObject.put("bbox", bbox?.toJson())
        jsonObject.put("segmentation", segmentation?.toJson())
        jsonObject.put("label", label)
        jsonObject.put("is_image_label", isImageLabel)
        return jsonObject
    }
}

package ai.fritz.core.events

import ai.fritz.core.annotations.Base64EncodableImage
import ai.fritz.core.annotations.DataAnnotation
import ai.fritz.core.FritzOnDeviceModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ModelRecordingEventData(
        val onDeviceModel: FritzOnDeviceModel,
        val encodedImage: Base64EncodableImage,
        val predictedAnnotations: List<DataAnnotation>,
        val userModifiedAnnotations: List<DataAnnotation>?) : EventData {

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("model_uid", onDeviceModel.modelId)
        obj.put("model_version", onDeviceModel.modelVersion)
        obj.put("input", encodedImage.encodedInput())
        obj.put("input_width", encodedImage.encodedSize().width)
        obj.put("input_height", encodedImage.encodedSize().height)

        val predictedAnnotationsJson = JSONArray()
        for (dataAnnotation: DataAnnotation in predictedAnnotations) {
            predictedAnnotationsJson.put(dataAnnotation.toJson())
        }

        obj.put("predicted_annotations", predictedAnnotationsJson)

        userModifiedAnnotations?.let {
            val modifiedAnnotationsJson = JSONArray()
            for (dataAnnotation: DataAnnotation in userModifiedAnnotations) {
                modifiedAnnotationsJson.put(dataAnnotation.toJson())
            }
            obj.put("modified_annotations", modifiedAnnotationsJson)
        } ?: obj.put("modified_annotations", null)


        return obj
    }
}
package ai.fritz.vision.base

import ai.fritz.core.Fritz
import ai.fritz.core.FritzOnDeviceModel
import ai.fritz.core.annotations.AnnotatableResult
import ai.fritz.core.annotations.Base64EncodableImage
import ai.fritz.core.annotations.DataAnnotation
import ai.fritz.core.events.ModelEvent
import ai.fritz.core.factories.ModelEventFactory
import android.util.Log

abstract class FritzVisionRecordablePredictor<T : AnnotatableResult>(onDeviceModel: FritzOnDeviceModel, options: FritzVisionPredictorOptions) : FritzVisionPredictor<T>(onDeviceModel, options) {

    /**
     * Records an image with the predicted and modified annotations
     *
     * @param image [Base64EncodableImage] - the image to record.
     * @param predictedAnnotations [List<SDKAnnotation>] - the list of predicted annotations from the model (default=[])
     * @param userModifiedAnnotations [List<SDKAnnotation>] - the list of modified/edited annotations
     */
    fun record(image: Base64EncodableImage, predictedAnnotations: List<DataAnnotation> = emptyList(), userModifiedAnnotations: List<DataAnnotation>? = null) {
        record(image, predictedAnnotations, userModifiedAnnotations,
                { -> Log.d(FritzVisionRecordablePredictor.TAG, "Successfully recorded the annotation") },
                { -> Log.d(FritzVisionRecordablePredictor.TAG, "Annotation failed to record.") })
    }

    /**
     * Records an image with the predicted and modified annotations
     *
     * @param image [Base64EncodableImage] - the image to record.
     * @param predictedAnnotations [List<SDKAnnotation>] - the list of predicted annotations from the model (default=[])
     * @param userModifiedAnnotations [List<SDKAnnotation>] - the list of modified/edited annotations (default=null)
     * @param onSuccess [Unit] - callback when the image is successfully recorded
     * @param onError [Unit] - callback when the image failed to record
     */
    fun record(image: Base64EncodableImage, predictedAnnotations: List<DataAnnotation>  = emptyList(), userModifiedAnnotations: List<DataAnnotation>? = null, onSuccess: () -> Unit, onFail: () -> Unit) {
        val event: ModelEvent = ModelEventFactory.createModelRecordEvent(onDeviceModel, image, predictedAnnotations, userModifiedAnnotations)
        Fritz.sessionManager.recordAnnotation(event, onSuccess, onFail)
    }

    /**
     * Records an image with the predicted result and modified annotations
     *
     * @param image [Base64EncodableImage] - the image to record.
     * @param predictionResult [AnnotatableResult] - the result from the prediction to record.
     * @param userModifiedAnnotations [List<SDKAnnotation>] - the list of modified/edited annotations (default: null).
     * @param onSuccess [Unit] - callback when the image is successfully recorded (default=null)
     * @param onError [Unit] - callback when the image failed to record
     */
    fun record(image: Base64EncodableImage, predictionResult: T, userModifiedAnnotations: List<DataAnnotation>? = null, onSuccess: () -> Unit, onFail: () -> Unit) {
        record(image, predictionResult.toAnnotations(), userModifiedAnnotations, onSuccess, onFail)
    }

    /**
     * Records an image with the predicted result and modified annotations
     *
     * @param image [Base64EncodableImage] - the image to record.
     * @param predictionResult [AnnotatableResult] - the result from the prediction to record.
     * @param userModifiedAnnotations [List<SDKAnnotation>] - the list of modified/edited annotations (default: null). (default=null)
     */
    fun record(image: Base64EncodableImage, predictionResult: T, userModifiedAnnotations: List<DataAnnotation>? = null) {
        record(image, predictionResult.toAnnotations(), userModifiedAnnotations,
                { -> Log.d(FritzVisionRecordablePredictor.TAG, "Successfully recorded the annotation") },
                { -> Log.d(FritzVisionRecordablePredictor.TAG, "Annotation failed to record.") })
    }

    companion object {
        private val TAG = FritzVisionRecordablePredictor::class.java.simpleName
    }
}
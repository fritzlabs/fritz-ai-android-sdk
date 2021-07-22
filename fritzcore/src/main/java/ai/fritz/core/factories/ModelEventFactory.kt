package ai.fritz.core.factories

import ai.fritz.core.FritzOnDeviceModel
import ai.fritz.core.annotations.Base64EncodableImage
import ai.fritz.core.annotations.DataAnnotation
import ai.fritz.core.constants.ModelEventName
import ai.fritz.core.events.InstallEventData
import ai.fritz.core.events.ModelEvent
import ai.fritz.core.events.ModelRecordingEventData
import ai.fritz.core.events.ModelTimingEventData
import java.util.concurrent.TimeUnit

/**
 * Factory to create ModelEvents with the specified EventData
 * @hide
 */
object ModelEventFactory {
    @JvmStatic
    fun createInstallEvent(onDeviceModel: FritzOnDeviceModel): ModelEvent {
        val timestamp = timestampSeconds
        val data = InstallEventData(onDeviceModel.modelId, onDeviceModel.modelVersion, onDeviceModel.isDownloadedOTA)
        return ModelEvent(ModelEventName.MODEL_INSTALL.eventName, data, timestamp)
    }

    @JvmStatic
    fun createPredictionTiming(onDeviceModel: FritzOnDeviceModel, elapsedNs: Long): ModelEvent {
        val timestamp = timestampSeconds
        val data = ModelTimingEventData(onDeviceModel.modelId, onDeviceModel.modelVersion, elapsedNs)
        return ModelEvent(ModelEventName.PREDICTION_TIMING.eventName, data, timestamp)
    }

    @JvmStatic
    fun createCustomTimingEvent(name: ModelEventName, onDeviceModel: FritzOnDeviceModel, elapsedNs: Long): ModelEvent {
        val timestamp = timestampSeconds
        val data = ModelTimingEventData(onDeviceModel.modelId, onDeviceModel.modelVersion, elapsedNs)
        return ModelEvent(name.eventName, data, timestamp)
    }

    @JvmStatic
    fun createModelRecordEvent(
            onDeviceModel: FritzOnDeviceModel,
            encodableImage: Base64EncodableImage,
            predictedAnnotations: List<DataAnnotation> = emptyList(),
            userModifiedAnnotations: List<DataAnnotation>? = null): ModelEvent {
        val timestamp = timestampSeconds
        val data = ModelRecordingEventData(onDeviceModel, encodableImage, predictedAnnotations, userModifiedAnnotations)
        return ModelEvent(ModelEventName.PREDICT_ANNOTATION.eventName, data, timestamp)
    }

    private val timestampSeconds: Long
        private get() {
            val millis = System.currentTimeMillis()
            return TimeUnit.MILLISECONDS.toSeconds(millis)
        }
}
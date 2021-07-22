package ai.fritz.core.constants

enum class ModelEventName(val eventName: String) {
    PREDICTION_TIMING("prediction"),
    MODEL_INSTALL("model_installed"),
    MODEL_PREPROCESS("model_preprocess"),
    MODEL_POSTPROCESS("model_postprocess"),
    MODEL_DOWNLOAD_COMPLETED("model_download_completed"),
    MODEL_DECRYPTION_COMPLETED("model_decryption_completed"),
    MODEL_DECRYPTION_FAILED("model_decryption_failed"),
    PREDICT_ANNOTATION("prediction_annotation");
}
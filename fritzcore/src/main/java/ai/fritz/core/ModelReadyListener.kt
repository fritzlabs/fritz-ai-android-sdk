package ai.fritz.core

/**
 * The callback for when the latest version of a [FritzManagedModel] is downloaded onto the device
 * and stored as a [FritzOnDeviceModel].
 */
interface ModelReadyListener {
    fun onModelReady(onDeviceModel: FritzOnDeviceModel)
}
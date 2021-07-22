package ai.fritz.listeners

import ai.fritz.core.FritzOnDeviceModel

interface DownloadTaggedModelsListener {
    fun onCompleted(onDeviceModels: List<FritzOnDeviceModel>)
    fun onError()
}
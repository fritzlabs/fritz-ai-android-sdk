package ai.fritz.listeners

import ai.fritz.core.FritzManagedModel

interface SearchModelTagsListener {
    fun onCompleted(modelVersions: MutableList<FritzManagedModel>)
    fun onError()
}
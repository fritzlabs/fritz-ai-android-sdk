package ai.fritz.sdktests.validators

import ai.fritz.vision.FritzVisionLabel
import ai.fritz.vision.imagelabeling.FritzVisionLabelResult
import ai.fritz.vision.objectdetection.FritzVisionObjectResult
import org.junit.Assert.*


class LabelResultValidator(private val labelResult: FritzVisionLabelResult ) {
    fun assertHasAtLeastOneObject() {
        assertTrue(labelResult.visionLabels.size > 0);
    }

    fun assertNumObjects(numObjects: Int) {
        assertEquals(labelResult.visionLabels.size, numObjects);
    }

    fun assertLabelExists(label: String): FritzVisionLabel? {
        var foundLabel: FritzVisionLabel? = null
        for (visionLabel in labelResult.visionLabels) {
            if (visionLabel.text.equals(label, ignoreCase = true)) {
                foundLabel = visionLabel
                break
            }
        }
        assertNotNull(foundLabel)
        return foundLabel;
    }
}
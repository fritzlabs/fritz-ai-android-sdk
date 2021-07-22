package ai.fritz.sdktests.validators

import ai.fritz.vision.objectdetection.FritzVisionObjectResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue


class ObjectResultValidator(private val objectResult: FritzVisionObjectResult) {
    fun assertHasAtLeastOneObject() {
        assertTrue(objectResult.objects.size > 0);
    }

    fun assertNumObjects(numObjects: Int) {
        assertEquals(objectResult.objects.size, numObjects);
    }
}
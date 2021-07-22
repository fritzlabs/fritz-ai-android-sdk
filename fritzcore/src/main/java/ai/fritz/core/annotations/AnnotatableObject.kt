package ai.fritz.core.annotations

import android.util.Size

interface AnnotatableLabel {
    fun toAnnotation(): DataAnnotation
}

interface AnnotatableObject {
    fun toAnnotation(size: Size): DataAnnotation
}

interface AnnotatableResult {
    fun toAnnotations(): List<DataAnnotation>
}
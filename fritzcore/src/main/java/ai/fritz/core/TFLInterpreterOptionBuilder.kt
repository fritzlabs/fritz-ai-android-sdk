package ai.fritz.core

import org.tensorflow.lite.Interpreter

interface TFLInterpreterOptionBuilder {
    fun buildInterpreterOptions(): Interpreter.Options?
}
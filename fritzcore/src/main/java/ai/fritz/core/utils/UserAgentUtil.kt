package ai.fritz.core.utils

import ai.fritz.core.BuildConfig
import android.os.Build
import java.util.*

/**
 * Get the app information from the build configs
 * @hide
 */
object UserAgentUtil {
    private const val UNKNOWN = "Unknown"
    @JvmStatic
    fun create(appName: String?, packageName: String?, versionName: String?, versionCode: Int): String {
        val osNameVersion = getOsNameVersion()
        val deviceModelName = getDeviceModelName()
        val sdkVersion = getSdkVersion()
        return String.format(Locale.ENGLISH,
                "%s/%s (%s; build:%d; platform:android; %s; %s) %s",
                appName ?: UNKNOWN,
                versionName ?: UNKNOWN,
                packageName ?: UNKNOWN,
                versionCode,
                osNameVersion,
                deviceModelName,
                sdkVersion)
    }

    private fun getOsNameVersion(): String {
        val fields = Build.VERSION_CODES::class.java.fields
        return fields[Build.VERSION.SDK_INT].name
    }

    /**
     * From https://stackoverflow.com/questions/1995439/get-android-phone-model-programmatically
     *
     * @return
     */
    private fun getDeviceModelName(): String {
        val manufacturer = Build.MANUFACTURER // for getting manufacturer
        val model = Build.MODEL // for getting Model of the device
        return if (manufacturer == null || model == null) {
            UNKNOWN
        } else "$manufacturer $model"
    }

    @JvmStatic
    fun getSdkVersion(): String {
        val version = BuildConfig.VERSION_NAME
        return String.format("Fritz/%s", version)
    }
}
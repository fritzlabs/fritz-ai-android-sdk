# Fritz Android SDK

An open-source version of the Fritz AI SDK.

This SDK functions identically to the closed-source Fritz SDK with the exception of features that make calls to the Fritz AI backend service. This version of the SDK does not support OTA model downloads, data collection, model encryption, or telemetry data collection.

Minimal code changes are required. Most importantly, you will need to update `build.gradle` files
to reflect the new location of the binaries.

```
// ADD FOR FRITZ DEPENDENCIES
maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/fritzlabs/fritz-ai-android-sdk")
}
```

This version of the Fritz SDK corresponded to version 7.0.0. Please update all dependencies to use this version.

```
dependencies {
    implementation "ai.fritz:core:7.0.0"
    implementation "ai.fritz:vision:7.0.0"
    ...
}
```

Functions that previous made calls to the Fritz AI backend service now simple return null.

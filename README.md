# Fritz Android SDK

An open-source version of the Fritz AI SDK.

This SDK functions identically to the closed-source Fritz SDK with the exception of features that make calls to the Fritz AI backend service. This version of the SDK does not support OTA model downloads, data collection, model encryption, or telemetry data collection.

Minimal code changes are required. Most importantly, you will need to update `build.gradle` files
to reflect the new location of the binaries.

## Updating your gradle dependencies

The GitHub package repository requires the user of a package to have a valid GitHub account. This means you must provide a user name and a personal access token (PAT) to install the dependency. 

**IMPORTANT:** When creating this token, you must select `read:packages` permissions. 

You may store them below in the gradle file (not recommended for security reasons) or as environmental variables.

Alternative, you can [download the packages](https://github.com/orgs/fritzlabs/packages) and host them yourself if needed.

```
ext {

    // To pull these values from the environment
    // GITHUB_TOKEN = System.getenv("GITHUB_TOKEN")
    GITHUB_TOKEN = "YOUR GITHUB PAT TOKEN"
    GITHUB_USER = "YOUR GITHUB USERNAME"
}

maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/fritzlabs/fritz-ai-android-sdk")
    credentials {
        username GITHUB_USER
        password GITHUB_TOKEN
    }
}
```

This version of the Fritz SDK corresponded to version 7.0.0 for `core` and `vision` modules and version 4.0.0 for individual models 

```
dependencies {
    implementation "ai.fritz:core:7.0.0"
    implementation "ai.fritz:vision:7.0.0"
    implementation "ai.fritz:vision-object-detection-model-fast:4.0.0"
    ...
}
```

Functions that previously made calls to the Fritz AI backend service now simply return null.

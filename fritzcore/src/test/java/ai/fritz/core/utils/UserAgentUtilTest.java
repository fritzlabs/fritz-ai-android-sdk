package ai.fritz.core.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import ai.fritz.core.BuildConfig;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class UserAgentUtilTest {

    @Test
    public void getSdkVersionTest() {
        // TODO:
        // Not a great test since I'll have to change with each version bump
        // but I'm not 100% sure how to read the gradle.properties file on
        // the project level.
        String sdkVersion = UserAgentUtil.getSdkVersion();
        assertEquals(sdkVersion, "Fritz/" + BuildConfig.VERSION_NAME);
    }
}

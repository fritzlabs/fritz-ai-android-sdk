package ai.fritz.core.testutils;

import java.util.UUID;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.api.Session;
import ai.fritz.core.utils.UserAgentUtil;

public class TestDataFactory {

    public static Session createSession() {
        String userAgent = UserAgentUtil.create(
                "Fritz Test App",
                "com.fritztest.app",
                "1.0.0", 1);
        return new Session(UUID.randomUUID().toString(), UUID.randomUUID().toString(), userAgent);
    }

    public static FritzOnDeviceModel createCustomModel() {
        return createCustomModel("mnist.tflite", UUID.randomUUID().toString(), 1);
    }

    public static FritzOnDeviceModel createCustomModel(String modelPath, String modelId, int modelVersion) {
        return new FritzOnDeviceModel(modelPath, modelId, modelVersion);
    }
}

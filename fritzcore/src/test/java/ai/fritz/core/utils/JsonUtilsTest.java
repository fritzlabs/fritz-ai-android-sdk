package ai.fritz.core.utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class JsonUtilsTest {

    @Test
    public void testConvertJsonArrayToList() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("tag-1");
        jsonArray.put("tag-2");

        try {
            List<String> items = JsonUtils.convertJsonArrayToList(jsonArray);
            assertArrayEquals(new String[]{"tag-1", "tag-2"}, items.toArray(new String[2]));

        } catch (JSONException e) {
            fail();
        }
    }

    @Test
    public void testToMap() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key1", "value1");
            jsonObject.put("key2", "value2");

            Map<String, String> map = JsonUtils.toMap(jsonObject);
            assertEquals("value1", map.get("key1"));
            assertEquals("value2", map.get("key2"));
        } catch (JSONException e) {
            fail();
        }
    }
}

package ai.fritz.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.robolectric.RuntimeEnvironment;

import java.util.Set;
import java.util.UUID;

import ai.fritz.core.api.ApiClient;
import ai.fritz.core.api.Session;
import ai.fritz.core.api.SessionSettings;
import ai.fritz.core.constants.SPKeys;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class BaseUnitTest {
    public static final String APP_LABEL = "Test Fritz App";
    public static final String PACKAGE_NAME = "ai.fritz.test";
    protected static final String TEST_API_KEY = UUID.randomUUID().toString();

    protected Context context;
    protected SharedPreferences sharedPrefs;
    protected SharedPreferences.Editor editor;
    protected PackageInfo packageInfo;
    protected ApplicationInfo applicationInfo;

    @Before
    public void setup() {
        sharedPrefs = mock(SharedPreferences.class);
        editor = mock(SharedPreferences.Editor.class);
        context = setupTestContext();
        setupPackageManager();
    }

    @After
    public void tearDown() {

    }

    protected Session configureFritz(String apiKey) {
        Session session = Fritz.intializeSession(context, apiKey);
        ApiClient apiClient = mock(ApiClient.class);
        SessionManager sessionContext = new SessionManager(context.getApplicationContext(), session, apiClient);
        Fritz.configure(sessionContext);
        return session;
    }

    /**
     * Helper method to setup a mocked context from robolectric
     *
     * @return Context
     */
    protected Context setupTestContext() {
        Context context = spy(RuntimeEnvironment.application);
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs);
        when(sharedPrefs.edit()).thenReturn(editor);
        setupSession(getTestSettings());

        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putFloat(anyString(), anyFloat())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putStringSet(anyString(), any(Set.class))).thenReturn(editor);

        when(context.getPackageName()).thenReturn(PACKAGE_NAME);

        // for fetching resources (fake address)
        doReturn("https://api-uniitests.fritz.ai").when(context).getString(anyInt());

        return context;
    }

    private Session getTestSettings() {
        Session session = new Session(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "whatever");
        session.setSettings(SessionSettings.createDefault());
        return session;
    }

    protected void setupSession(Session session) {
        try {
            doReturn(session.toJson().toString()).when(sharedPrefs).getString(SPKeys.FRITZ_SESSION, null);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setupPackageManager() {
        PackageManager mockPackageManager = mock(PackageManager.class);
        applicationInfo = mock(ApplicationInfo.class);
        applicationInfo.metaData = new Bundle();
        packageInfo = new PackageInfo();
        packageInfo.versionName = "LOLLIPOP";
        packageInfo.versionCode = 16;
        packageInfo.packageName = PACKAGE_NAME;

        try {
            when(mockPackageManager.getApplicationInfo(eq(PACKAGE_NAME), eq(PackageManager.GET_META_DATA))).thenReturn(applicationInfo);
            when(mockPackageManager.getPackageInfo(eq(PACKAGE_NAME), eq(0))).thenReturn(packageInfo);
            when(mockPackageManager.getApplicationLabel(any(ApplicationInfo.class))).thenReturn(APP_LABEL);

        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("How did we get here?!");
        }
        when(context.getPackageManager()).thenReturn(mockPackageManager);
    }
}

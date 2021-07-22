package ai.fritz.sdktests;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ai.fritz.core.Fritz;
import ai.fritz.core.api.DownloadModelTask;
import ai.fritz.core.utils.PreferenceManager;
import ai.fritz.sdktests.BaseFritzTest;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

public class DownloadModelTests extends BaseFritzTest {
    private static final String DUMMY_URL = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
    private static final String DUMMY_NAME = "dumdum";
    private static final int READ_BYTE_BUFFER_LENGTH = 8192;
    private static final int TIMEOUT_SECONDS = 10;
    private static final int MODEL_VERSION = 1;
    private static File file;

    @Before
    public void setup() {
        super.setup();

        // Clear the key for this model and initialize
        PreferenceManager.clearAll(appContext);
        Fritz.configure(appContext);
        file = new File(appContext.getFilesDir(), createDummyFileName());
    }

    @Test
    public void testDownloadExists() {
        // Download the whole file
        downloadFile(2);

        final CountDownLatch latch = new CountDownLatch(1);
        DownloadModelTask.PostExecuteListener listener = new DownloadModelTask.PostExecuteListener() {
            @Override
            public void onSuccess(String absolutePath) {
                fail("Download should not resume.");
            }

            @Override
            public void onFailure() {
                // File already completely written and should not be downloaded again
                latch.countDown();
            }
        };

        DownloadModelTask task = new DownloadModelTask(DUMMY_NAME, MODEL_VERSION, appContext.getFilesDir(), listener);
        task.execute(DUMMY_URL);

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted.");
        }
    }

    @Ignore("Skipping test that involve the Fritz AI backend.")
    @Test
    public void testResumeDownload() {
        // Download half the file
        downloadFile(1);
        final long startingLength = file.length();
        
        // The file already has contents
        assertTrue(startingLength > 0);

        final CountDownLatch latch = new CountDownLatch(1);
        DownloadModelTask.PostExecuteListener listener = new DownloadModelTask.PostExecuteListener() {
            @Override
            public void onSuccess(String absolutePath) {
                latch.countDown();
            }

            @Override
            public void onFailure() {
                fail("Download not resumed.");
            }
        };

        DownloadModelTask task = new DownloadModelTask(DUMMY_NAME, MODEL_VERSION, appContext.getFilesDir(), listener);
        task.execute(DUMMY_URL);

        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted.");
        }

        final long endingLength = file.length();
        
        // The file has more written to it
        assertTrue(endingLength > startingLength);
    }

    /**
     * Downloads a specified amount of the target file.
     * Test file has a total size of ~13kB and finishes being read in 2 segments.
     * Reading 1 segment results in half a file of ~7.5kB.
     *
     * @param portion amount of segments to read
     */
    private void downloadFile(int portion) {
        if (portion > 2 || portion < 0) {
            fail("Invalid bounds.");
        }
        
        try {
            URL wallpaperURL = new URL(DUMMY_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) wallpaperURL.openConnection();
            FileOutputStream outputStream = new FileOutputStream(file);

            urlConnection.connect();
            InputStream bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream(), READ_BYTE_BUFFER_LENGTH);
            byte[] buffer = new byte[READ_BYTE_BUFFER_LENGTH];

            // MAX 2 SEGMENTS TO READ
            for (int i = 0; i < portion; i++) {
                int dataSize = bufferedInputStream.read(buffer);
                outputStream.write(buffer, 0, dataSize);
            }
            
            bufferedInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createDummyFileName() {
        return DUMMY_NAME + "_v" + MODEL_VERSION + ".tflite";
    }
}

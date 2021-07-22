package ai.fritz.core.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import ai.fritz.core.BaseUnitTest;
import ai.fritz.core.FritzCustomModelService;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.testutils.TestDataFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, packageName = "ai.fritz.sdkapp")
public class JobUtilTest extends BaseUnitTest {

    @Test
    public void testJobScheduledSuccessful() {
        JobScheduler jobScheduler = mock(JobScheduler.class);
        // don't return any jobs
        when(jobScheduler.getAllPendingJobs()).thenReturn(new ArrayList<JobInfo>());
        when(jobScheduler.schedule(any(JobInfo.class))).thenReturn(JobScheduler.RESULT_SUCCESS);

        when(context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).thenReturn(jobScheduler);
        int status = JobUtil.checkForModelUpdate(context, TestDataFactory.createCustomModel(), false);
        verify(jobScheduler, times(1)).schedule(any(JobInfo.class));
        assertEquals(JobScheduler.RESULT_SUCCESS, status);
    }

    @Test
    public void testJobAlreadyRunning() {
        JobScheduler jobScheduler = mock(JobScheduler.class);
        FritzOnDeviceModel onDeviceModel = TestDataFactory.createCustomModel();

        // Return one job already running
        List<JobInfo> jobInfoList = new ArrayList<>();
        jobInfoList.add(buildJobInfo(context, onDeviceModel));
        when(jobScheduler.getAllPendingJobs()).thenReturn(jobInfoList);
        when(jobScheduler.schedule(any(JobInfo.class))).thenReturn(JobScheduler.RESULT_SUCCESS);
        when(context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).thenReturn(jobScheduler);

        // Check model update
        int status = JobUtil.checkForModelUpdate(context, onDeviceModel, false);

        // Check that the job is not scheduled
        verify(jobScheduler, times(0)).schedule(any(JobInfo.class));
        assertEquals(JobScheduler.RESULT_FAILURE, status);
    }

    @Test
    public void testWifiNotSet() {
        JobScheduler jobScheduler = mock(JobScheduler.class);
        FritzOnDeviceModel onDeviceModel = TestDataFactory.createCustomModel();

        // don't return any jobs
        when(jobScheduler.getAllPendingJobs()).thenReturn(new ArrayList<JobInfo>());
        when(jobScheduler.schedule(any(JobInfo.class))).thenReturn(JobScheduler.RESULT_SUCCESS);
        when(context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).thenReturn(jobScheduler);

        // Use any network when use wifi is false
        JobUtil.checkForModelUpdate(context, onDeviceModel, false);
        ArgumentCaptor<JobInfo> argument = ArgumentCaptor.forClass(JobInfo.class);
        verify(jobScheduler).schedule(argument.capture());
        int networkType = argument.getValue().getNetworkType();
        assertEquals(networkType, JobInfo.NETWORK_TYPE_ANY);
    }

    @Test
    public void testWifiSet() {
        JobScheduler jobScheduler = mock(JobScheduler.class);
        FritzOnDeviceModel onDeviceModel = TestDataFactory.createCustomModel();

        // don't return any jobs
        when(jobScheduler.getAllPendingJobs()).thenReturn(new ArrayList<JobInfo>());
        when(jobScheduler.schedule(any(JobInfo.class))).thenReturn(JobScheduler.RESULT_SUCCESS);
        when(context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).thenReturn(jobScheduler);

        // Use unmetered networks when wifi is true
        JobUtil.checkForModelUpdate(context, onDeviceModel, true);
        ArgumentCaptor<JobInfo> argumentWithWifi = ArgumentCaptor.forClass(JobInfo.class);
        verify(jobScheduler).schedule(argumentWithWifi.capture());
        int networkTypeWithWifi = argumentWithWifi.getValue().getNetworkType();
        assertEquals(networkTypeWithWifi, JobInfo.NETWORK_TYPE_UNMETERED);
    }


    private JobInfo buildJobInfo(Context appContext, FritzOnDeviceModel onDeviceModel) {
        return new JobInfo.Builder(onDeviceModel.getModelId().hashCode(), new ComponentName(appContext, FritzCustomModelService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setBackoffCriteria(1000, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .setOverrideDeadline(1000)
                .build();
    }
}

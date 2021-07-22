package ai.fritz.aistudio;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ai.fritz.core.Fritz;
import ai.fritz.aistudio.adapters.DemoAdapter;
import ai.fritz.aistudio.adapters.DemoItem;
import ai.fritz.aistudio.ui.SeparatorDecoration;
import ai.fritz.aistudio.utils.Navigation;
import ai.fritz.vision.FritzVision;
import io.fabric.sdk.android.Fabric;

/**
 * The primary activity that shows the different model demos.
 */
public class MainActivity extends AppCompatActivity {
    private static final String FRITZ_URL = "https://fritz.ai";
    private static final String FRITZ_REGISTER_URL = "https://www.fritz.ai/pricing/";
    private static final String TAG = MainActivity.class.getSimpleName();
    private Logger logger = Logger.getLogger(this.getClass().getName());

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.demo_title);
        recyclerView = findViewById(R.id.demo_list_view);

        // Initialize Fritz
        Fritz.configure(this);
        FritzVision.preload();

        // Only enable crash reporting on release.
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        // Setup the recycler view
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager rvLinearLayoutMgr = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rvLinearLayoutMgr);

        // Add a divider
        SeparatorDecoration decoration = new SeparatorDecoration(this, Color.GRAY, 1);
        recyclerView.addItemDecoration(decoration);

        // Add the adapter
        DemoAdapter adapter = new DemoAdapter(getDemoItems());
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);
    }

    private List<DemoItem> getDemoItems() {
        // Add different demo items here
        List<DemoItem> demoItems = new ArrayList<>();

        demoItems.add(new DemoItem(
                getString(R.string.image_labeling_title),
                getString(R.string.image_labeling_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.goToLabelingActivity(v.getContext());
                    }
                }));
        demoItems.add(new DemoItem(
                getString(R.string.fritz_object_detection_title),
                getString(R.string.fritz_object_detection_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.goToObjectDetection(v.getContext());
                    }
                }));
        demoItems.add(new DemoItem(
                getString(R.string.fritz_pose_estimation_title),
                getString(R.string.fritz_pose_estimation_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.goToPoseEstimation(v.getContext());
                    }
                }));
        // This layout requires GPUImageView. Only show it if OpenGL ES 2.0 is supported.
        // Some emulators don't support OpenGL.
        if(supportsOpenGLES2()) {
            demoItems.add(new DemoItem(
                    getString(R.string.fritz_hair_color_title),
                    getString(R.string.fritz_hair_color_description),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Navigation.goToHairSegmentation(v.getContext());
                        }
                    }));
        }
        demoItems.add(new DemoItem(
                getString(R.string.fritz_people_segmentation_title),
                getString(R.string.fritz_people_segmentation_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.goToPeopleSegmentation(v.getContext());
                    }
                }));
        demoItems.add(new DemoItem(
                getString(R.string.fritz_sky_segmentation_title),
                getString(R.string.fritz_sky_segmentation_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.goToSkySegmentation(v.getContext());
                    }
                }));
        demoItems.add(new DemoItem(
                getString(R.string.fritz_pet_segmentation_title),
                getString(R.string.fritz_pet_segmentation_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.goToPetSegmentation(v.getContext());
                    }
                }));
        demoItems.add(new DemoItem(
                getString(R.string.fritz_vision_style_transfer),
                getString(R.string.fritz_vision_style_transfer_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Navigation.goToStyleTransfer(v.getContext());
                    }
                }));
        demoItems.add(new DemoItem(
                getString(R.string.fritz_info_title),
                getString(R.string.fritz_info_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(FRITZ_URL));
                        startActivity(i);
                    }
                }));
        demoItems.add(new DemoItem(
                getString(R.string.fritz_develop_title),
                getString(R.string.fritz_develop_description),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(FRITZ_REGISTER_URL));
                        startActivity(i);
                    }
                }));
        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= 24) {
                demoItems.add(new DemoItem(
                        getString(R.string.fritz_background_replacement_title),
                        getString(R.string.fritz_background_replacement_description),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Navigation.goToBackgroundReplacement(v.getContext());
                            }
                        }));
            }
            demoItems.add(new DemoItem(
                    getString(R.string.fritz_customtflite_title),
                    getString(R.string.fritz_customtflite_description),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Navigation.goToTFLite(v.getContext());
                        }
                    }));
        }
        return demoItems;
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * Code from GPUImage:
     * https://github.com/cats-oss/android-gpuimage/blob/c62bbc10dd36ec300368a71eae098ecd3f792f13/library/src/main/java/jp/co/cyberagent/android/gpuimage/GPUImage.java#L93
     *
     * @return true, if successful
     */
    private boolean supportsOpenGLES2() {
        final ActivityManager activityManager = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }
}

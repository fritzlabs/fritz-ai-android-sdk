package ai.fritz.aistudio.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import ai.fritz.aistudio.R;


public class OptionMenu extends CoordinatorLayout {
    private final float DEFAULT_PROGRESS_INCREMENT = .1f;

    private LayoutInflater inflater;
    private ViewGroup source;
    private BottomSheetBehavior sheetBehavior;

    public OptionMenu(Context context) {
        super(context);
        initSheet(context);
    }

    public OptionMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSheet(context);
    }

    public OptionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSheet(context);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        final int totalChild = source.getChildCount();
        for (int i = 0; i < totalChild; i++) {
            if (source.getChildAt(i).getVisibility() != View.VISIBLE) {
                setVisibility(View.INVISIBLE);
                return;
            }
        }
        setVisibility(View.VISIBLE);
    }

    /**
     * Initializes the behavior of the menu.
     * The menu only expands/collapses when clicking its top for better experience when interacting.
     *
     * @param context the environment the view is in.
     */
    private void initSheet(Context context) {
        inflate(context, R.layout.option_body, this);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.source = findViewById(R.id.bottom_sheet_layout);
        this.sheetBehavior = BottomSheetBehavior.from(source);

        final ImageButton chevron = findViewById(R.id.show_option_button);
        final RelativeLayout menuBar = findViewById(R.id.option_chin);
        menuBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    chevron.setImageResource(R.drawable.ic_chevron_down);
                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    chevron.setImageResource(R.drawable.ic_chevron_up);
                }
            }
        });

        // Prevent menu from being dragged
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            private int previousState = sheetBehavior.getState();

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    if (previousState == BottomSheetBehavior.STATE_EXPANDED) {
                        sheetBehavior.setState(previousState);
                        chevron.setImageResource(R.drawable.ic_chevron_down);
                    }

                    if (previousState == BottomSheetBehavior.STATE_COLLAPSED) {
                        sheetBehavior.setState(previousState);
                        chevron.setImageResource(R.drawable.ic_chevron_up);
                    }
                }

                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED) {
                    previousState = newState;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
        });
    }

    /**
     * Adds a header to the menu.
     * If the header to add is also a child of the parent, removes the view from the parent
     * and adds it to the menu. Must be run on the same thread as the parent.
     *
     * @param view the view to be added to the menu.
     * @return this menu for chaining.
     */
    public OptionMenu withHeader(final View view) {
        ViewGroup parent = (ViewGroup) view.getParent();

        if (parent != null) {
            parent.removeView(view);
        }

        if (view.getMeasuredHeight() == 0) {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        sheetBehavior.setPeekHeight(sheetBehavior.getPeekHeight() + view.getMeasuredHeight());
        source.addView(view);

        return this;
    }

    /**
     * Adds a slider to the menu.
     * Conversions are done to enable float values.
     *
     * @param name       the name of the slider.
     * @param upperBound the maximum value of the slider.
     * @param base       the starting value of the slider.
     * @param toRun      the function to be ran upon moving the slider.
     * @param increment  the steps for the slider.
     * @return this menu for chaining.
     */
    public OptionMenu withSlider(String name, int upperBound, float base, final Function<Float, Void> toRun, float increment) {
        View sliderDrawer = inflater.inflate(R.layout.option_slider, source, false);
        TextView title = sliderDrawer.findViewById(R.id.slider_title);
        SeekBar slider = sliderDrawer.findViewById(R.id.slider);
        final TextView digit = sliderDrawer.findViewById(R.id.slider_progress);

        // Used to determine the max value of the progress view.
        final float sliderRatio = 1 / increment;
        title.setText(name);
        slider.setMax((int) (upperBound * sliderRatio));
        slider.setProgress((int) (base * sliderRatio));
        slider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser) {
                        String display = Float.toString(progress / sliderRatio);
                        digit.setText(display);
                        toRun.apply(progress / sliderRatio);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        String display = Float.toString(slider.getProgress() / sliderRatio);
        digit.setText(display);
        source.addView(sliderDrawer);

        return this;
    }

    /**
     * Adds a slider to the menu.
     * Conversions are done to enable float values. Default step for the slider is .1f.
     *
     * @param name       the name of the slider.
     * @param upperBound the maximum value of the slider.
     * @param base       the starting value of the slider.
     * @param toRun      the function to be ran upon moving the slider.
     * @return this menu for chaining.
     */
    public OptionMenu withSlider(String name, int upperBound, float base, final Function<Float, Void> toRun) {
        return withSlider(name, upperBound, base, toRun, DEFAULT_PROGRESS_INCREMENT);
    }
}

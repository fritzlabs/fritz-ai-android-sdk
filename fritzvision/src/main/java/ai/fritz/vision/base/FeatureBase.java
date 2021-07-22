package ai.fritz.vision.base;

import ai.fritz.core.FritzManagedModel;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.PredictorStatusListener;

/**
 * @hide
 * @param <Predictor>
 * @param <Options>
 */
public abstract class FeatureBase<Predictor extends FritzVisionPredictor, Options extends FritzVisionPredictorOptions, ManagedModel extends FritzManagedModel, OnDeviceModel extends  FritzOnDeviceModel> {

    public Predictor getPredictor(OnDeviceModel onDeviceModel) {
        return getPredictor(onDeviceModel, getDefaultOptions());
    }

    public void loadPredictor(final ManagedModel managedModel, final PredictorStatusListener statusListener) {
        loadPredictor(managedModel, getDefaultOptions(), statusListener, false);
    }

    public void loadPredictor(final ManagedModel managedModel, final PredictorStatusListener statusListener, boolean useWifi) {
        loadPredictor(managedModel, getDefaultOptions(), statusListener, useWifi);
    }

    public void loadPredictor(final ManagedModel managedModel, final Options options, final PredictorStatusListener statusListener) {
        loadPredictor(managedModel, options, statusListener, false);
    }

    public abstract Predictor getPredictor(OnDeviceModel onDeviceModel, Options options);

    public abstract void loadPredictor(final ManagedModel managedModel, final Options options, final PredictorStatusListener statusListener, boolean useWifi);

    protected abstract Options getDefaultOptions();

}

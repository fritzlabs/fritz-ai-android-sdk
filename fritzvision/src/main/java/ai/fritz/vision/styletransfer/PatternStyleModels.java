package ai.fritz.vision.styletransfer;

import org.json.JSONObject;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.utils.JsonUtils;

public class PatternStyleModels {

    private JSONObject patternsConfig;

    public PatternStyleModels(String patternsConfigPath) {
        this.patternsConfig = JsonUtils.buildFromJsonFile(patternsConfigPath);
    }

    public FritzOnDeviceModel getBlueArrow() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "BLUE_ARROW_MODEL")
        );
    }

    public FritzOnDeviceModel getChristmasLights() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "CHRISTMAS_LIGHTS_MODEL")
        );
    }

    public FritzOnDeviceModel getComic() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "COMIC_MODEL")
        );
    }

    public FritzOnDeviceModel getFilament() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "FILAMENT_MODEL")
        );
    }

    public FritzOnDeviceModel getLampPost() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "LAMP_POST_MODEL")
        );
    }

    public FritzOnDeviceModel getMosaic() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "MOSAIC_MODEL")
        );
    }

    public FritzOnDeviceModel getNotreDame() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "NOTRE_DAME_MODEL")
        );
    }

    public FritzOnDeviceModel getShades() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "SHADES_MODEL")
        );
    }

    public FritzOnDeviceModel getSketch() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "SKETCH_MODEL")
        );
    }

    public FritzOnDeviceModel getSnowflake() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "SNOWFLAKE_MODEL")
        );
    }

    public FritzOnDeviceModel getSprinkles() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "SPRINKLES_MODEL")
        );
    }

    public FritzOnDeviceModel getSwirl() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "SWIRL_MODEL")
        );
    }

    public FritzOnDeviceModel getTile() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "TILE_MODEL")
        );
    }

    public FritzOnDeviceModel getVector() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(patternsConfig, "VECTOR_MODEL")
        );
    }

    public FritzOnDeviceModel[] getAll() {
        return new FritzOnDeviceModel[]{
                getBlueArrow(),
                getChristmasLights(),
                getComic(),
                getFilament(),
                getLampPost(),
                getMosaic(),
                getNotreDame(),
                getShades(),
                getSketch(),
                getSnowflake(),
                getSprinkles(),
                getSwirl(),
                getTile(),
                getVector()
        };
    }
}

package ai.fritz.vision.styletransfer;

import org.json.JSONObject;

import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.utils.JsonUtils;

public class PaintingStyleModels {

    private JSONObject paintingModelsConfig;

    public PaintingStyleModels(String paintingModelsPath) {
        this.paintingModelsConfig = JsonUtils.buildFromJsonFile(paintingModelsPath);
    }

    public FritzOnDeviceModel getBicentennialPrint() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "BICENTENNIAL_PRINT_MODEL")
        );
    }

    public FritzOnDeviceModel getFemmes() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "FEMMES_MODEL")
        );
    }

    public FritzOnDeviceModel getHeadOfClown() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "HEAD_OF_CLOWN_MODEL")
        );
    }

    public FritzOnDeviceModel getHorsesOnSeashore() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "HORSES_ON_SEASHORE_MODEL")
        );
    }

    public FritzOnDeviceModel getKaleidoscope() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "KALEIDOSCOPE_MODEL")
        );
    }

    public FritzOnDeviceModel getPinkBlueRhombus() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "PINK_BLUE_RHOMBUS_MODEL")
        );
    }

    public FritzOnDeviceModel getPoppyField() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "POPPY_FIELD_MODEL")
        );
    }

    public FritzOnDeviceModel getRitmoPlastico() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "RITMO_PLASTICO_MODEL")
        );
    }

    public FritzOnDeviceModel getStarryNight() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "STARRY_NIGHT_MODEL")
        );
    }

    public FritzOnDeviceModel getTheScream() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "THE_SCREAM_MODEL")
        );
    }

    public FritzOnDeviceModel getTheTrial() {
        return FritzOnDeviceModel.buildFromModelConfig(
                JsonUtils.getJSONObject(paintingModelsConfig, "THE_TRAIL_MODEL")
        );
    }

    public FritzOnDeviceModel[] getAll() {
        return new FritzOnDeviceModel[]{
                getBicentennialPrint(),
                getFemmes(),
                getHeadOfClown(),
                getHorsesOnSeashore(),
                getKaleidoscope(),
                getPinkBlueRhombus(),
                getPoppyField(),
                getRitmoPlastico(),
                getStarryNight(),
                getTheScream(),
                getTheTrial()
        };
    }

}

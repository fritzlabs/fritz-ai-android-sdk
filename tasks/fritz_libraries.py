import pathlib

GRADLE_PROPERTIES_FILE = "gradle.properties"


class LibraryReplacement(object):
    DEVELOP_TO_RELEASE = "develop_to_release"
    RELEASE_TO_DEVELOP = "release_to_develop"


class FritzLibraryNames(object):
    CORE = "fritzcore"
    VISION = "fritzvision"
    LABEL_MODEL_FAST = "fritzvisionlabelmodelfast"
    LABEL_MODEL_SMALL = "fritzvisionlabelmodelsmall"
    LABEL_MODEL_ACCURATE = "fritzvisionlabelmodelaccurate"

    OBJECT_MODEL_FAST = "fritzvisionobjectdetectionmodelfast"
    STYLE_PAINTING_MODELS = "fritzvisionstylepaintingmodels"
    PEOPLE_SEG_MODEL_FAST = "fritzvisionpeoplesegmentationmodelfast"
    PEOPLE_SEG_MODEL_SMALL = "fritzvisionpeoplesegmentationmodelsmall"
    PEOPLE_SEG_MODEL_ACCURATE = "fritzvisionpeoplesegmentationmodelaccurate"
    LIVING_ROOM_SEG_MODEL_FAST = "fritzvisionlivingroomsegmentationmodelfast"
    LIVING_ROOM_SEG_MODEL_SMALL = "fritzvisionlivingroomsegmentationmodelsmall"
    OUTDOOR_SEG_MODEL_FAST = "fritzvisionoutdoorsegmentationmodelfast"
    OUTDOOR_SEG_MODEL_SMALL = "fritzvisionoutdoorsegmentationmodelsmall"
    OUTDOOR_SEG_MODEL_ACCURATE = "fritzvisionoutdoorsegmentationmodelaccurate"
    HAIR_SEG_MODEL_FAST = "fritzvisionhairsegmentationmodelfast"
    HAIR_SEG_MODEL_SMALL = "fritzvisionhairsegmentationmodelsmall"
    HAIR_SEG_MODEL_ACCURATE = "fritzvisionhairsegmentationmodelaccurate"
    PET_SEG_MODEL_FAST = "fritzvisionpetsegmentationmodelfast"
    PET_SEG_MODEL_SMALL = "fritzvisionpetsegmentationmodelsmall"
    PET_SEG_MODEL_ACCURATE = "fritzvisionpetsegmentationmodelaccurate"
    SKY_SEG_MODEL_FAST = "fritzvisionskysegmentationmodelfast"
    SKY_SEG_MODEL_SMALL = "fritzvisionskysegmentationmodelsmall"
    SKY_SEG_MODEL_ACCURATE = "fritzvisionskysegmentationmodelaccurate"

    POSE_MODEL_SMALL = "fritzvisionposeestimationmodelsmall"
    POSE_MODEL_FAST = "fritzvisionposeestimationmodelfast"
    POSE_MODEL_ACCURATE = "fritzvisionposeestimationmodelaccurate"

    VISION_CV = "fritzvisionCV"
    OPEN_CV = "openCVLibrary341"
    TENSORFLOW_LITE = "tensorflowlite"
    TENSORFLOW_LITE_GPU = "tensorflowlitegpu"


class FritzLibrary(object):
    def __init__(self, module_name: str, distributed_name: str, version_key: str):
        self._module_name = module_name
        self._distributed_name = distributed_name
        self._version_key = version_key

    @property
    def distributed_name(self):
        return self._distributed_name

    @property
    def module_name(self):
        return self._module_name

    @property
    def version(self):
        return f"${{{self._version_key}}}"

    @property
    def version_key(self):
        return self._version_key

    @property
    def local_gradle_project_name(self):
        return f'project(\':{self.module_name}\')'

    @property
    def distributed_gradle_project_name(self):
        return f"\"ai.fritz:{self.distributed_name}:{self.version}\""

    @property
    def distributed_gradle_project_name_only(self):
        return f"ai.fritz:{self.distributed_name}:"

    @property
    def gradle_file_path(self) -> pathlib.Path:
        return pathlib.Path(f"{self.module_name}/build.gradle")

    @property
    def release_build_output_path(self) -> pathlib.Path:
        return pathlib.Path(f"{self.module_name}/build/outputs/aar/{self.release_aar_filename}")

    @property
    def release_aar_filename(self) -> pathlib.Path:
        return f"{self.module_name}-release.aar"

    def get_current_version(self) -> str:
        path = pathlib.Path(GRADLE_PROPERTIES_FILE)
        lines = path.read_text().split("\n")
        for line in lines:
            result = line.split("=")
            if len(result) != 2:
                continue
            key, value = result
            if key == self.version_key:
                return value

        return None

    def change_version(self, new_version):
        """Change the sdk_version in gradle.properties

        Args:
            ctx
            new_version: the sdk version to change it to
        """
        path = pathlib.Path(GRADLE_PROPERTIES_FILE)
        lines = path.read_text().split("\n")
        updated_content = ""
        for line in lines:
            result = line.split("=")
            if len(result) != 2:
                updated_content += f"{line}\n"
                continue
            key = result[0]
            if key == self.version_key:
                updated_content += f"{key}={new_version}\n"
                continue

            updated_content += f"{line}\n"

        with open(GRADLE_PROPERTIES_FILE, "w") as f:
            f.write(updated_content)


core_library = FritzLibrary(
    FritzLibraryNames.CORE,
    "core",
    "sdk_version"
)

vision_library = FritzLibrary(
    FritzLibraryNames.VISION,
    "vision",
    "sdk_version"
)

label_model_fast = FritzLibrary(
    FritzLibraryNames.LABEL_MODEL_FAST,
    "vision-labeling-model-fast",
    "label_version_fast"
)

object_model_fast = FritzLibrary(
    FritzLibraryNames.OBJECT_MODEL_FAST,
    "vision-object-detection-model-fast",
    "object_version_fast"
)

style_model = FritzLibrary(
    FritzLibraryNames.STYLE_PAINTING_MODELS,
    "vision-style-painting-models",
    "painting_version"
)

people_seg_model_fast = FritzLibrary(
    FritzLibraryNames.PEOPLE_SEG_MODEL_FAST,
    "vision-people-segmentation-model-fast",
    "people_version_fast"
)

people_seg_model_small = FritzLibrary(
    FritzLibraryNames.PEOPLE_SEG_MODEL_SMALL,
    "vision-people-segmentation-model-small",
    "people_version_small"
)

people_seg_model_accurate = FritzLibrary(
    FritzLibraryNames.PEOPLE_SEG_MODEL_ACCURATE,
    "vision-people-segmentation-model-accurate",
    "people_version_accurate"
)

pose_model_fast = FritzLibrary(
    FritzLibraryNames.POSE_MODEL_FAST,
    "vision-pose-estimation-model-fast",
    "human_pose_version_fast"
)

pose_model_small = FritzLibrary(
    FritzLibraryNames.POSE_MODEL_SMALL,
    "vision-pose-estimation-model-small",
    "human_pose_version_small"
)

pose_model_accurate = FritzLibrary(
    FritzLibraryNames.POSE_MODEL_ACCURATE,
    "vision-pose-estimation-model-accurate",
    "human_pose_version_accurate"
)

living_room_model_fast = FritzLibrary(
    FritzLibraryNames.LIVING_ROOM_SEG_MODEL_FAST,
    "vision-living-room-segmentation-model-fast",
    "living_room_version_fast"
)

living_room_model_small = FritzLibrary(
    FritzLibraryNames.LIVING_ROOM_SEG_MODEL_SMALL,
    "vision-living-room-segmentation-model-small",
    "living_room_version_small"
)

outdoor_model_fast = FritzLibrary(
    FritzLibraryNames.OUTDOOR_SEG_MODEL_FAST,
    "vision-outdoor-segmentation-model-fast",
    "outdoor_version_fast"
)

outdoor_model_small = FritzLibrary(
    FritzLibraryNames.OUTDOOR_SEG_MODEL_SMALL,
    "vision-outdoor-segmentation-model-small",
    "outdoor_version_small"
)

outdoor_model_accurate = FritzLibrary(
    FritzLibraryNames.OUTDOOR_SEG_MODEL_ACCURATE,
    "vision-outdoor-segmentation-model-accurate",
    "outdoor_version_accurate"
)

hair_seg_model_fast = FritzLibrary(
    FritzLibraryNames.HAIR_SEG_MODEL_FAST,
    "vision-hair-segmentation-model-fast",
    "hair_version_fast"
)

hair_seg_model_small = FritzLibrary(
    FritzLibraryNames.HAIR_SEG_MODEL_SMALL,
    "vision-hair-segmentation-model-small",
    "hair_version_small"
)

hair_seg_model_accurate = FritzLibrary(
    FritzLibraryNames.HAIR_SEG_MODEL_ACCURATE,
    "vision-hair-segmentation-model-accurate",
    "hair_version_accurate"
)

pet_seg_model_fast = FritzLibrary(
    FritzLibraryNames.PET_SEG_MODEL_FAST,
    "vision-pet-segmentation-model-fast",
    "pet_version_fast"
)

pet_seg_model_small = FritzLibrary(
    FritzLibraryNames.PET_SEG_MODEL_SMALL,
    "vision-pet-segmentation-model-small",
    "pet_version_small"
)

pet_seg_model_accurate = FritzLibrary(
    FritzLibraryNames.PET_SEG_MODEL_ACCURATE,
    "vision-pet-segmentation-model-accurate",
    "pet_version_accurate"
)

sky_seg_model_fast = FritzLibrary(
    FritzLibraryNames.SKY_SEG_MODEL_FAST,
    "vision-sky-segmentation-model-fast",
    "sky_version_fast"
)

sky_seg_model_small = FritzLibrary(
    FritzLibraryNames.SKY_SEG_MODEL_SMALL,
    "vision-sky-segmentation-model-small",
    "sky_version_small"
)

sky_seg_model_accurate = FritzLibrary(
    FritzLibraryNames.SKY_SEG_MODEL_ACCURATE,
    "vision-sky-segmentation-model-accurate",
    "sky_version_accurate"
)

vision_cv = FritzLibrary(
    FritzLibraryNames.VISION_CV,
    "vision-cv",
    "visionCV_version"
)

opencv = FritzLibrary(
    FritzLibraryNames.OPEN_CV,
    "opencv",
    "opencv_version"
)

tensorflow_lite = FritzLibrary(
    FritzLibraryNames.TENSORFLOW_LITE,
    "tensorflow-lite",
    "tensorflowlite_version"
)
tensorflow_lite_gpu = FritzLibrary(
    FritzLibraryNames.TENSORFLOW_LITE_GPU,
    "tensorflow-lite-gpu",
    "tensorflowlitegpu_version"
)


library_map = {
    FritzLibraryNames.CORE: core_library,
    FritzLibraryNames.VISION: vision_library,
    FritzLibraryNames.LABEL_MODEL_FAST: label_model_fast,
    FritzLibraryNames.OBJECT_MODEL_FAST: object_model_fast,
    FritzLibraryNames.STYLE_PAINTING_MODELS: style_model,
    FritzLibraryNames.PEOPLE_SEG_MODEL_FAST: people_seg_model_fast,
    FritzLibraryNames.PEOPLE_SEG_MODEL_SMALL: people_seg_model_small,
    FritzLibraryNames.PEOPLE_SEG_MODEL_ACCURATE: people_seg_model_accurate,
    FritzLibraryNames.POSE_MODEL_SMALL: pose_model_small,
    FritzLibraryNames.POSE_MODEL_FAST: pose_model_fast,
    FritzLibraryNames.POSE_MODEL_ACCURATE: pose_model_accurate,
    FritzLibraryNames.LIVING_ROOM_SEG_MODEL_FAST: living_room_model_fast,
    FritzLibraryNames.LIVING_ROOM_SEG_MODEL_SMALL: living_room_model_small,
    FritzLibraryNames.OUTDOOR_SEG_MODEL_FAST: outdoor_model_fast,
    FritzLibraryNames.OUTDOOR_SEG_MODEL_SMALL: outdoor_model_small,
    FritzLibraryNames.OUTDOOR_SEG_MODEL_ACCURATE: outdoor_model_accurate,
    FritzLibraryNames.HAIR_SEG_MODEL_FAST: hair_seg_model_fast,
    FritzLibraryNames.HAIR_SEG_MODEL_SMALL: hair_seg_model_small,
    FritzLibraryNames.HAIR_SEG_MODEL_ACCURATE: hair_seg_model_accurate,
    FritzLibraryNames.PET_SEG_MODEL_FAST: pet_seg_model_fast,
    FritzLibraryNames.PET_SEG_MODEL_SMALL: pet_seg_model_small,
    FritzLibraryNames.PET_SEG_MODEL_ACCURATE: pet_seg_model_accurate,
    FritzLibraryNames.SKY_SEG_MODEL_FAST: sky_seg_model_fast,
    FritzLibraryNames.SKY_SEG_MODEL_SMALL: sky_seg_model_small,
    FritzLibraryNames.SKY_SEG_MODEL_ACCURATE: sky_seg_model_accurate,
    FritzLibraryNames.VISION_CV: vision_cv,
    FritzLibraryNames.OPEN_CV: opencv,
    FritzLibraryNames.TENSORFLOW_LITE: tensorflow_lite,
    FritzLibraryNames.TENSORFLOW_LITE_GPU: tensorflow_lite_gpu,
}

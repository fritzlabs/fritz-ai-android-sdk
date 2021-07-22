import os
from invoke import task
from tasks import fritz_libraries
from tasks import helpers


def _extract_release_version_from_github_ref() -> str:
    # set from github actions
    github_ref = os.environ.get("GITHUB_REF")

    if not github_ref:
        raise Exception("No release version detected...aborting")

    sdk_release_version = github_ref.split("/")[-1]
    if not sdk_release_version.startswith("sdk-"):
        raise Exception("Tag does not start with sdk-...aborting")
    return sdk_release_version.split("sdk-")[-1]


def _build_step(ctx, command: str, error_msg: str):
    result = ctx.run(command)
    if not result.ok:
        raise Exception(error_msg)


@task
def docs(ctx, release_version=None):
    """Generate documentation.

    Args:
        ctx
    """
    if not release_version:
        release_version = _extract_release_version_from_github_ref()
    _build_step(ctx, "./deploy/build_dokka_documentation.sh", "Dokka failed to build")

    _build_step(
        ctx,
        f"./deploy/deploy_documentation.sh {release_version}",
        "Failed to deploy docs to s3",
    )


@task
def single_library(ctx, library_name, release_version):
    """Release a single library and update the version.

    Args:
        ctx: context
        library_name: the name of the module "fritzvisionlabelingmodel"
        release_version: the version.
    """
    library = fritz_libraries.library_map[library_name]
    if helpers.is_developing(library):
        print(f"{library.module_name} has dependencies in development...")
        if helpers.yes_or_no("Would you like to use distributed dependencies?"):
            helpers.replace_sdk_dependencies(
                fritz_libraries.LibraryReplacement.DEVELOP_TO_RELEASE
            )
        else:
            print("Aborting...")
            return

    library.change_version(release_version)
    ctx.run(f"./gradlew :{library.module_name}:publish")
    helpers.change_dependency(library)

    print(f"{library.module_name} ({release_version.strip()}) released successfully!")


@task
def all_models(ctx):
    """Release all models with the specified version.

    Args:
        ctx: the context.
    """
    models = [
        fritz_libraries.label_model_fast,
        fritz_libraries.object_model_fast,
        fritz_libraries.living_room_model_fast,
        fritz_libraries.living_room_model_small,
        fritz_libraries.outdoor_model_fast,
        fritz_libraries.outdoor_model_small,
        fritz_libraries.outdoor_model_accurate,
        fritz_libraries.hair_seg_model_fast,
        fritz_libraries.hair_seg_model_small,
        fritz_libraries.hair_seg_model_accurate,
        fritz_libraries.style_model,
        fritz_libraries.pose_model_fast,
        fritz_libraries.pose_model_small,
        fritz_libraries.pose_model_accurate,
        fritz_libraries.pet_seg_model_fast,
        fritz_libraries.pet_seg_model_small,
        fritz_libraries.pet_seg_model_accurate,
        fritz_libraries.sky_seg_model_fast,
        fritz_libraries.sky_seg_model_small,
        fritz_libraries.sky_seg_model_accurate,
        fritz_libraries.people_seg_model_fast,
        fritz_libraries.people_seg_model_small,
        fritz_libraries.people_seg_model_accurate,
    ]

    for lib in models:
        current_version = lib.get_current_version()
        single_library(ctx, lib.module_name, current_version)


@task
def sdk(ctx, release_version=None, warn=True):
    """Run a complete release of all the packages with the sdk version.

    Args:
        ctx
        release_version: the version to release
        tag: If we should add a git tag after this completes.
    """
    if not release_version:
        release_version = _extract_release_version_from_github_ref()

    # Change the SDK version
    fritz_libraries.core_library.change_version(release_version)

    # Build Steps for Core + TFL/TFM + Vision libraries
    build_steps = [[fritz_libraries.core_library], [fritz_libraries.vision_library]]

    fritz_tfl_builds = [
        fritz_libraries.tensorflow_lite,
        fritz_libraries.tensorflow_lite_gpu,
    ]

    # Use distributed version of the TFL builds
    for library in fritz_tfl_builds:
        helpers.change_dependency(library)

    for step_libraries in build_steps:
        for library in step_libraries:
            if helpers.is_developing(library):
                print(f"{library.module_name} is developing...aborting the release...")
                return

            result = ctx.run(f"./gradlew :{library.module_name}:publish", warn=True)
            if not result.ok:
                raise Exception(
                    f"{library.module_name} was not successfully published."
                )
            helpers.change_dependency(library)

    print("SUCCESSFULLY RELEASED VERSION: " + release_version)


@task
def tag_release(ctx, release_version):
    """Tag and update master with the newly released version."""
    helpers.tag_and_push(ctx, release_version)


@task
def studio_app(ctx):
    """Build the Fritz AI Studio release app."""
    helpers.replace_sdk_dependencies(
        fritz_libraries.LibraryReplacement.DEVELOP_TO_RELEASE
    )
    helpers.replace_app_dependencies(
        fritz_libraries.LibraryReplacement.DEVELOP_TO_RELEASE
    )

    ctx.run("./gradlew :app:assembleRelease")
    ctx.run("open app/build/outputs/apk/release")
    print("BUILT FRITZ AI STUDIO RELEASE")

import os
from invoke import task
import pathlib
from tasks import fritz_libraries
from tasks import helpers


@task
def local(ctx):
    """Develop on the libraries.

    Args:
        ctx: the context
    """
    helpers.replace_sdk_dependencies(
        fritz_libraries.LibraryReplacement.RELEASE_TO_DEVELOP)
    helpers.replace_app_dependencies(
        fritz_libraries.LibraryReplacement.RELEASE_TO_DEVELOP)


@task
def hosted(ctx):
    """Use the distributed libraries.

    Args:
        ctx: the context
    """
    helpers.replace_sdk_dependencies(
        fritz_libraries.LibraryReplacement.DEVELOP_TO_RELEASE)
    helpers.replace_app_dependencies(
        fritz_libraries.LibraryReplacement.DEVELOP_TO_RELEASE)

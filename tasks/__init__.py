from invoke import task, Collection
from invoke import task

from tasks import release
from tasks import develop
from tasks import models
from tasks import fritz_libraries


@task(default=True)
def list_tasks(ctx):
    """Lists all available tasks"""
    ctx.run("invoke --list")


@task
def show_sdk_libraries(ctx):
    """Show SDK libraries.

    Args:
        ctx: the context
    """
    for library in fritz_libraries.library_map.values():
        print(f"\n\nModule Name: {library.module_name}")
        print("------------------------------------")
        print(f"Development Name: {library.local_gradle_project_name}")
        print(f"Release Name: {library.distributed_gradle_project_name}")


# pylint: disable=invalid-name
namespace = Collection(list_tasks, show_sdk_libraries, release, develop, models)

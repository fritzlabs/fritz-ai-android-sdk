import pathlib
from tasks import fritz_libraries


def replace_sdk_dependencies(replacement_type, change_library=None):
    """Replace the dependencies for the SDK: develop -> release.

    Args:
        replacement_type - release -> develop or develop -> release
        change_library - specific name of the library to change.
            Default changes all of them.
    """
    for library in fritz_libraries.library_map.values():
        gradle_file_path = library.gradle_file_path
        new_contents = gradle_file_path.read_text()
        if change_library:
            match_str = change_library.local_gradle_project_name
            replace_str = change_library.distributed_gradle_project_name

            if (
                replacement_type
                == fritz_libraries.LibraryReplacement.RELEASE_TO_DEVELOP
            ):
                match_str = change_library.distributed_gradle_project_name
                replace_str = change_library.local_gradle_project_name

            new_contents = new_contents.replace(match_str, replace_str)
        else:
            for other_library in fritz_libraries.library_map.values():
                match_str = other_library.local_gradle_project_name
                replace_str = other_library.distributed_gradle_project_name

                if (
                    replacement_type
                    == fritz_libraries.LibraryReplacement.RELEASE_TO_DEVELOP
                ):
                    match_str = other_library.distributed_gradle_project_name
                    replace_str = other_library.local_gradle_project_name

                new_contents = new_contents.replace(match_str, replace_str)
        gradle_file_path.write_text(new_contents)


def replace_app_dependencies(replacement_type):
    """Replace the dependencies for the app.

    Args:
        replacement_type: release -> develop or develop -> release
    """
    gradle_file_path = pathlib.Path("app/build.gradle")
    new_contents = gradle_file_path.read_text()
    for other_library in fritz_libraries.library_map.values():
        match_str = other_library.local_gradle_project_name
        replace_str = other_library.distributed_gradle_project_name

        if replacement_type == fritz_libraries.LibraryReplacement.RELEASE_TO_DEVELOP:
            match_str = other_library.distributed_gradle_project_name
            replace_str = other_library.local_gradle_project_name

        new_contents = new_contents.replace(match_str, replace_str)

    gradle_file_path.write_text(new_contents)


def yes_or_no(question: str):
    """Prompt user to input y[es]/n[o] to a yes or no question.

    Args:
        question: Question to answer

    Returns: True if yes, False if no.
    """
    answer = input(question + " (y/n): ").lower().strip()

    while answer not in ["y", "n", "yes", "no"]:
        print("Input yes or no")
        answer = input(question + "(y/n):").lower().strip()

    if answer.startswith("y"):
        return True
    else:
        return False


def tag_and_push(ctx, release_version):
    ctx.run(f"git add {fritz_libraries.GRADLE_PROPERTIES_FILE}")
    ctx.run(f'git commit -am "Bump to version {release_version}"')
    ctx.run(f'git tag -a {release_version} -m "Release new version {release_version}"')
    ctx.run("git push")
    ctx.run("git push --tags")


def is_developing(library: fritz_libraries.FritzLibrary):
    gradle_file_path = library.gradle_file_path
    contents = gradle_file_path.read_text()
    for other_library in fritz_libraries.library_map.values():
        if other_library.local_gradle_project_name in contents:
            return True

    return False


def change_dependency(library: fritz_libraries.FritzLibrary, develop=False):
    """Change the package dependency between the distributed and local versions.

    Args:
        library: searches for dependencies with this library.
        develop: should use local libraries or released libraries.
    """
    # Target a specific library to change
    if not develop:
        replace_sdk_dependencies(
            fritz_libraries.LibraryReplacement.DEVELOP_TO_RELEASE, library
        )
        return

    replace_sdk_dependencies(
        fritz_libraries.LibraryReplacement.RELEASE_TO_DEVELOP, library
    )

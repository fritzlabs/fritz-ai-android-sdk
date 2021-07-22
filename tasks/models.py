import json
import pathlib
import fritz
from invoke import task


@task
def update_all(ctx):
    """Update all on device models to number in pinned version field."""
    fritz.configure()
    root = pathlib.Path('./')

    for path in root.rglob('**/src/main/assets/*.json'):

        data = json.load(path.open())
        if "pinned_version" not in data:
            continue

        version = data["pinned_version"]
        existing_version = data["model_version"]
        if version == existing_version:
            print("Version is same as existing version, continuing")
            continue

        output_dir = path.parent
        existing_model_name = pathlib.Path(data["model_path"]).name
        existing_model_path = output_dir / existing_model_name
        print(f"Updating {path}")
        model_id = data["model_id"]
        model = fritz.Model.get(model_id=model_id)

        _, output_path = model.download(
            version_number=version, output_dir=output_dir
        )
        output_path = pathlib.Path(output_path)
        data["model_path"] = f"file:///android_asset/{output_path.name}"
        data["model_version"] = version
        print(f" - Downloaded model to {output_path}.")
        if existing_model_path.exists():
            existing_model_path.unlink()
            print(f" - Removed existing model.")

        json.dump(data, path.open(mode='w'), indent=2)
        print(f" - Updated model file.")

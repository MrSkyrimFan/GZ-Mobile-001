"""Run the two published shell checks on harmless, isolated text samples."""
import argparse
from pathlib import Path
import subprocess
import tempfile

BRAND = "! grep -R -nE 'ChromaVale|Focavera|com[.]greenezone[.](chromavale|focavera)' app/src/main app/build.gradle.kts settings.gradle.kts"
PERMISSION = "! grep -R -nE 'android[.]permission[.](INTERNET|READ_MEDIA_IMAGES|READ_EXTERNAL_STORAGE|WRITE_EXTERNAL_STORAGE)' app/src/main"

def statuses(commands):
    with tempfile.TemporaryDirectory() as temp:
        root = Path(temp)
        app = root / "app" / "src" / "main"
        app.mkdir(parents=True)
        (root / "settings.gradle.kts").touch()
        (root / "app" / "build.gradle.kts").touch()
        result = []
        for label, contents in (("clean", "GZ Editor Pro"), ("old brand", "Focavera"),
                                ("permission", "android.permission.INTERNET"),
                                ("both", "Focavera android.permission.INTERNET")):
            (app / "sample.txt").write_text(contents)
            run = subprocess.run(["bash", "-e", "-o", "pipefail", "-c", commands],
                                 cwd=temp, capture_output=True, text=True)
            result.append((label, run.returncode))
        return result

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--workflow", type=Path, help="Verify commands against the pinned android-ci.yml")
    args = parser.parse_args()
    if args.workflow:
        statements = [s.strip() for s in args.workflow.read_text().splitlines()]
        assert any(statements[i:i+2] == [BRAND, PERMISSION] for i in range(len(statements) - 1))
    actual = statuses(BRAND + "\n" + PERMISSION)
    print("Pinned two-line step:", actual)
    expected = [("clean", 0), ("old brand", 0), ("permission", 1), ("both", 1)]
    if actual != expected:
        raise SystemExit("Unexpected shell behavior.")
    # Illustrative control. A production check should also distinguish grep errors.
    combined = statuses('''for pattern in 'Focavera' 'android[.]permission[.]INTERNET'; do
    if grep -RqE "$pattern" app/src/main; then exit 1; else code=$?; fi
    if [ "$code" != 1 ]; then exit "$code"; fi
done''')
    print("Explicit status control:", combined)
    if combined != [("clean", 0), ("old brand", 1), ("permission", 1), ("both", 1)]:
        raise SystemExit("Control did not detect both classes.")

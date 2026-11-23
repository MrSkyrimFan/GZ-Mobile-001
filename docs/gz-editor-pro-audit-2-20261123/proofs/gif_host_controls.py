"""Replay synthetic positive and negative controls for the two GIF checkers.
Requires Python 3, Pillow and ffmpeg. Uses temporary files. Does not run GZ.
"""
from pathlib import Path
from struct import pack
from tempfile import TemporaryDirectory
import subprocess
import sys


ROOT = Path(__file__).resolve().parent


def run_check(script, args, should_pass):
    result = subprocess.run([sys.executable, str(ROOT / script), *map(str, args)],
                            text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if (result.returncode == 0) != should_pass:
        raise RuntimeError(f"Unexpected checker result {result.returncode}: {result.stdout} {result.stderr}")
    print(("accepted" if should_pass else "rejected"), args[-1].name)


def set_delay(frame, ticks):
    return frame[:4] + pack("<H", ticks) + frame[6:]


def main():
    with TemporaryDirectory() as temp:
        output = Path(temp)
        source = (ROOT / "gif_inputs/gif_long_hold.gif").read_bytes()
        # These fixed, original 1x1 frames have one GCE and one image each.
        # General GIF input belongs in a real parser such as the checker uses.
        first = source.index(b"\x21\xf9")
        second = source.index(b"\x21\xf9", first + 1)
        red, blue = source[first:second], source[second:-1]
        for name, first_hold, second_hold, should_pass in [
            ("safe-split", 65534, 2, True),
            ("one-tick-tail", 65535, 1, False),
        ]:
            path = output / (name + ".gif")
            path.write_bytes(source[:first] + set_delay(red, first_hold)
                             + set_delay(red, second_hold) + set_delay(blue, 20) + b"\x3b")
            run_check("gif_hold_fixture.py", ["check", path], should_pass)

        for name, geometry, source_name, should_pass in [
            ("square", "320:180", "gif_aspect_square.mp4", True),
            ("wrong-header", "320:256", "gif_aspect_anamorphic.mp4", False),
            ("wrong-shape", "320:180", "gif_aspect_anamorphic.mp4", False),
        ]:
            path = output / (name + ".gif")
            command = f"scale={geometry}"
            if name == "wrong-shape":
                # Distort only the drawn circle, then place it in the correct container.
                command = "scale=224:180,pad=320:180:48:0:color=black"
            subprocess.run(["ffmpeg", "-nostdin", "-n", "-loglevel", "error", "-i",
                            str(ROOT / "gif_inputs" / source_name), "-vf", command, str(path)],
                           check=True)
            run_check("gif_check_display_aspect.py", [path], should_pass)


if __name__ == "__main__":
    main()

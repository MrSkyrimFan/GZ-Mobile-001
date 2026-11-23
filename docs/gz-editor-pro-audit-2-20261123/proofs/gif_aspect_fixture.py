"""Generate a synthetic non-square-pixel MP4 and an equivalent square-pixel control.
Requires Python 3, ffmpeg with libx264, and ffprobe. Pass a new output directory.
"""
import argparse
import json
from pathlib import Path
import subprocess


def picture(index):
    # The reference is a circle in a square-pixel 256 x 144 display raster.
    center_x = 72 + index * 4
    center_y = 72
    return bytes(
        235 if (x - center_x) ** 2 + (y - center_y) ** 2 <= 28 ** 2 else 20
        for y in range(144) for x in range(256)
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    args.directory.mkdir(parents=True, exist_ok=True)
    frames = b"".join(picture(i) for i in range(24))
    for name, width, sar in [("gif_aspect_square", 256, "1/1"),
                             ("gif_aspect_anamorphic", 180, "64/45")]:
        path = args.directory / (name + ".mp4")
        subprocess.run([
            "ffmpeg", "-nostdin", "-n", "-loglevel", "error", "-f", "rawvideo",
            "-pixel_format", "gray", "-video_size", "256x144", "-framerate", "12",
            "-i", "pipe:0", "-vf", f"scale={width}:144:flags=area,setsar={sar}",
            "-c:v", "libx264", "-pix_fmt", "yuv420p", "-threads", "1", "-g", "12",
            "-crf", "12", "-movflags", "+faststart", str(path)
        ], input=frames, check=True)
        raw = subprocess.check_output([
            "ffprobe", "-v", "error", "-select_streams", "v:0", "-show_entries",
            "stream=width,height,sample_aspect_ratio,display_aspect_ratio,nb_frames",
            "-of", "json", str(path)
        ])
        info = json.loads(raw)["streams"][0]
        assert info["width"] == width and info["height"] == 144
        assert info["sample_aspect_ratio"] == sar.replace("/", ":")
        assert info["display_aspect_ratio"] == "16:9" and info["nb_frames"] == "24"
        print(path.name, info)


if __name__ == "__main__":
    main()

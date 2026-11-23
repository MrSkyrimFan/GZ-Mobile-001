"""Check a Small, original-aspect export of either bundled SAR test MP4.
Requires Pillow. Frames have a bright circle on a dark field, no caption or crop.
"""
import argparse
from pathlib import Path
from PIL import Image


def verify(path):
    with Image.open(path) as animation:
        assert animation.size == (320, 180), f"{animation.size} instead of 320 x 180"
        assert animation.n_frames >= 4, "Too few frames to check motion"
        points = []
        for index in [0, animation.n_frames // 2, animation.n_frames - 1]:
            animation.seek(index)
            # Compression and palette border pixels can vary. The 150 threshold is
            # far from both the authored foreground (235) and background (20).
            mask = animation.convert("L").point(lambda lum: 255 if lum > 150 else 0)
            bounds = mask.getbbox()
            assert bounds, f"No circle in frame {index}"
            left, top, right, bottom = bounds
            width, height = right - left, bottom - top
            assert abs(width - height) <= 3, (index, "stretched marker", width, height)
            assert 65 <= height <= 75, (index, "unexpected marker scale", height)
            points.append((left + right) / 2)
        assert points[0] + 25 < points[1] and points[1] + 25 < points[2], points
        print(path.name, "dimensions/marker geometry/motion passed", points)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("gif", type=Path)
    verify(parser.parse_args().gif)

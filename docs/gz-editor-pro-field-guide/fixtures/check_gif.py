"""Read the included full-frame portrait clock from an exported animated GIF."""
import argparse
from pathlib import Path
from statistics import median
from PIL import Image

def read_counter(image):
    width, height = image.size
    gray = image.convert("L")
    bits = []
    for bit in range(8):
        x = round((30 + 30 * bit) * width / 270)
        y = round(200 * height / 480)
        level = median(gray.getpixel((x + dx, y + dy))
                       for dy in (-1, 0, 1) for dx in (-1, 0, 1))
        if 98 <= level <= 150:
            raise ValueError(f"ambiguous counter bit {bit}, gray level {level}")
        bits.append(int(level > 150))
    return sum(value << bit for bit, value in enumerate(bits))

def check(path, reverse):
    with Image.open(path) as animation:
        if animation.format != "GIF":
            raise ValueError("Expected a GIF.")
        if abs(animation.width / animation.height - 9 / 16) > 0.006:
            raise ValueError("Use the full portrait frame without bars or crop.")
        if animation.width < 32 or animation.height < 56:
            raise ValueError("The output is too small to resolve the fixture counter.")
        ids, delays = [], []
        for i in range(animation.n_frames):
            animation.seek(i)
            ids.append(read_counter(animation))
            delays.append(animation.info.get("duration", 0))
    print("Decoded source frame IDs:", ids)
    print("Frame delays (ms):", delays)
    if len(ids) != 18 or delays != [100] * 18:
        raise ValueError("Expected 18 frames at 100 ms for the specified recipe.")
    ordered = ids[::-1] if reverse else ids
    for position, source_id in enumerate(ordered):
        expected = 66 + 3 * position
        if abs(source_id - expected) > 1:
            raise ValueError(
                f"Source frame {source_id} lies outside {expected} ± 1 at position {position}."
            )
    if any(b - a not in (2, 3, 4) for a, b in zip(ordered, ordered[1:])):
        raise ValueError("Frame order or cadence differs from the 30 fps source at 10 fps output.")
    print("PASS for this fixture, direction, cadence and trim. Inspect the color and motion separately.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("gif", type=Path)
    parser.add_argument("--reverse", action="store_true")
    args = parser.parse_args()
    try:
        check(args.gif, args.reverse)
    except (OSError, ValueError) as error:
        parser.exit(1, f"FAIL: {error}\n")

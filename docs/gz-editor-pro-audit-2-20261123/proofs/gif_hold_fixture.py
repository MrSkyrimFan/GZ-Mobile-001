"""Build an original two-color GIF and verify raw delays for a 50% speed regression.
Usage: python gif_hold_fixture.py make NEW.gif
       python gif_hold_fixture.py check ANDROID_EXPORTED.gif

The checker needs Pillow. It accepts any valid split of a long hold that
preserves its visible color duration in both GIF time and GZ input policy.
"""
import argparse
from itertools import groupby
from pathlib import Path
from struct import pack


def make(path):
    # Logical canvas 1x1, red and blue, no loop. Legal 3-bit LZW
    # codes CLEAR, one palette index, EOI form each one-pixel image.
    data = bytearray(b"GIF89a" + pack("<HHBBB", 1, 1, 0x80, 0, 0))
    data.extend(bytes.fromhex("ff0000 0000ff"))
    for color, ticks in [(0, 32768), (1, 10)]:
        data.extend(b"\x21\xf9\x04\x04" + pack("<H", ticks) + b"\x00\x00")
        data.extend(b"\x2c" + pack("<HHHHB", 0, 0, 1, 1, 0))
        codes = 4 | (color << 3) | (5 << 6)
        data.extend(b"\x02\x02" + pack("<H", codes) + b"\x00")
    data.extend(b"\x3b")
    with path.open("xb") as handle:
        handle.write(data)


def inspect(path):
    from PIL import Image
    samples = []
    with Image.open(path) as image:
        for i in range(image.n_frames):
            image.seek(i)
            color = image.convert("RGB").getpixel((image.width // 2, image.height // 2))
            assert color in [(255, 0, 0), (0, 0, 255)], (i, color)
            samples.append((color, image.info["duration"]))
    return samples


def group_holds(samples, read_delay):
    return [(color, sum(read_delay(delay) for _, delay in group))
            for color, group in groupby(samples, key=lambda item: item[0])]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("operation", choices=["make", "check"])
    parser.add_argument("path", type=Path)
    args = parser.parse_args()
    if args.operation == "make":
        make(args.path)
    factor = 1 if args.operation == "make" else 2
    expected = [((255, 0, 0), 327680 * factor), ((0, 0, 255), 100 * factor)]
    samples = inspect(args.path)
    raw = group_holds(samples, lambda ms: ms)
    gz_reader = group_holds(samples, lambda ms: 100 if ms < 20 else ms)
    print("Encoded frame delays (ms):", [d for _, d in samples])
    print("Visible holds using raw delays:", raw)
    print("Visible holds using baseline GZ input rules:", gz_reader)
    assert raw == expected and gz_reader == expected, (raw, gz_reader, expected)


if __name__ == "__main__":
    main()

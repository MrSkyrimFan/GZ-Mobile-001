"""Independent PNG reader and fixture for the Regular opaque-cover/flip regression.

Requires Python 3 and Pillow. No Android project or device connection is used.
Run without arguments for positive and negative controls. Pass an exported PNG
to evaluate the edited file. Use the sibling regular_cover_source.png in the app.
"""
import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps


BASE = (31, 87, 139)
SECRET = (255, 0, 255)
ANCHOR = (249, 178, 25)
BLACK = (0, 0, 0)
SIZE = (256, 128)


def source_image():
    image = Image.new("RGB", SIZE, BASE)
    draw = ImageDraw.Draw(image)
    draw.rectangle((56, 59, 71, 68), fill=SECRET)
    draw.rectangle((211, 17, 226, 31), fill=ANCHOR)
    return image


def black_bar(image):
    covered = image.copy()
    # Referenced placed bar is 45% wide, 16% high, centered at 0.25, 0.50.
    ImageDraw.Draw(covered).rectangle((6, 53, 121, 74), fill=BLACK)
    return covered


def issues(image):
    if image.size != SIZE:
        return [f"expected an Original-size 256x128 PNG, got {image.size}"]
    rgba = image.convert("RGBA")
    probes = [
        ("the mirrored subject must be fully covered", (187, 61, 196, 67), BLACK),
        ("the gold orientation marker must be on the left", (32, 19, 42, 28), ANCHOR),
        ("the old subject location must show the blue background", (60, 62, 68, 67), BASE),
        ("the upper-right background must survive", (230, 10, 237, 17), BASE),
    ]
    failures = []
    for label, box, rgb in probes:
        left, top, right, bottom = box
        pixels = (rgba.getpixel((x, y)) for y in range(top, bottom) for x in range(left, right))
        for pixel in pixels:
            if pixel != (*rgb, 255):
                failures.append(f"{label}: found {pixel}, expected {(*rgb, 255)}")
                break
    return failures


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("exported_png", type=Path, nargs="?")
    parser.add_argument("--make-source", type=Path, help="write a new copy of the synthetic fixture")
    args = parser.parse_args()
    src = source_image()
    if args.make_source:
        src.save(args.make_source, "PNG")
    if args.exported_png:
        with Image.open(args.exported_png) as image:
            if image.format != "PNG":
                raise SystemExit("Expected a PNG with Original dimensions and neutral grade")
            problems = issues(image)
        if problems:
            raise SystemExit("\n".join(problems))
        print("PASS: protected subject, orientation and unaffected pixels match")
        return
    controls = [
        ("flip the protected image", ImageOps.mirror(black_bar(src)), True),
        ("flip then paint the old coordinates", black_bar(ImageOps.mirror(src)), False),
        ("omit the flip", black_bar(src), False),
        ("omit the cover", ImageOps.mirror(src), False),
        ("blanket cover", Image.new("RGB", SIZE, BLACK), False),
    ]
    for name, image, accepted in controls:
        problems = issues(image)
        if bool(problems) == accepted:
            raise SystemExit(f"unexpected control result: {name}: {problems}")
        print(f"{name}: {'accepted' if accepted else 'rejected'}")


if __name__ == "__main__":
    main()

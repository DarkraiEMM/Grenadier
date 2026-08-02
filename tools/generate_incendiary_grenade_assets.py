from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "armsrace" / "textures"
ITEM_DIR = TEXTURES / "item"
ENTITY_DIR = TEXTURES / "entity"

T = (0, 0, 0, 0)
BROWN_DARK = (47, 31, 23, 255)
BROWN_SHADOW = (62, 40, 28, 255)
BROWN_MID = (82, 53, 34, 255)
BROWN_LIGHT = (104, 69, 42, 255)
CHARCOAL = (24, 25, 24, 255)
STEEL = (43, 45, 43, 255)
STEEL_LIGHT = (64, 66, 63, 255)
ORANGE_DARK = (160, 57, 5, 255)
ORANGE = (224, 91, 8, 255)
ORANGE_LIGHT = (255, 144, 15, 255)
YELLOW = (255, 190, 36, 255)
BRASS_DARK = (117, 82, 29, 255)
BRASS = (181, 132, 48, 255)


def rect(draw, box, color):
    draw.rectangle(box, fill=color)


def make_item():
    image = Image.new("RGBA", (16, 16), T)
    d = ImageDraw.Draw(image)

    rect(d, (3, 5, 11, 14), CHARCOAL)
    rect(d, (4, 6, 10, 13), BROWN_MID)
    rect(d, (4, 6, 4, 12), BROWN_LIGHT)
    rect(d, (10, 6, 10, 12), BROWN_DARK)
    rect(d, (4, 13, 10, 14), (18, 19, 18, 255))

    # Fixed two-pixel heat warning band with a dark notch rhythm.
    rect(d, (4, 10, 10, 11), ORANGE)
    for x in (5, 8):
        d.point((x, 10), fill=ORANGE_DARK)
    for x in (6, 9):
        d.point((x, 11), fill=ORANGE_LIGHT)

    # Compact stepped flame stencil above the band.
    d.point((6, 9), fill=ORANGE_LIGHT)
    d.point((7, 8), fill=YELLOW)
    d.point((7, 7), fill=ORANGE)
    d.point((8, 9), fill=ORANGE)

    # Square igniter, vent, lever and pin.
    rect(d, (5, 2, 10, 5), CHARCOAL)
    rect(d, (6, 1, 9, 2), (16, 17, 16, 255))
    rect(d, (6, 3, 9, 4), STEEL)
    rect(d, (7, 2, 8, 3), (8, 9, 8, 255))
    rect(d, (11, 4, 13, 5), CHARCOAL)
    rect(d, (12, 5, 13, 12), CHARCOAL)
    rect(d, (11, 6, 12, 11), STEEL)
    d.point((10, 3), fill=BRASS)
    d.point((10, 4), fill=BRASS_DARK)

    image.save(ITEM_DIR / "incendiary_grenade.png", optimize=False)


def fill(draw, xy, color, accent=None):
    x0, y0, x1, y1 = xy
    if x1 <= x0 or y1 <= y0:
        return
    rect(draw, (x0, y0, x1 - 1, y1 - 1), color)
    if accent is not None and x1 - x0 >= 3 and y1 - y0 >= 3:
        draw.point((x0 + 1, y0 + 1), fill=accent)


def face_rects(u, v, dx, dy, dz):
    return {
        "west": (u, v + dz, u + dz, v + dz + dy),
        "north": (u + dz, v + dz, u + dz + dx, v + dz + dy),
        "east": (u + dz + dx, v + dz, u + dz + dx + dz, v + dz + dy),
        "south": (u + dz + dx + dz, v + dz, u + dz + dx + dz + dx, v + dz + dy),
        "up": (u + dz + dx, v, u + dz + dx + dx, v + dz),
        "down": (u + dz, v, u + dz + dx, v + dz),
    }


def paint_box(draw, u, v, dx, dy, dz, colors):
    faces = face_rects(u, v, dx, dy, dz)
    for name, color in zip(("west", "north", "east", "south", "up", "down"), colors):
        fill(draw, faces[name], color, BROWN_LIGHT if name == "north" and dy >= 8 else None)
    return faces


def paint_band(draw, face, local_y):
    x0, y0, x1, _ = face
    width = x1 - x0
    for row in range(2):
        rect(draw, (x0, y0 + local_y + row, x1 - 1, y0 + local_y + row), ORANGE if row == 0 else ORANGE_DARK)
    for index, x in enumerate(range(x0 + 1, x1 - 1, 3)):
        draw.point((x, y0 + local_y + (index & 1)), fill=ORANGE_LIGHT)


def make_entity():
    image = Image.new("RGBA", (64, 64), T)
    d = ImageDraw.Draw(image)
    brown = (BROWN_SHADOW, BROWN_MID, BROWN_DARK, BROWN_MID, BROWN_LIGHT, BROWN_DARK)
    dark = (CHARCOAL, STEEL, CHARCOAL, (32, 33, 32, 255), STEEL_LIGHT, (14, 15, 14, 255))
    brass = (BRASS_DARK, BRASS, BRASS_DARK, BRASS, (211, 160, 64, 255), (78, 54, 20, 255))

    body = paint_box(d, 0, 0, 8, 10, 6, brown)
    for side in ("west", "north", "east", "south"):
        paint_band(d, body[side], 5)

    # Flame stencil on the north face, directly above the wraparound band.
    nx0, ny0, _, _ = body["north"]
    for x, y, color in [
        (3, 4, ORANGE), (3, 3, YELLOW), (3, 2, ORANGE),
        (2, 4, ORANGE_LIGHT), (4, 4, ORANGE_DARK),
    ]:
        d.point((nx0 + x, ny0 + y), fill=color)

    paint_box(d, 0, 18, 9, 1, 7, dark)
    paint_box(d, 32, 0, 6, 1, 5, dark)
    cap = paint_box(d, 32, 8, 4, 2, 4, dark)
    paint_box(d, 32, 16, 1, 8, 2, dark)
    paint_box(d, 40, 28, 1, 1, 1, brass)

    # Painted square vent on the cap top face; no extra geometry.
    ux0, uy0, ux1, uy1 = cap["up"]
    if ux1 - ux0 >= 4 and uy1 - uy0 >= 4:
        rect(d, (ux0 + 1, uy0 + 1, ux1 - 2, uy1 - 2), (7, 8, 7, 255))

    image.save(ENTITY_DIR / "incendiary_grenade.png", optimize=False)


def main():
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    make_item()
    make_entity()


if __name__ == "__main__":
    main()

from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "armsrace" / "textures"
ITEM_DIR = TEXTURES / "item"
ENTITY_DIR = TEXTURES / "entity"

T = (0, 0, 0, 0)
OLIVE_BLACK = (31, 38, 22, 255)
OLIVE_DARK = (48, 59, 32, 255)
OLIVE = (69, 82, 43, 255)
OLIVE_LIGHT = (91, 104, 57, 255)
SEAM = (24, 29, 18, 255)
CHARCOAL = (22, 25, 25, 255)
STEEL = (47, 52, 52, 255)
STEEL_LIGHT = (76, 82, 80, 255)
YELLOW_DARK = (143, 103, 0, 255)
YELLOW = (218, 164, 8, 255)
YELLOW_LIGHT = (244, 196, 26, 255)
BRASS_DARK = (107, 72, 24, 255)
BRASS = (181, 132, 47, 255)


def rect(draw, box, color):
    draw.rectangle(box, fill=color)


def make_item():
    image = Image.new("RGBA", (16, 16), T)
    d = ImageDraw.Draw(image)

    rect(d, (3, 5, 11, 14), OLIVE_BLACK)
    rect(d, (4, 5, 10, 13), OLIVE)
    rect(d, (4, 5, 4, 13), OLIVE_LIGHT)
    rect(d, (10, 5, 10, 13), OLIVE_DARK)
    rect(d, (4, 8, 10, 8), SEAM)
    rect(d, (4, 12, 10, 12), SEAM)
    for x in (6, 9):
        rect(d, (x, 5, x, 13), SEAM)
    rect(d, (4, 9, 10, 10), YELLOW)
    rect(d, (4, 9, 10, 9), YELLOW_LIGHT)
    d.point((10, 10), fill=YELLOW_DARK)
    rect(d, (3, 14, 11, 14), CHARCOAL)

    rect(d, (5, 2, 10, 4), CHARCOAL)
    rect(d, (6, 1, 9, 2), STEEL_LIGHT)
    rect(d, (10, 3, 12, 4), CHARCOAL)
    rect(d, (11, 4, 13, 12), CHARCOAL)
    rect(d, (12, 5, 12, 11), STEEL)
    d.point((10, 2), fill=BRASS)
    d.point((10, 3), fill=BRASS_DARK)

    image.save(ITEM_DIR / "frag_grenade.png", optimize=False)


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
        fill(draw, faces[name], color, OLIVE_LIGHT if name == "north" and dy >= 8 else None)
    return faces


def paint_segmented_face(draw, face, width, height):
    x0, y0, _, _ = face
    for local_y in (3, 7):
        if local_y < height:
            rect(draw, (x0, y0 + local_y, x0 + width - 1, y0 + local_y), SEAM)
    for local_x in range(2, width, 3):
        rect(draw, (x0 + local_x, y0, x0 + local_x, y0 + height - 1), SEAM)
    rect(draw, (x0, y0 + 4, x0 + width - 1, y0 + 5), YELLOW)
    rect(draw, (x0, y0 + 4, x0 + width - 1, y0 + 4), YELLOW_LIGHT)
    if width >= 2:
        rect(draw, (x0 + width - 1, y0 + 5, x0 + width - 1, y0 + 5), YELLOW_DARK)


def make_entity():
    image = Image.new("RGBA", (64, 64), T)
    d = ImageDraw.Draw(image)
    olive = (OLIVE_DARK, OLIVE, OLIVE_BLACK, OLIVE_DARK, OLIVE_LIGHT, OLIVE_BLACK)
    dark = (CHARCOAL, STEEL, CHARCOAL, (34, 38, 38, 255), STEEL_LIGHT, (13, 15, 15, 255))
    brass = (BRASS_DARK, BRASS, BRASS_DARK, BRASS, (211, 161, 67, 255), (74, 48, 16, 255))

    body = paint_box(d, 0, 0, 8, 10, 6, olive)
    for side, width in (("west", 6), ("north", 8), ("east", 6), ("south", 8)):
        paint_segmented_face(d, body[side], width, 10)
    paint_box(d, 0, 18, 9, 1, 7, dark)
    paint_box(d, 32, 0, 6, 1, 5, dark)
    paint_box(d, 32, 8, 5, 2, 4, dark)
    paint_box(d, 32, 16, 1, 8, 2, dark)
    paint_box(d, 40, 28, 1, 1, 1, brass)

    image.save(ENTITY_DIR / "frag_grenade.png", optimize=False)


def main():
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    make_item()
    make_entity()


if __name__ == "__main__":
    main()

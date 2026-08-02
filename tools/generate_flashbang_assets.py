from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "armsrace" / "textures"
ITEM_DIR = TEXTURES / "item"
ENTITY_DIR = TEXTURES / "entity"

T = (0, 0, 0, 0)
GRAY_DARK = (55, 61, 63, 255)
GRAY_SHADOW = (72, 79, 82, 255)
GRAY_MID = (103, 112, 115, 255)
GRAY_LIGHT = (137, 146, 148, 255)
WHITE_SHADOW = (186, 201, 204, 255)
WHITE = (231, 235, 232, 255)
CHARCOAL = (25, 28, 29, 255)
STEEL = (48, 53, 55, 255)
STEEL_LIGHT = (80, 87, 89, 255)
CYAN_DARK = (6, 109, 141, 255)
CYAN = (12, 181, 218, 255)
CYAN_LIGHT = (57, 215, 236, 255)
BRASS_DARK = (116, 82, 29, 255)
BRASS = (180, 132, 49, 255)


def rect(draw, box, color):
    draw.rectangle(box, fill=color)


def make_item():
    image = Image.new("RGBA", (16, 16), T)
    d = ImageDraw.Draw(image)

    rect(d, (3, 5, 11, 14), CHARCOAL)
    rect(d, (4, 6, 10, 13), GRAY_MID)
    rect(d, (4, 6, 4, 12), GRAY_LIGHT)
    rect(d, (10, 6, 10, 12), GRAY_DARK)
    rect(d, (4, 13, 10, 14), (19, 22, 22, 255))

    rect(d, (4, 9, 10, 11), WHITE)
    rect(d, (4, 11, 10, 11), WHITE_SHADOW)
    rect(d, (6, 7, 8, 8), CYAN)
    d.point((6, 7), fill=CYAN_LIGHT)
    rect(d, (6, 12, 8, 12), CYAN_DARK)

    rect(d, (5, 2, 10, 5), CHARCOAL)
    rect(d, (5, 1, 10, 3), STEEL_LIGHT)
    for y in (1, 2):
        for x in (6, 8, 10):
            if x <= 10:
                d.point((x, y), fill=(9, 11, 11, 255))
    rect(d, (11, 4, 13, 5), CHARCOAL)
    rect(d, (12, 5, 13, 12), CHARCOAL)
    rect(d, (11, 6, 12, 11), STEEL)
    d.point((10, 3), fill=BRASS)
    d.point((10, 4), fill=BRASS_DARK)

    image.save(ITEM_DIR / "flashbang.png", optimize=False)


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
        fill(draw, faces[name], color, GRAY_LIGHT if name == "north" and dy >= 8 else None)
    return faces


def paint_white_band(draw, face, local_y):
    x0, y0, x1, _ = face
    rect(draw, (x0, y0 + local_y, x1 - 1, y0 + local_y + 1), WHITE)
    rect(draw, (x0, y0 + local_y + 2, x1 - 1, y0 + local_y + 2), WHITE_SHADOW)


def make_entity():
    image = Image.new("RGBA", (64, 64), T)
    d = ImageDraw.Draw(image)
    silver = (GRAY_SHADOW, GRAY_MID, GRAY_DARK, GRAY_MID, GRAY_LIGHT, GRAY_DARK)
    dark = (CHARCOAL, STEEL, CHARCOAL, (34, 38, 39, 255), STEEL_LIGHT, (14, 16, 16, 255))
    brass = (BRASS_DARK, BRASS, BRASS_DARK, BRASS, (210, 160, 66, 255), (78, 54, 20, 255))

    body = paint_box(d, 0, 0, 8, 10, 6, silver)
    for side in ("west", "north", "east", "south"):
        paint_white_band(d, body[side], 4)

    nx0, ny0, _, _ = body["north"]
    rect(d, (nx0 + 3, ny0 + 1, nx0 + 4, ny0 + 2), CYAN)
    d.point((nx0 + 3, ny0 + 1), fill=CYAN_LIGHT)
    rect(d, (nx0 + 3, ny0 + 8, nx0 + 4, ny0 + 8), CYAN_DARK)

    paint_box(d, 0, 18, 9, 1, 7, dark)
    paint_box(d, 32, 0, 6, 1, 5, dark)
    cap = paint_box(d, 32, 8, 6, 2, 5, dark)
    paint_box(d, 32, 17, 1, 8, 2, dark)
    paint_box(d, 40, 29, 1, 1, 1, brass)

    # Painted 3x3 square vent matrix; holes add no geometry.
    ux0, uy0, ux1, uy1 = cap["up"]
    for row in range(3):
        for column in range(3):
            x = ux0 + 1 + column * 2
            y = uy0 + 1 + row
            if x < ux1 and y < uy1:
                d.point((x, y), fill=(7, 9, 9, 255))

    image.save(ENTITY_DIR / "flashbang.png", optimize=False)


def main():
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    make_item()
    make_entity()


if __name__ == "__main__":
    main()

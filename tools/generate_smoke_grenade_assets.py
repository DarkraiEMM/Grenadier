from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "armsrace" / "textures"
ITEM_DIR = TEXTURES / "item"
ENTITY_DIR = TEXTURES / "entity"

TRANSPARENT = (0, 0, 0, 0)
OLIVE_DARK = (38, 44, 28, 255)
OLIVE_MID = (70, 79, 43, 255)
OLIVE_LIGHT = (91, 101, 57, 255)
OLIVE_SHADOW = (52, 59, 35, 255)
CHARCOAL = (27, 30, 28, 255)
STEEL = (48, 52, 50, 255)
STEEL_LIGHT = (69, 73, 70, 255)
BRASS_DARK = (116, 83, 31, 255)
BRASS = (174, 130, 52, 255)
SMOKE_DARK = (132, 136, 132, 255)
SMOKE_LIGHT = (184, 187, 183, 255)


def rect(draw: ImageDraw.ImageDraw, box, color):
    draw.rectangle(box, fill=color)


def make_item_textures():
    base = Image.new("RGBA", (16, 16), TRANSPARENT)
    d = ImageDraw.Draw(base)

    # Body outline and stepped olive metal faces.
    rect(d, (3, 5, 11, 14), CHARCOAL)
    rect(d, (4, 6, 10, 13), OLIVE_MID)
    rect(d, (4, 6, 4, 12), OLIVE_LIGHT)
    rect(d, (10, 6, 10, 12), OLIVE_SHADOW)
    rect(d, (4, 13, 10, 14), (20, 23, 21, 255))
    d.point((5, 7), fill=(102, 111, 66, 255))
    d.point((9, 12), fill=(43, 49, 29, 255))

    # Fuse cap, stepped top, safety lever and square brass pin.
    rect(d, (5, 2, 10, 5), CHARCOAL)
    rect(d, (6, 1, 9, 2), (20, 22, 21, 255))
    rect(d, (6, 3, 10, 4), STEEL)
    rect(d, (11, 4, 13, 5), CHARCOAL)
    rect(d, (12, 5, 13, 12), CHARCOAL)
    rect(d, (11, 6, 12, 11), STEEL)
    d.point((10, 4), fill=BRASS_DARK)
    d.point((10, 3), fill=BRASS)

    # Three offset puffs; intentionally avoids a medical-cross silhouette.
    for point, color in [
        ((5, 11), SMOKE_DARK), ((6, 10), SMOKE_LIGHT),
        ((5, 9), SMOKE_LIGHT), ((6, 8), SMOKE_DARK),
        ((7, 7), SMOKE_LIGHT),
    ]:
        d.point(point, fill=color)

    stripe = Image.new("RGBA", (16, 16), TRANSPARENT)
    sd = ImageDraw.Draw(stripe)
    rect(sd, (8, 6, 9, 12), (255, 255, 255, 255))
    sd.point((8, 7), fill=(224, 224, 224, 255))
    sd.point((9, 11), fill=(208, 208, 208, 255))

    base.save(ITEM_DIR / "signal_flare_base.png", optimize=False)
    stripe.save(ITEM_DIR / "signal_flare_stripe.png", optimize=False)


def fill_face(draw, xy, color, accent=None):
    x0, y0, x1, y1 = xy
    if x1 <= x0 or y1 <= y0:
        return
    rect(draw, (x0, y0, x1 - 1, y1 - 1), color)
    if accent is not None and x1 - x0 >= 3 and y1 - y0 >= 3:
        draw.point((x0 + 1, y0 + 1), fill=accent)
        draw.point((x1 - 2, y1 - 2), fill=accent)


def paint_box_uv(draw, u, v, dx, dy, dz, palette, smoke_emblem=False):
    west, north, east, south, up, down = palette
    # Matches ModelPart.Cube's standard unfolded cuboid layout.
    fill_face(draw, (u, v + dz, u + dz, v + dz + dy), west)
    fill_face(draw, (u + dz, v + dz, u + dz + dx, v + dz + dy), north, OLIVE_LIGHT if smoke_emblem else None)
    fill_face(draw, (u + dz + dx, v + dz, u + dz + dx + dz, v + dz + dy), east)
    fill_face(draw, (u + dz + dx + dz, v + dz, u + dz + dx + dz + dx, v + dz + dy), south)
    fill_face(draw, (u + dz + dx, v, u + dz + dx + dx, v + dz), up)
    fill_face(draw, (u + dz, v, u + dz + dx, v + dz), down)

    if smoke_emblem:
        # North face occupies [u+dz, u+dz+dx) x [v+dz, v+dz+dy).
        ox = u + dz + 2
        oy = v + dz + 3
        for px, py, color in [
            (0, 4, SMOKE_DARK), (1, 3, SMOKE_LIGHT),
            (0, 2, SMOKE_LIGHT), (1, 1, SMOKE_DARK),
            (2, 0, SMOKE_LIGHT),
        ]:
            draw.point((ox + px, oy + py), fill=color)


def make_entity_textures():
    atlas = Image.new("RGBA", (64, 64), TRANSPARENT)
    d = ImageDraw.Draw(atlas)
    olive = (OLIVE_SHADOW, OLIVE_MID, OLIVE_DARK, OLIVE_MID, OLIVE_LIGHT, OLIVE_DARK)
    dark = (CHARCOAL, STEEL, CHARCOAL, (33, 36, 34, 255), STEEL_LIGHT, (18, 20, 19, 255))
    brass = (BRASS_DARK, BRASS, BRASS_DARK, BRASS, (207, 162, 70, 255), (80, 58, 24, 255))

    paint_box_uv(d, 0, 0, 8, 10, 6, olive, smoke_emblem=True)
    paint_box_uv(d, 0, 18, 9, 1, 7, dark)
    paint_box_uv(d, 32, 0, 6, 2, 5, dark)
    paint_box_uv(d, 32, 8, 5, 1, 4, dark)
    paint_box_uv(d, 32, 14, 1, 8, 2, dark)
    paint_box_uv(d, 40, 26, 1, 1, 1, brass)

    # A few deliberate single-pixel wear marks, kept off transparent space.
    d.point((8, 7), fill=(105, 113, 67, 255))
    d.point((12, 14), fill=(42, 48, 28, 255))

    stripe = Image.new("RGBA", (64, 64), TRANSPARENT)
    sd = ImageDraw.Draw(stripe)
    # The two thin stripe cuboids both start in the first 16 texels.
    rect(sd, (0, 0, 15, 15), (255, 255, 255, 255))
    for y in range(1, 15, 3):
        sd.point((2, y), fill=(218, 218, 218, 255))
        sd.point((10, y + 1), fill=(218, 218, 218, 255))

    atlas.save(ENTITY_DIR / "smoke_grenade.png", optimize=False)
    stripe.save(ENTITY_DIR / "smoke_grenade_stripe.png", optimize=False)


def main():
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    make_item_textures()
    make_entity_textures()


if __name__ == "__main__":
    main()

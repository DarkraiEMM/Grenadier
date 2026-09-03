from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/grenadier"
MODEL_DIR = ASSETS / "models/item"
TEXTURE_DIR = ASSETS / "textures/item"

# UV regions are expressed in Java model's 0..16 coordinate space.
UV = {
    "olive": [0, 0, 4, 4],
    "graphite": [4, 0, 8, 4],
    "metal": [8, 0, 12, 4],
    "red": [12, 0, 16, 4],
    "tint": [0, 4, 4, 8],
    "vents": [4, 4, 8, 8],
    "frag": [8, 4, 12, 8],
    "impact": [12, 4, 16, 8],
    "yellow": [0, 8, 4, 12],
    "dark_olive": [4, 8, 8, 12],
}


def paint_region(draw: ImageDraw.ImageDraw, cell_x: int, cell_y: int, colors: tuple[str, str, str], pattern=None):
    x0, y0 = cell_x * 16, cell_y * 16
    hi, mid, shadow = colors
    draw.rectangle((x0, y0, x0 + 15, y0 + 15), fill=mid)
    draw.line((x0, y0, x0 + 15, y0), fill=hi)
    draw.line((x0, y0, x0, y0 + 15), fill=hi)
    draw.line((x0, y0 + 15, x0 + 15, y0 + 15), fill=shadow)
    draw.line((x0 + 15, y0, x0 + 15, y0 + 15), fill=shadow)
    if pattern:
        pattern(draw, x0, y0, hi, mid, shadow)


def vent_pattern(draw, x0, y0, hi, mid, shadow):
    for y in (3, 7, 11):
        draw.rectangle((x0 + 3, y0 + y, x0 + 5, y0 + y + 2), fill="#171A19")
        draw.rectangle((x0 + 10, y0 + y, x0 + 12, y0 + y + 2), fill="#252927")


def frag_pattern(draw, x0, y0, hi, mid, shadow):
    for p in (5, 10):
        draw.line((x0 + p, y0 + 1, x0 + p, y0 + 14), fill=shadow)
        draw.line((x0 + 1, y0 + p, x0 + 14, y0 + p), fill=shadow)
    for x, y in ((3, 3), (8, 3), (13, 3), (3, 8), (8, 8), (13, 8)):
        draw.point((x0 + x, y0 + y), fill=hi)


def impact_pattern(draw, x0, y0, hi, mid, shadow):
    for y in range(2, 15, 4):
        for x in range(2 + (y // 4 % 2) * 2, 15, 4):
            draw.point((x0 + x, y0 + y), fill=hi)
            if x + 1 < 15:
                draw.point((x0 + x + 1, y0 + y + 1), fill=shadow)


def make_texture():
    image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    paint_region(draw, 0, 0, ("#71804A", "#4F5B32", "#2D351F"))
    paint_region(draw, 1, 0, ("#4A504C", "#252927", "#171A19"))
    paint_region(draw, 2, 0, ("#D2CCB4", "#A39D83", "#6F6A5B"))
    paint_region(draw, 3, 0, ("#D25B4C", "#A6322B", "#641F1C"))
    paint_region(draw, 0, 1, ("#FFFFFF", "#E7E7E1", "#9A9A92"))
    paint_region(draw, 1, 1, ("#E4E5DE", "#AEB3AF", "#606864"), vent_pattern)
    paint_region(draw, 2, 1, ("#71804A", "#4F5B32", "#27301D"), frag_pattern)
    paint_region(draw, 3, 1, ("#74814D", "#535F36", "#303820"), impact_pattern)
    paint_region(draw, 0, 2, ("#E2B94D", "#B58A25", "#735414"))
    paint_region(draw, 1, 2, ("#405740", "#2C4030", "#1F3024"))
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURE_DIR / "grenade_materials.png")
    stripe = Image.new("RGBA", (64, 64), (255, 255, 255, 255))
    stripe.save(ASSETS / "textures/entity/smoke_grenade_stripe.png")


def faces(material: str, tint: int | None = None):
    result = {}
    for direction in ("north", "east", "south", "west", "up", "down"):
        face = {"uv": UV[material], "texture": "#materials"}
        if tint is not None:
            face["tintindex"] = tint
        result[direction] = face
    return result


def cube(name: str, start, end, material: str, tint: int | None = None):
    return {"name": name, "from": start, "to": end, "faces": faces(material, tint)}


def ring(x: float, y: float, z: float):
    return [
        cube("ring_top", [x, y + 2.5, z], [x + 2.5, y + 3, z + 0.5], "metal"),
        cube("ring_bottom", [x, y, z], [x + 2.5, y + 0.5, z + 0.5], "metal"),
        cube("ring_outer", [x + 2, y + 0.5, z], [x + 2.5, y + 2.5, z + 0.5], "metal"),
    ]


DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.82, 0.82, 0.82]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.42, 0.42, 0.42]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.72, 0.72, 0.72]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 1], "scale": [0.55, 0.55, 0.55]},
    "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 2, 1], "scale": [0.55, 0.55, 0.55]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [1.5, 3, 1], "scale": [0.62, 0.62, 0.62]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [1.5, 3, 1], "scale": [0.62, 0.62, 0.62]},
}


def smoke():
    parts = [
        cube("body", [5, 2, 5], [11, 12, 11], "olive"),
        cube("body_facets", [5.5, 2, 4.5], [10.5, 12, 11.5], "olive"),
        cube("bottom_collar", [4.75, 1, 4.75], [11.25, 2.25, 11.25], "dark_olive"),
        cube("top_shoulder", [4.75, 11, 4.75], [11.25, 12.5, 11.25], "dark_olive"),
        cube("fuze", [6, 12.5, 6], [10, 14.5, 10], "graphite"),
        cube("fuze_cap", [5.5, 14.5, 5.5], [10.5, 15.5, 10.5], "dark_olive"),
        cube("lever", [10.4, 10, 6], [11.2, 14.5, 8], "graphite"),
        cube("stripe_n", [5, 7, 4.4], [11, 8.4, 4.7], "tint", 1),
        cube("stripe_s", [5, 7, 11.3], [11, 8.4, 11.6], "tint", 1),
        cube("stripe_w", [4.4, 7, 5], [4.7, 8.4, 11], "tint", 1),
        cube("stripe_e", [11.3, 7, 5], [11.6, 8.4, 11], "tint", 1),
    ]
    parts.extend(ring(10.5, 12, 4.25))
    return parts


def incendiary():
    parts = [
        cube("red_body", [4.5, 3, 4.75], [11.5, 11.5, 11.25], "red"),
        cube("red_facets", [5, 2.75, 4.25], [11, 11.5, 11.75], "red"),
        cube("bottom_collar", [4.25, 1.5, 4.5], [11.75, 3, 11.5], "graphite"),
        cube("top_collar", [4.25, 11.25, 4.5], [11.75, 12.5, 11.5], "graphite"),
        cube("fuze", [6, 12.5, 6], [10, 14.5, 10], "graphite"),
        cube("fuze_cap", [5.5, 14.5, 5.5], [10.5, 15.5, 10.5], "graphite"),
        cube("lever", [10.4, 10.5, 6], [11.2, 14.5, 8], "graphite"),
    ]
    parts.extend(ring(10.5, 12, 4.25))
    return parts


def flashbang():
    parts = [
        cube("vented_body", [5, 2.5, 5], [11, 12.5, 11], "vents"),
        cube("body_facets", [5.5, 3, 4.5], [10.5, 12, 11.5], "vents"),
        cube("bottom_collar", [4.75, 1.25, 4.75], [11.25, 2.75, 11.25], "graphite"),
        cube("top_collar", [4.5, 12, 4.5], [11.5, 13.25, 11.5], "graphite"),
        cube("fuze", [6, 13.25, 6], [10, 15, 10], "graphite"),
        cube("fuze_cap", [5.5, 15, 5.5], [10.5, 16, 10.5], "graphite"),
        cube("lever", [10.5, 10.5, 6], [11.25, 15, 8], "graphite"),
    ]
    parts.extend(ring(10.6, 12.5, 4.25))
    return parts


def frag():
    parts = [
        cube("body_center", [4, 4.5, 4.5], [12, 11, 11.5], "frag"),
        cube("body_upper", [5, 10, 5], [11, 12.75, 11], "frag"),
        cube("body_lower", [5, 2.5, 5], [11, 5, 11], "frag"),
        cube("body_wide", [4.5, 4.5, 4], [11.5, 12, 12], "frag"),
        cube("neck", [6, 12.25, 6], [10, 13.75, 10], "dark_olive"),
        cube("fuze_cap", [5.5, 13.5, 5.5], [10.5, 14.75, 10.5], "graphite"),
        cube("spoon", [10.5, 5.5, 6], [11.5, 14, 8], "graphite"),
    ]
    parts.extend(ring(10.5, 11.5, 4.25))
    return parts


def impact():
    parts = [
        cube("round_center", [4.5, 4.5, 5], [11.5, 10, 11], "impact"),
        cube("round_cross", [5, 4, 4.5], [11, 10.5, 11.5], "impact"),
        cube("round_upper", [5.5, 9.5, 5.5], [10.5, 11.75, 10.5], "impact"),
        cube("round_lower", [5.5, 3, 5.5], [10.5, 5, 10.5], "impact"),
        cube("fuze_collar", [6, 11.25, 6], [10, 12.75, 10], "graphite"),
        cube("yellow_cap", [6.5, 12.5, 6.5], [9.5, 13.75, 9.5], "yellow"),
    ]
    parts.extend(ring(9.5, 10.5, 4.5))
    return parts


def write_model(name: str, elements):
    model = {
        "credit": "Approved Grenadier TACZ-inspired original low-poly pass",
        "gui_light": "side",
        "textures": {"materials": "grenadier:item/grenade_materials", "particle": "grenadier:item/grenade_materials"},
        "display": DISPLAY,
        "elements": elements,
    }
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    (MODEL_DIR / f"{name}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")


def recolor_entity_textures():
    entity_dir = ASSETS / "textures/entity"
    incendiary = Image.open(entity_dir / "incendiary_grenade.png").convert("RGBA")
    pixels = incendiary.load()
    for y in range(incendiary.height):
        for x in range(incendiary.width):
            r, g, b, a = pixels[x, y]
            if a and r > g * 1.2 and r > 60:
                brightness = max(r, g, b)
                pixels[x, y] = (max(90, brightness), max(24, brightness // 3), max(20, brightness // 4), a)
    incendiary.save(entity_dir / "incendiary_grenade.png")

    impact_texture = Image.open(entity_dir / "impact_grenade.png").convert("RGBA")
    pixels = impact_texture.load()
    for y in range(impact_texture.height):
        for x in range(impact_texture.width):
            r, g, b, a = pixels[x, y]
            if a and r > g * 1.25 and r > 70:
                pixels[x, y] = (181, 138, 37, a)
    impact_texture.save(entity_dir / "impact_grenade.png")


def main():
    make_texture()
    for name, elements in {
        "signal_flare": smoke(),
        "incendiary_grenade": incendiary(),
        "flashbang": flashbang(),
        "frag_grenade": frag(),
        "impact_grenade": impact(),
    }.items():
        write_model(name, elements)
    recolor_entity_textures()


if __name__ == "__main__":
    main()

from __future__ import annotations

import json
import sys
import uuid
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
RESOURCE_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "armsrace"
TEXTURE_DIR = RESOURCE_ROOT / "textures" / "block"
BLOCK_MODEL_DIR = RESOURCE_ROOT / "models" / "block"
ITEM_MODEL_DIR = RESOURCE_ROOT / "models" / "item"
SOURCE_DIR = ROOT / "blockbench"
PREVIEW_DIR = ROOT / "build" / "reports" / "signal-beacon-assets"

INACTIVE_TEXTURE = TEXTURE_DIR / "tactical_signal_beacon_olive.png"
ACTIVE_TEXTURE = TEXTURE_DIR / "tactical_signal_beacon_olive_active.png"

COLORS = {
    "transparent": (0, 0, 0, 0),
    "graphite_black": "#1B1E1D",
    "graphite_mid": "#343936",
    "graphite_light": "#565D58",
    "olive_shadow": "#30371F",
    "olive_dark": "#46522A",
    "olive_mid": "#62713A",
    "olive_light": "#7E8C4D",
    "forest_dark": "#26382B",
    "forest_mid": "#38523F",
    "forest_light": "#54705A",
    "sand_shadow": "#736F60",
    "sand_mid": "#99937D",
    "sand_light": "#B9B29A",
    "amber_dark": "#9F5E0D",
    "amber_mid": "#D18B24",
    "amber_light": "#F0AD31",
    "cyan_dark": "#5A9495",
    "cyan_mid": "#A9DDDA",
    "cyan_light": "#DDFBF5",
    "emitter_white": "#F3FFFC",
}

# Pixel rectangles use [left, top, right, bottom), matching the approved guide.
UV = {
    "BODY_SIDE": (0, 0, 14, 8),
    "BODY_TOP": (16, 0, 30, 14),
    "BODY_BOTTOM": (32, 0, 46, 14),
    "BASE_SIDE": (0, 10, 16, 12),
    "BASE_TOP": (0, 14, 16, 30),
    "BASE_BOTTOM": (16, 14, 32, 30),
    "SHOULDER_SIDE": (32, 16, 44, 18),
    "SHOULDER_TOP": (32, 20, 44, 32),
    "SHOULDER_BOTTOM": (44, 20, 56, 32),
    "CHANNEL_TOP": (0, 32, 10, 42),
    "CHANNEL_SIDE": (12, 32, 22, 33),
    "EMITTER_TOP": (24, 32, 32, 40),
    "EMITTER_SIDE": (34, 32, 42, 33),
    "FRAME_TOP": (0, 44, 8, 46),
    "FRAME_LONG_SIDE": (0, 47, 8, 48),
    "FRAME_SHORT_SIDE": (9, 47, 11, 48),
    "FRAME_BOTTOM": (0, 49, 8, 51),
    "CAP_TOP": (12, 44, 14, 46),
    "CAP_SIDE": (15, 44, 17, 46),
    "CAP_BOTTOM": (18, 44, 20, 46),
}

CUBES = [
    ("base_plinth", [0, 0, 0], [16, 2, 16], "base"),
    ("main_casing", [1, 2, 1], [15, 10, 15], "body"),
    ("upper_shoulder", [2, 10, 2], [14, 12, 14], "shoulder"),
    ("inner_channel", [3, 12, 3], [13, 13, 13], "channel"),
    ("emitter_surface", [4, 13, 4], [12, 14, 12], "emitter"),
    ("frame_north", [4, 13, 2], [12, 14, 4], "frame_ns"),
    ("frame_south", [4, 13, 12], [12, 14, 14], "frame_ns"),
    ("frame_west", [2, 13, 4], [4, 14, 12], "frame_we"),
    ("frame_east", [12, 13, 4], [14, 14, 12], "frame_we"),
    ("cap_north_west", [2, 13, 2], [4, 15, 4], "cap"),
    ("cap_north_east", [12, 13, 2], [14, 15, 4], "cap"),
    ("cap_south_west", [2, 13, 12], [4, 15, 14], "cap"),
    ("cap_south_east", [12, 13, 12], [14, 15, 14], "cap"),
]

ANCHORS = {
    "beam_anchor": [8, 14, 8],
    "smoke_north": [8, 14.1, 4.5],
    "smoke_south": [8, 14.1, 11.5],
    "smoke_west": [4.5, 14.1, 8],
    "smoke_east": [11.5, 14.1, 8],
}


def fill(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], color: str) -> None:
    x0, y0, x1, y1 = rect
    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=COLORS[color])


def pixel(draw: ImageDraw.ImageDraw, x: int, y: int, color: str) -> None:
    draw.point((x, y), fill=COLORS[color])


def flecks(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], entries: list[tuple[int, int, str]]) -> None:
    """Place a small deterministic set of vanilla-style material pixels inside a UV island."""
    x0, y0, x1, y1 = rect
    for dx, dy, color in entries:
        x, y = x0 + dx, y0 + dy
        assert x0 <= x < x1 and y0 <= y < y1
        pixel(draw, x, y, color)


def hard_border(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], base: str, light: str, dark: str) -> None:
    x0, y0, x1, y1 = rect
    fill(draw, rect, base)
    draw.line((x0, y0, x1 - 1, y0), fill=COLORS[light])
    draw.line((x0, y0, x0, y1 - 1), fill=COLORS[light])
    draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=COLORS[dark])
    draw.line((x1 - 1, y0, x1 - 1, y1 - 1), fill=COLORS[dark])


def outlined_bevel(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], base: str, light: str, dark: str) -> None:
    """Vanilla-like one-pixel silhouette plus stepped, non-smoothed bevel."""
    x0, y0, x1, y1 = rect
    fill(draw, rect, "graphite_black")
    if x1 - x0 < 4 or y1 - y0 < 4:
        return
    draw.rectangle((x0 + 1, y0 + 1, x1 - 2, y1 - 2), fill=COLORS[base])
    draw.line((x0 + 2, y0 + 1, x1 - 3, y0 + 1), fill=COLORS[light])
    draw.line((x0 + 1, y0 + 2, x0 + 1, y1 - 3), fill=COLORS[light])
    draw.line((x0 + 2, y1 - 2, x1 - 2, y1 - 2), fill=COLORS[dark])
    draw.line((x1 - 2, y0 + 2, x1 - 2, y1 - 3), fill=COLORS[dark])


def soften_hard_border(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], light: str, base: str, dark: str) -> None:
    """Add clustered one-pixel transitions without blur, noise, or mixed texel scale."""
    x0, y0, x1, y1 = rect
    width, height = x1 - x0, y1 - y0
    if width < 6 or height < 6:
        return
    for offset in range(2, width - 2, 4):
        pixel(draw, x0 + offset, y0, base)
        pixel(draw, x0 + offset + 1, y0 + 1, light)
    for offset in range(3, width - 2, 5):
        pixel(draw, x0 + offset, y1 - 1, base)
        pixel(draw, x0 + offset - 1, y1 - 2, dark)
    for offset in range(2, height - 2, 4):
        pixel(draw, x0, y0 + offset, base)
        pixel(draw, x0 + 1, y0 + offset + 1, light)
    for offset in range(3, height - 2, 5):
        pixel(draw, x1 - 1, y0 + offset, base)
        pixel(draw, x1 - 2, y0 + offset - 1, dark)


def paint_body_side(draw: ImageDraw.ImageDraw) -> None:
    x0, y0, _, _ = UV["BODY_SIDE"]
    # One-pixel armoured rim surrounding an asymmetric recessed equipment bay.
    # Asymmetry is intentional: a centred bright bar reads like a face when the
    # 14x8 island is magnified on a block.
    fill(draw, UV["BODY_SIDE"], "olive_dark")
    draw.line((x0 + 1, y0, x0 + 11, y0), fill=COLORS["olive_light"])
    draw.line((x0, y0 + 1, x0, y0 + 5), fill=COLORS["olive_mid"])
    draw.line((x0 + 2, y0 + 7, x0 + 12, y0 + 7), fill=COLORS["olive_shadow"])
    draw.line((x0 + 13, y0 + 2, x0 + 13, y0 + 6), fill=COLORS["olive_shadow"])
    # Low-frequency material variation: coherent clusters, not a repeating
    # checker pattern around the entire block.
    draw.line((x0 + 2, y0, x0 + 4, y0), fill=COLORS["olive_light"])
    draw.line((x0 + 5, y0, x0 + 9, y0), fill=COLORS["olive_mid"])
    draw.line((x0 + 3, y0 + 7, x0 + 6, y0 + 7), fill=COLORS["olive_shadow"])

    # Black gasket and inset bay.
    draw.rectangle((x0 + 2, y0 + 2, x0 + 11, y0 + 5), fill=COLORS["graphite_black"])
    draw.line((x0 + 3, y0 + 2, x0 + 9, y0 + 2), fill=COLORS["graphite_mid"])
    # Left-hand vent stack.
    draw.rectangle((x0 + 3, y0 + 3, x0 + 5, y0 + 4), fill=COLORS["graphite_mid"])
    pixel(draw, x0 + 3, y0 + 3, "graphite_light")
    pixel(draw, x0 + 5, y0 + 4, "graphite_black")

    # Right-hand control cassette: two dark rails, one tiny amber status lamp.
    draw.rectangle((x0 + 7, y0 + 3, x0 + 10, y0 + 4), fill=COLORS["graphite_mid"])
    draw.line((x0 + 7, y0 + 3, x0 + 9, y0 + 3), fill=COLORS["graphite_light"])
    pixel(draw, x0 + 7, y0 + 4, "sand_shadow")
    pixel(draw, x0 + 8, y0 + 4, "amber_light")
    pixel(draw, x0 + 9, y0 + 4, "amber_dark")
    pixel(draw, x0 + 10, y0 + 4, "graphite_light")

    # Sparse, intentional wear; never a noisy full-surface filter.
    flecks(draw, UV["BODY_SIDE"], [
        (1, 2, "olive_light"), (12, 2, "olive_mid"),
        (1, 5, "olive_shadow"), (11, 6, "olive_mid"),
    ])
    # Selective outline: only the four vertical corners of the middle casing.
    # Left/right UV borders are shared by all four side faces.
    draw.line((x0, y0 + 1, x0, y0 + 6), fill=COLORS["graphite_mid"])
    draw.line((x0 + 13, y0 + 1, x0 + 13, y0 + 6), fill=COLORS["graphite_mid"])


def paint_emitter(draw: ImageDraw.ImageDraw, active: bool) -> None:
    x0, y0, x1, y1 = UV["EMITTER_TOP"]
    if active:
        fill(draw, (x0, y0, x1, y1), "cyan_dark")
        draw.rectangle((x0 + 1, y0 + 1, x1 - 2, y1 - 2), fill=COLORS["cyan_mid"])
        draw.rectangle((x0 + 2, y0 + 2, x0 + 5, y0 + 5), fill=COLORS["emitter_white"])
        pixel(draw, x0 + 1, y0 + 1, "cyan_light")
        pixel(draw, x0 + 2, y0 + 1, "cyan_light")
        pixel(draw, x0 + 1, y0 + 2, "cyan_light")
    else:
        fill(draw, (x0, y0, x1, y1), "graphite_black")
        draw.rectangle((x0 + 1, y0 + 1, x1 - 2, y1 - 2), fill=COLORS["graphite_mid"])
        draw.rectangle((x0 + 2, y0 + 2, x1 - 3, y1 - 3), fill=COLORS["forest_dark"])
        draw.rectangle((x0 + 3, y0 + 3, x0 + 4, y0 + 4), fill=COLORS["sand_mid"])


def build_texture(active: bool) -> Image.Image:
    image = Image.new("RGBA", (64, 64), COLORS["transparent"])
    draw = ImageDraw.Draw(image)
    paint_body_side(draw)
    hard_border(draw, UV["BODY_TOP"], "olive_dark", "olive_light", "olive_shadow")
    flecks(draw, UV["BODY_TOP"], [
        (3, 2, "olive_mid"), (10, 3, "olive_shadow"), (5, 9, "olive_mid"),
        (11, 11, "olive_shadow"), (2, 12, "olive_mid"), (8, 6, "olive_light"),
    ])
    fill(draw, UV["BODY_BOTTOM"], "olive_shadow")
    x0, y0, x1, y1 = UV["BASE_SIDE"]
    fill(draw, UV["BASE_SIDE"], "graphite_mid")
    draw.line((x0, y0, x1 - 1, y0), fill=COLORS["graphite_light"])
    draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=COLORS["graphite_black"])
    for x in (2, 5, 9, 13):
        pixel(draw, x0 + x, y1 - 1, "graphite_mid")
    hard_border(draw, UV["BASE_TOP"], "graphite_mid", "graphite_light", "graphite_black")
    flecks(draw, UV["BASE_TOP"], [
        (2, 3, "graphite_light"), (12, 2, "graphite_black"), (5, 7, "graphite_black"),
        (10, 10, "graphite_light"), (3, 13, "graphite_black"), (13, 12, "graphite_mid"),
    ])
    fill(draw, UV["BASE_BOTTOM"], "graphite_black")
    x0, y0, x1, y1 = UV["SHOULDER_SIDE"]
    fill(draw, UV["SHOULDER_SIDE"], "forest_mid")
    draw.line((x0, y0, x1 - 1, y0), fill=COLORS["forest_light"])
    draw.line((x0, y1 - 1, x1 - 1, y1 - 1), fill=COLORS["forest_dark"])
    hard_border(draw, UV["SHOULDER_TOP"], "forest_mid", "forest_light", "forest_dark")
    flecks(draw, UV["SHOULDER_TOP"], [
        (2, 2, "forest_light"), (8, 3, "forest_dark"), (4, 8, "forest_dark"),
        (9, 9, "forest_light"), (2, 10, "forest_mid"),
    ])
    fill(draw, UV["SHOULDER_BOTTOM"], "forest_dark")
    outlined_bevel(draw, UV["CHANNEL_TOP"], "graphite_mid", "graphite_light", "graphite_black")
    flecks(draw, UV["CHANNEL_TOP"], [
        (2, 2, "graphite_black"), (7, 2, "graphite_light"),
        (3, 7, "graphite_light"), (7, 7, "graphite_black"),
    ])
    fill(draw, UV["CHANNEL_SIDE"], "graphite_black")
    paint_emitter(draw, active)
    fill(draw, UV["EMITTER_SIDE"], "graphite_mid")
    hard_border(draw, UV["FRAME_TOP"], "olive_dark", "olive_mid", "olive_shadow")
    fill(draw, UV["FRAME_LONG_SIDE"], "olive_dark")
    fill(draw, UV["FRAME_SHORT_SIDE"], "olive_shadow")
    fill(draw, UV["FRAME_BOTTOM"], "olive_shadow")
    hard_border(draw, UV["CAP_TOP"], "graphite_mid", "graphite_light", "graphite_black")
    hard_border(draw, UV["CAP_SIDE"], "graphite_mid", "graphite_light", "graphite_black")
    fill(draw, UV["CAP_BOTTOM"], "graphite_black")
    return image


def uv16(name: str) -> list[float]:
    return [value / 4 for value in UV[name]]


def face(uv_name: str, *, rotation: int = 0, cullface: str | None = None) -> dict:
    result = {"uv": uv16(uv_name), "texture": "#all"}
    if rotation:
        result["rotation"] = rotation
    if cullface:
        result["cullface"] = cullface
    return result


def faces_for(kind: str) -> dict:
    if kind == "base":
        return {
            "down": face("BASE_BOTTOM", cullface="down"), "up": face("BASE_TOP"),
            "north": face("BASE_SIDE"), "south": face("BASE_SIDE"),
            "west": face("BASE_SIDE"), "east": face("BASE_SIDE"),
        }
    if kind == "body":
        return {
            "down": face("BODY_BOTTOM"), "up": face("BODY_TOP"),
            "north": face("BODY_SIDE"), "south": face("BODY_SIDE"),
            "west": face("BODY_SIDE"), "east": face("BODY_SIDE"),
        }
    if kind == "shoulder":
        return {
            "down": face("SHOULDER_BOTTOM"), "up": face("SHOULDER_TOP"),
            "north": face("SHOULDER_SIDE"), "south": face("SHOULDER_SIDE"),
            "west": face("SHOULDER_SIDE"), "east": face("SHOULDER_SIDE"),
        }
    if kind == "channel":
        return {
            "down": face("CHANNEL_TOP"), "up": face("CHANNEL_TOP"),
            "north": face("CHANNEL_SIDE"), "south": face("CHANNEL_SIDE"),
            "west": face("CHANNEL_SIDE"), "east": face("CHANNEL_SIDE"),
        }
    if kind == "emitter":
        return {
            "down": face("EMITTER_TOP"), "up": face("EMITTER_TOP"),
            "north": face("EMITTER_SIDE"), "south": face("EMITTER_SIDE"),
            "west": face("EMITTER_SIDE"), "east": face("EMITTER_SIDE"),
        }
    if kind in ("frame_ns", "frame_we"):
        rotation = 90 if kind == "frame_we" else 0
        return {
            "down": face("FRAME_BOTTOM", rotation=rotation),
            "up": face("FRAME_TOP", rotation=rotation),
            "north": face("FRAME_LONG_SIDE" if kind == "frame_ns" else "FRAME_SHORT_SIDE"),
            "south": face("FRAME_LONG_SIDE" if kind == "frame_ns" else "FRAME_SHORT_SIDE"),
            "west": face("FRAME_SHORT_SIDE" if kind == "frame_ns" else "FRAME_LONG_SIDE"),
            "east": face("FRAME_SHORT_SIDE" if kind == "frame_ns" else "FRAME_LONG_SIDE"),
        }
    if kind == "cap":
        return {
            "down": face("CAP_BOTTOM"), "up": face("CAP_TOP"),
            "north": face("CAP_SIDE"), "south": face("CAP_SIDE"),
            "west": face("CAP_SIDE"), "east": face("CAP_SIDE"),
        }
    raise ValueError(kind)


def java_element(name: str, start: list[int], end: list[int], kind: str) -> dict:
    return {"name": name, "from": start, "to": end, "faces": faces_for(kind)}


def build_java_model() -> dict:
    return {
        "credit": "Generated from the approved tactical signal beacon Blockbench guide",
        "ambientocclusion": True,
        "gui_light": "side",
        "textures": {
            "all": "grenadier:block/tactical_signal_beacon_olive",
            "particle": "#all",
        },
        "elements": [java_element(*cube) for cube in CUBES],
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
            "ground": {"translation": [0, 3, 0], "scale": [0.35, 0.35, 0.35]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.6, 0.6, 0.6]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.38, 0.38, 0.38]},
            "thirdperson_lefthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.38, 0.38, 0.38]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
        },
    }


def bb_face(uv_name: str, *, rotation: int = 0) -> dict:
    return {"uv": uv16(uv_name), "texture": 0, "rotation": rotation}


def stable_uuid(name: str) -> str:
    return str(uuid.uuid5(uuid.NAMESPACE_URL, f"grenadier:tactical_signal_beacon/{name}"))


def build_bbmodel() -> dict:
    element_ids: dict[str, str] = {name: stable_uuid(f"cube/{name}") for name, *_ in CUBES}
    elements = []
    for name, start, end, kind in CUBES:
        model_faces = faces_for(kind)
        faces = {}
        for direction, model_face in model_faces.items():
            faces[direction] = {
                # Java block JSON expresses UVs in the canonical 0..16 model space,
                # while a .bbmodel with a 64x64 texture stores UVs in texture pixels.
                "uv": [coordinate * 4 for coordinate in model_face["uv"]],
                "texture": 0,
                "rotation": model_face.get("rotation", 0),
            }
        elements.append({
            "name": name,
            "box_uv": False,
            "rescale": False,
            "locked": False,
            "light_emission": 0,
            "render_order": "default",
            "allow_mirror_modeling": True,
            "from": start,
            "to": end,
            "autouv": 0,
            "color": 0,
            "origin": [8, 8, 8],
            "faces": faces,
            "type": "cube",
            "uuid": element_ids[name],
        })

    body_names = {"base_plinth", "main_casing", "upper_shoulder"}
    emitter_names = {name for name, *_ in CUBES} - body_names

    def group(name: str, children: list, origin: list[float] | None = None) -> dict:
        return {
            "name": name,
            "origin": origin or [8, 8, 8],
            "color": 0,
            "uuid": stable_uuid(f"group/{name}"),
            "export": True,
            "isOpen": True,
            "locked": False,
            "visibility": True,
            "autouv": 0,
            "children": children,
        }

    body = group("body", [element_ids[name] for name, *_ in CUBES if name in body_names])
    emitter = group("emitter", [element_ids[name] for name, *_ in CUBES if name in emitter_names])
    effect_children = [group(name, [], coords) for name, coords in ANCHORS.items()]
    effects = group("effect_anchors", effect_children)
    root = group("root", [body, emitter, effects])

    return {
        "meta": {
            "format_version": "4.10",
            "model_format": "java_block",
            "box_uv": False,
        },
        "name": "tactical_signal_beacon_olive",
        "model_identifier": "grenadier:tactical_signal_beacon",
        "visible_box": [1, 1, 0],
        "variable_placeholders": "beam_anchor=[8,14,8]; beam_bounds=[5,14,5]-[11,+inf,11]",
        "resolution": {"width": 64, "height": 64},
        "elements": elements,
        "outliner": [root],
        "textures": [{
            "path": str(INACTIVE_TEXTURE),
            "name": INACTIVE_TEXTURE.name,
            "folder": "block",
            "namespace": "armsrace",
            "id": "0",
            "particle": True,
            "render_mode": "default",
            "visible": True,
            "mode": "bitmap",
            "saved": True,
            "uuid": stable_uuid("texture/inactive"),
            "relative_path": "assets/grenadier/textures/block/tactical_signal_beacon_olive.png",
        }],
    }


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def render_preview(path: Path, texture: Image.Image, title: str) -> None:
    # Deterministic, nearest-neighbour isometric-style proof sheet. This is a geometry/UV
    # review aid, not an in-game render.
    scale = 10
    canvas = Image.new("RGBA", (720, 620), "#D8D6CE")
    draw = ImageDraw.Draw(canvas)
    origin_x, origin_y = 360, 470

    def project(point: tuple[float, float, float]) -> tuple[int, int]:
        x, y, z = point
        return (
            round(origin_x + (x - z) * scale * 1.55),
            round(origin_y - y * scale * 1.55 + (x + z) * scale * 0.52),
        )

    color_by_kind = {
        "base": COLORS["graphite_mid"],
        "body": COLORS["olive_mid"],
        "shoulder": COLORS["forest_mid"],
        "channel": COLORS["graphite_black"],
        "emitter": COLORS["cyan_mid"] if title.endswith("Active") else COLORS["forest_dark"],
        "frame_ns": COLORS["olive_dark"],
        "frame_we": COLORS["olive_dark"],
        "cap": COLORS["graphite_mid"],
    }

    for _, start, end, kind in sorted(CUBES, key=lambda c: (c[1][1], c[1][0] + c[1][2])):
        x0, y0, z0 = start
        x1, y1, z1 = end
        top = [project(p) for p in ((x0, y1, z0), (x1, y1, z0), (x1, y1, z1), (x0, y1, z1))]
        left = [project(p) for p in ((x0, y0, z0), (x0, y1, z0), (x0, y1, z1), (x0, y0, z1))]
        right = [project(p) for p in ((x0, y0, z1), (x0, y1, z1), (x1, y1, z1), (x1, y0, z1))]
        base = color_by_kind[kind]
        draw.polygon(left, fill=base, outline=COLORS["graphite_black"])
        draw.polygon(right, fill=COLORS["olive_shadow"] if "frame" in kind or kind == "body" else base, outline=COLORS["graphite_black"])
        draw.polygon(top, fill=COLORS["olive_light"] if kind in {"body", "frame_ns", "frame_we"} else base, outline=COLORS["graphite_black"])

    draw.text((24, 20), title, fill=COLORS["graphite_black"])
    draw.text((24, 45), "13 axis-aligned cuboids | 16x15x16 | emitter 8x8 | beam 6x6 centered", fill=COLORS["graphite_mid"])
    atlas = texture.resize((256, 256), Image.Resampling.NEAREST)
    canvas.alpha_composite(atlas, (440, 330))
    draw.rectangle((439, 329, 696, 586), outline=COLORS["graphite_black"], width=2)
    path.parent.mkdir(parents=True, exist_ok=True)
    canvas.convert("RGB").save(path, optimize=False)


def render_top_preview(path: Path, active: bool) -> None:
    scale = 28
    margin = 48
    canvas = Image.new("RGB", (16 * scale + margin * 2, 16 * scale + margin * 2), "#D8D6CE")
    draw = ImageDraw.Draw(canvas)
    visible = sorted(CUBES, key=lambda cube: cube[2][1])
    color_by_kind = {
        "base": COLORS["graphite_mid"], "body": COLORS["olive_mid"],
        "shoulder": COLORS["forest_mid"], "channel": COLORS["graphite_black"],
        "emitter": COLORS["cyan_mid"] if active else COLORS["forest_dark"],
        "frame_ns": COLORS["olive_dark"], "frame_we": COLORS["olive_dark"],
        "cap": COLORS["graphite_mid"],
    }
    for _, start, end, kind in visible:
        x0, _, z0 = start
        x1, _, z1 = end
        box = (
            margin + x0 * scale,
            margin + z0 * scale,
            margin + x1 * scale - 1,
            margin + z1 * scale - 1,
        )
        draw.rectangle(box, fill=color_by_kind[kind], outline=COLORS["graphite_black"], width=2)
    # Approved runtime beam footprint: 6 x 6, centered at X/Z = 8.
    beam = (margin + 5 * scale, margin + 5 * scale, margin + 11 * scale, margin + 11 * scale)
    draw.rectangle(beam, outline=COLORS["cyan_light"], width=3)
    draw.line((margin + 8 * scale, margin + 4.7 * scale, margin + 8 * scale, margin + 11.3 * scale), fill=COLORS["emitter_white"], width=1)
    draw.line((margin + 4.7 * scale, margin + 8 * scale, margin + 11.3 * scale, margin + 8 * scale), fill=COLORS["emitter_white"], width=1)
    draw.text((margin, 16), "Top orthographic | emitter 8x8 | runtime beam footprint 6x6 centered", fill=COLORS["graphite_black"])
    path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(path, optimize=False)


def validate(inactive: Image.Image, active: Image.Image, model: dict) -> None:
    assert inactive.size == (64, 64) and active.size == (64, 64)
    assert inactive.mode == "RGBA" and active.mode == "RGBA"
    changed = []
    for y in range(64):
        for x in range(64):
            if inactive.getpixel((x, y)) != active.getpixel((x, y)):
                changed.append((x, y))
    emitter = UV["EMITTER_TOP"]
    assert changed, "Active texture must differ from inactive texture"
    assert all(emitter[0] <= x < emitter[2] and emitter[1] <= y < emitter[3] for x, y in changed)
    assert len(model["elements"]) == 13
    names = {element["name"] for element in model["elements"]}
    assert len(names) == 13
    for element in model["elements"]:
        assert all(0 <= value <= 16 and float(value).is_integer() for value in element["from"] + element["to"])
        assert all(a < b for a, b in zip(element["from"], element["to"]))
        assert set(element["faces"]) == {"down", "up", "north", "south", "west", "east"}
    emitter_element = next(element for element in model["elements"] if element["name"] == "emitter_surface")
    assert emitter_element["from"] == [4, 13, 4] and emitter_element["to"] == [12, 14, 12]


def main() -> None:
    for directory in (TEXTURE_DIR, BLOCK_MODEL_DIR, ITEM_MODEL_DIR, SOURCE_DIR, PREVIEW_DIR):
        directory.mkdir(parents=True, exist_ok=True)

    inactive = build_texture(active=False)
    active = build_texture(active=True)

    if "--textures-only" in sys.argv[1:]:
        inactive.save(INACTIVE_TEXTURE, optimize=False)
        active.save(ACTIVE_TEXTURE, optimize=False)
        changed = [
            (x, y)
            for y in range(64)
            for x in range(64)
            if inactive.getpixel((x, y)) != active.getpixel((x, y))
        ]
        emitter = UV["EMITTER_TOP"]
        assert changed
        assert all(emitter[0] <= x < emitter[2] and emitter[1] <= y < emitter[3] for x, y in changed)
        print(f"Regenerated PNG textures only: {INACTIVE_TEXTURE} and {ACTIVE_TEXTURE}")
        return

    java_model = build_java_model()
    validate(inactive, active, java_model)

    inactive.save(INACTIVE_TEXTURE, optimize=False)
    active.save(ACTIVE_TEXTURE, optimize=False)
    write_json(BLOCK_MODEL_DIR / "tactical_signal_beacon_base.json", java_model)
    write_json(BLOCK_MODEL_DIR / "tactical_signal_beacon.json", {
        "parent": "grenadier:block/tactical_signal_beacon_base"
    })
    write_json(BLOCK_MODEL_DIR / "tactical_signal_beacon_active.json", {
        "parent": "grenadier:block/tactical_signal_beacon_base",
        "textures": {
            "all": "grenadier:block/tactical_signal_beacon_olive_active",
            "particle": "#all"
        }
    })
    write_json(ITEM_MODEL_DIR / "tactical_signal_beacon.json", {
        "parent": "grenadier:block/tactical_signal_beacon"
    })
    write_json(SOURCE_DIR / "tactical_signal_beacon_olive.bbmodel", build_bbmodel())
    write_json(SOURCE_DIR / "tactical_signal_beacon_effect_anchors.json", {
        "beam_bounds": {"from": [5, 14, 5], "to": [11, "infinite", 11]},
        "anchors": ANCHORS,
        "notes": [
            "Runtime-only metadata; it is not exported as static geometry.",
            "Dye changes beam, smoke, and dynamic emitter overlay only; casing stays olive."
        ]
    })
    render_preview(PREVIEW_DIR / "tactical_signal_beacon_inactive_preview.png", inactive, "Tactical Signal Beacon - Inactive")
    render_preview(PREVIEW_DIR / "tactical_signal_beacon_active_preview.png", active, "Tactical Signal Beacon - Active")
    render_top_preview(PREVIEW_DIR / "tactical_signal_beacon_top_preview.png", active=False)

    print(f"Generated {INACTIVE_TEXTURE}")
    print(f"Generated {ACTIVE_TEXTURE}")
    print(f"Generated {SOURCE_DIR / 'tactical_signal_beacon_olive.bbmodel'}")
    print("Validated: 13 cubes, integer 0..16 bounds, 64x64 RGBA, active delta confined to emitter UV")


if __name__ == "__main__":
    main()

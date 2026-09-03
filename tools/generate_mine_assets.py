from __future__ import annotations

import json
import shutil
from copy import deepcopy
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/grenadier"
DATA = ROOT / "src/main/resources/data/grenadier"

UV = {
    "olive": [0, 0, 4, 4], "dark": [4, 0, 8, 4], "yellow": [8, 0, 12, 4],
    "red": [12, 0, 16, 4], "metal": [0, 4, 4, 8], "pressure": [4, 4, 8, 8],
    "claymore_front": [8, 4, 12, 8],
}

AP_UV = {
    "body_top": [0, 0, 8, 8], "body_side": [0, 8, 8, 10], "body_bottom": [0, 10, 8, 12],
    "plate_top": [8, 0, 13, 5], "plate_side": [8, 5, 13, 6.5], "cap": [13, 0, 15, 2],
    "buried_top": [8, 8, 14, 12], "buried_side": [8, 12, 14, 13.5], "metal": [14, 8, 16, 10],
}

CLAYMORE_UV = {
    "front": [0, 0, 8, 6], "back": [8, 0, 16, 6],
    "shoulder_front": [0, 6, 4, 10], "side": [4, 6, 7, 10],
    "leg_dark": [8, 6, 10, 12], "leg_metal": [10, 6, 12, 12],
    "rail": [12, 6, 16, 8], "socket": [12, 8, 14, 10], "warning": [0, 10, 6, 11.5],
    "bottom": [6, 10, 8, 12],
}

THERMITE_UV = {
    "base_top": [0, 0, 8, 8], "base_side": [8, 0, 12, 4], "base_bottom": [8, 4, 12, 8],
    "core_top": [0, 8, 6, 14], "core_side": [6, 8, 10, 11],
    "charge_top": [12, 0, 16, 4], "charge_side": [12, 4, 16, 7],
    "igniter_top": [10, 8, 12, 10], "igniter_side": [10, 10, 12, 12],
}

def cell(draw, cx, cy, hi, mid, low, pattern=None):
    x, y = cx * 16, cy * 16
    draw.rectangle((x, y, x + 15, y + 15), fill=mid)
    draw.line((x, y, x + 15, y), fill=hi)
    draw.line((x, y, x, y + 15), fill=hi)
    draw.line((x, y + 15, x + 15, y + 15), fill=low)
    draw.line((x + 15, y, x + 15, y + 15), fill=low)
    if pattern: pattern(draw, x, y, hi, low)

def pressure_pattern(draw, x, y, hi, low):
    draw.rectangle((x + 4, y + 4, x + 11, y + 11), outline=low)
    draw.line((x + 5, y + 5, x + 10, y + 5), fill=hi)

def claymore_pattern(draw, x, y, hi, low):
    # Chunky warning panel: readable as a marked front face at inventory scale,
    # without pretending that tiny geometry can carry literal lettering.
    warning = "#d6b94a"
    ink = "#20251a"
    draw.rectangle((x + 2, y + 3, x + 13, y + 12), outline=low)
    draw.line((x + 4, y + 5, x + 11, y + 5), fill=warning, width=2)
    draw.line((x + 4, y + 9, x + 6, y + 9), fill=warning, width=2)
    draw.line((x + 9, y + 9, x + 11, y + 9), fill=warning, width=2)
    draw.point((x + 3, y + 5), fill=ink)
    draw.point((x + 12, y + 5), fill=ink)

def make_texture():
    image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    cell(draw, 0, 0, "#788553", "#4c5a32", "#29331e")
    cell(draw, 1, 0, "#515752", "#292e2b", "#151917")
    cell(draw, 2, 0, "#e6c15a", "#b88b24", "#6f5014")
    cell(draw, 3, 0, "#d84f40", "#9f2d26", "#5d1918")
    cell(draw, 0, 1, "#c8c5b7", "#88877c", "#4e504b")
    cell(draw, 1, 1, "#81905d", "#57663d", "#303923", pressure_pattern)
    cell(draw, 2, 1, "#7f8c57", "#526039", "#28321f", claymore_pattern)
    out = ASSETS / "textures/block/mine_materials.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    image.save(out)

    make_ap_texture()
    make_claymore_texture()
    make_thermite_texture()

def uv_box(uv):
    return tuple(round(value * 4) for value in uv)

def paint_panel(draw, uv, hi, mid, low, seam=None):
    x1, y1, x2, y2 = uv_box(uv)
    x2 -= 1; y2 -= 1
    draw.rectangle((x1, y1, x2, y2), fill=mid)
    draw.line((x1, y1, x2, y1), fill=hi)
    draw.line((x1, y1, x1, y2), fill=hi)
    draw.line((x1, y2, x2, y2), fill=low)
    draw.line((x2, y1, x2, y2), fill=low)
    if seam and x2 - x1 > 5 and y2 - y1 > 5:
        draw.rectangle((x1 + 2, y1 + 2, x2 - 2, y2 - 2), outline=seam)
    return x1, y1, x2, y2

def paint_flat(draw, uv, color, accent=None):
    """Paint a seam-free top surface for geometry assembled from strips."""
    x1, y1, x2, y2 = uv_box(uv)
    x2 -= 1; y2 -= 1
    draw.rectangle((x1, y1, x2, y2), fill=color)
    if accent and x2 - x1 > 8 and y2 - y1 > 8:
        draw.line((x1 + 3, y1 + 3, x2 - 4, y1 + 3), fill=accent)
    return x1, y1, x2, y2

def make_ap_texture():
    image = Image.new("RGBA", (64, 64), (35, 40, 29, 255))
    draw = ImageDraw.Draw(image)
    paint_flat(draw, AP_UV["body_top"], "#465333", "#56643e")
    side = paint_panel(draw, AP_UV["body_side"], "#647448", "#414e31", "#232a1d")
    sx1, sy1, sx2, sy2 = side
    draw.line((sx1 + 5, sy1 + 3, sx2 - 5, sy1 + 3), fill="#333e29")
    paint_panel(draw, AP_UV["body_bottom"], "#3c4434", "#252b22", "#171a16")
    paint_flat(draw, AP_UV["plate_top"], "#30372b", "#424a3a")
    paint_panel(draw, AP_UV["plate_side"], "#4d5541", "#30372b", "#191d18")
    paint_panel(draw, AP_UV["cap"], "#d0ad3e", "#95701e", "#503a12")
    buried = paint_flat(draw, AP_UV["buried_top"], "#3e4a31")
    bx1, by1, bx2, by2 = buried
    for px, py in ((bx1 + 3, by1 + 3), (bx2 - 4, by1 + 5), (bx1 + 7, by2 - 3), (bx2 - 8, by2 - 4)):
        draw.rectangle((px, py, px + 2, py + 1), fill="#62523a")
    paint_panel(draw, AP_UV["buried_side"], "#53623f", "#36432d", "#20271d")
    paint_panel(draw, AP_UV["metal"], "#b7b9ad", "#747970", "#3e433e")
    out = ASSETS / "textures/block/mines/anti_personnel_mine.png"
    out.parent.mkdir(parents=True, exist_ok=True); image.save(out)

def make_claymore_texture():
    image = Image.new("RGBA", (64, 64), (28, 33, 24, 255))
    draw = ImageDraw.Draw(image)
    front = paint_panel(draw, CLAYMORE_UV["front"], "#7c8958", "#526039", "#28311f", "#3a462d")
    x1, y1, x2, y2 = front
    draw.rectangle((x1 + 3, y1 + 3, x2 - 3, y2 - 3), outline="#5f6c43")
    warning = "#d3b548"; ink = "#242a1e"
    # One chunky T-shaped direction mark matches the approved concept and
    # remains readable without pseudo-lettering or dense dotted decoration.
    mid_x = (x1 + x2) // 2
    draw.rectangle((mid_x - 7, y1 + 8, mid_x + 7, y1 + 10), fill=warning)
    draw.rectangle((mid_x - 2, y1 + 10, mid_x + 2, y1 + 17), fill=warning)
    for px, py in ((x1 + 4, y1 + 4), (x2 - 4, y1 + 4), (x1 + 4, y2 - 4), (x2 - 4, y2 - 4)):
        draw.rectangle((px - 1, py - 1, px + 1, py + 1), fill="#20251b")
    back = paint_panel(draw, CLAYMORE_UV["back"], "#657249", "#404d32", "#222a1e", "#303a28")
    bx1, by1, bx2, by2 = back
    draw.rectangle((bx1 + 8, by1 + 6, bx2 - 8, by2 - 6), outline="#242c20")
    draw.line((bx1 + 10, by1 + 9, bx2 - 10, by1 + 9), fill="#596641")
    shoulder = paint_panel(draw, CLAYMORE_UV["shoulder_front"], "#738153", "#4a5836", "#27301f")
    qx1, qy1, qx2, qy2 = shoulder
    draw.line((qx1 + 3, qy1 + 4, qx2 - 3, qy2 - 4), fill="#65734a")
    paint_panel(draw, CLAYMORE_UV["side"], "#5e6b45", "#3a462f", "#20271c")
    paint_panel(draw, CLAYMORE_UV["leg_dark"], "#4d534c", "#292e2b", "#151917")
    metal = paint_panel(draw, CLAYMORE_UV["leg_metal"], "#a9aaa0", "#6d716b", "#3b403d")
    mx1, my1, mx2, my2 = metal
    draw.line((mx1 + 3, my1 + 2, mx1 + 3, my2 - 2), fill="#c2c2b6")
    paint_panel(draw, CLAYMORE_UV["rail"], "#555c50", "#30372e", "#181d19")
    paint_panel(draw, CLAYMORE_UV["socket"], "#c0c0b4", "#777b73", "#424641")
    warn = paint_panel(draw, CLAYMORE_UV["warning"], "#edd268", "#c49a33", "#76561b")
    wx1, wy1, wx2, wy2 = warn
    draw.line((wx1 + 3, wy1 + 3, wx2 - 3, wy1 + 3), fill="#2b3023")
    paint_panel(draw, CLAYMORE_UV["bottom"], "#3d4832", "#293225", "#181e17")
    out = ASSETS / "textures/block/mines/directional_fragmentation_mine.png"
    out.parent.mkdir(parents=True, exist_ok=True); image.save(out)

def make_thermite_texture():
    image = Image.new("RGBA", (64, 64), (31, 32, 28, 255))
    draw = ImageDraw.Draw(image)
    base = paint_panel(draw, THERMITE_UV["base_top"], "#464b43", "#292e29", "#151815", "#20251f")
    x1, y1, x2, y2 = base
    draw.rectangle((x1 + 5, y1 + 5, x2 - 5, y2 - 5), outline="#61685d")
    for px, py in ((x1 + 4, y1 + 4), (x2 - 4, y1 + 4), (x1 + 4, y2 - 4), (x2 - 4, y2 - 4)):
        draw.rectangle((px - 1, py - 1, px + 1, py + 1), fill="#111411")
    side = paint_panel(draw, THERMITE_UV["base_side"], "#41463f", "#252a27", "#131613")
    sx1, sy1, sx2, sy2 = side
    draw.line((sx1 + 5, sy1 + 5, sx2 - 5, sy1 + 5), fill="#6f5722")
    paint_panel(draw, THERMITE_UV["base_bottom"], "#343834", "#1e221f", "#111311")
    core = paint_panel(draw, THERMITE_UV["core_top"], "#626f45", "#414d31", "#222a1d", "#303a27")
    cx1, cy1, cx2, cy2 = core
    draw.rectangle((cx1 + 4, cy1 + 4, cx2 - 4, cy2 - 4), outline="#252c20")
    draw.line((cx1 + 6, cy1 + 7, cx2 - 6, cy2 - 7), fill="#68764a")
    paint_panel(draw, THERMITE_UV["core_side"], "#647346", "#414e31", "#242c20")
    charge = paint_panel(draw, THERMITE_UV["charge_top"], "#6b3833", "#432321", "#241313", "#34201e")
    rx1, ry1, rx2, ry2 = charge
    draw.rectangle((rx1 + 3, ry1 + 3, rx2 - 3, ry2 - 3), fill="#512824")
    draw.rectangle((rx1 + 7, ry1 + 7, rx2 - 7, ry2 - 7), fill="#63302a")
    paint_panel(draw, THERMITE_UV["charge_side"], "#54302b", "#35201d", "#1d1111")
    paint_panel(draw, THERMITE_UV["igniter_top"], "#b08a32", "#79591b", "#3f2e10")
    paint_panel(draw, THERMITE_UV["igniter_side"], "#927023", "#604616", "#32240d")
    out = ASSETS / "textures/block/mines/thermite_mine.png"
    out.parent.mkdir(parents=True, exist_ok=True); image.save(out)

def faces(material):
    return {side: {"uv": UV[material], "texture": "#materials"}
            for side in ("north", "east", "south", "west", "up", "down")}

def cube(name, start, end, material):
    return {"name": name, "from": start, "to": end, "faces": faces(material)}

def detailed_cube(name, start, end, materials, rotation=None):
    value = {"name": name, "from": start, "to": end,
             "faces": {side: {"uv": UV[materials.get(side, materials["all"])], "texture": "#materials"}
                       for side in ("north", "east", "south", "west", "up", "down")}}
    if rotation:
        value["rotation"] = rotation
    return value

def uv_cube(name, start, end, uv_regions, default_region, rotation=None, shade=True):
    value = {"name": name, "from": start, "to": end,
             "faces": {side: {"uv": uv_regions.get(side, default_region), "texture": "#skin"}
                       for side in ("north", "east", "south", "west", "up", "down")}}
    if rotation:
        value["rotation"] = rotation
    if not shade:
        value["shade"] = False
    return value

def model(elements, texture="grenadier:block/mine_materials"):
    return {"ambientocclusion": False,
            "textures": {"materials": texture, "skin": texture, "particle": texture},
            "elements": elements}

def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 1, 0], "scale": [0.9, 0.9, 0.9]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.8, 0.8, 0.8]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 1], "scale": [0.55, 0.55, 0.55]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [1.5, 3, 1], "scale": [0.62, 0.62, 0.62]},
}

ANTI_PERSONNEL_DISPLAY = {
    **DISPLAY,
    "gui": {"rotation": [34, 225, 0], "translation": [0, 9.75, 0], "scale": [2, 2, 2]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 1, 0], "scale": [0.55, 0.55, 0.55]},
}

THERMITE_DISPLAY = {
    **DISPLAY,
    "gui": {"rotation": [38, 225, 0], "translation": [0, 9.75, 0], "scale": [2, 2, 2]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 1, 0], "scale": [0.5, 0.5, 0.5]},
}

DIRECTIONAL_DISPLAY = {
    "gui": {"rotation": [18, 225, 0], "translation": [0, 7.25, 0], "scale": [1.7, 1.7, 1.7]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_righthand": {"rotation": [72, 45, 0], "translation": [0, 2, 1], "scale": [0.62, 0.62, 0.62]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [1.5, 3, 1], "scale": [0.68, 0.68, 0.68]},
}

def make_models():
    block_dir = ASSETS / "models/block"
    item_dir = ASSETS / "models/item"
    ap = [
        # Blockbench-approved compact M14-like stepped octagon.
        uv_cube("body_center", [5.25, 0, 6], [10.75, 0.52, 10],
                {"up": AP_UV["body_top"], "down": AP_UV["body_bottom"]}, AP_UV["body_side"], shade=False),
        uv_cube("body_north", [6, 0, 5.25], [10, 0.52, 6],
                {"up": AP_UV["body_top"], "down": AP_UV["body_bottom"]}, AP_UV["body_side"], shade=False),
        uv_cube("body_south", [6, 0, 10], [10, 0.52, 10.75],
                {"up": AP_UV["body_top"], "down": AP_UV["body_bottom"]}, AP_UV["body_side"], shade=False),
        uv_cube("cap_shadow_center", [6.2, 0.52, 6.72], [9.8, 0.66, 9.28],
                {"up": AP_UV["buried_top"]}, AP_UV["buried_side"], shade=False),
        uv_cube("cap_shadow_north", [6.72, 0.52, 6.45], [9.28, 0.66, 6.72],
                {"up": AP_UV["buried_top"]}, AP_UV["buried_side"], shade=False),
        uv_cube("cap_shadow_south", [6.72, 0.52, 9.28], [9.28, 0.66, 9.55],
                {"up": AP_UV["buried_top"]}, AP_UV["buried_side"], shade=False),
        uv_cube("pressure_cap_center", [6.4, 0.66, 6.88], [9.6, 1.02, 9.12],
                {"up": AP_UV["plate_top"]}, AP_UV["plate_side"], shade=False),
        uv_cube("pressure_cap_north", [6.88, 0.66, 6.65], [9.12, 1.02, 6.88],
                {"up": AP_UV["plate_top"]}, AP_UV["plate_side"], shade=False),
        uv_cube("pressure_cap_south", [6.88, 0.66, 9.12], [9.12, 1.02, 9.35],
                {"up": AP_UV["plate_top"]}, AP_UV["plate_side"], shade=False),
        uv_cube("safe_arm_notch", [7.55, 0.18, 5.08], [8.45, 0.45, 5.28], {}, AP_UV["cap"], shade=False),
    ]
    ap_buried = [
        uv_cube("buried_rim_north", [6, 0, 5.5], [10, 0.18, 6],
                {"up": AP_UV["buried_top"]}, AP_UV["buried_side"]),
        uv_cube("buried_rim_center", [5.5, 0, 6], [10.5, 0.18, 10],
                {"up": AP_UV["buried_top"]}, AP_UV["buried_side"]),
        uv_cube("buried_rim_south", [6, 0, 10], [10, 0.18, 10.5],
                {"up": AP_UV["buried_top"]}, AP_UV["buried_side"]),
        uv_cube("pressure_cap", [6.5, 0.18, 6.5], [9.5, 0.55, 9.5],
                {"up": AP_UV["plate_top"]}, AP_UV["plate_side"]),
        uv_cube("safe_arm_notch", [7.7, 0.18, 5.75], [8.3, 0.36, 6.15], {}, AP_UV["cap"]),
    ]
    directional = [
        uv_cube("charge_body", [5, 2.1, 7], [11, 6.2, 8.35],
                {"north": CLAYMORE_UV["back"], "south": CLAYMORE_UV["back"], "up": CLAYMORE_UV["rail"]}, CLAYMORE_UV["side"]),
        uv_cube("left_shoulder", [4.7, 2.5, 7.15], [5.35, 5.8, 8.15],
                {"north": CLAYMORE_UV["shoulder_front"]}, CLAYMORE_UV["side"]),
        uv_cube("right_shoulder", [10.65, 2.5, 7.15], [11.3, 5.8, 8.15],
                {"north": CLAYMORE_UV["shoulder_front"]}, CLAYMORE_UV["side"]),
        uv_cube("front_panel", [5.35, 2.45, 6.82], [10.65, 5.75, 7.02],
                {"north": CLAYMORE_UV["back"]}, CLAYMORE_UV["side"], shade=False),
        uv_cube("front_warning_bar", [6.65, 4.15, 6.62], [9.35, 4.55, 6.8], {}, CLAYMORE_UV["warning"], shade=False),
        uv_cube("front_warning_stem", [7.75, 3.35, 6.62], [8.25, 4.18, 6.8], {}, CLAYMORE_UV["warning"], shade=False),
        uv_cube("sight_left", [7.25, 6.2, 7.35], [7.55, 7.05, 7.75], {}, CLAYMORE_UV["leg_metal"]),
        uv_cube("sight_right", [8.45, 6.2, 7.35], [8.75, 7.05, 7.75], {}, CLAYMORE_UV["leg_metal"]),
        uv_cube("fuze_socket", [7.25, 6.75, 7.35], [8.75, 7.05, 7.75], {}, CLAYMORE_UV["socket"]),
        uv_cube("left_front_leg", [5.3, 0.05, 7.45], [5.8, 2.45, 7.95], {}, CLAYMORE_UV["leg_dark"],
                {"origin": [5.55, 2.25, 7.7], "axis": "z", "angle": -22.5}),
        uv_cube("left_rear_leg", [5.8, 0.15, 7.45], [6.3, 2.35, 7.95], {}, CLAYMORE_UV["leg_metal"],
                {"origin": [6.0, 2.15, 7.7], "axis": "z", "angle": 22.5}),
        uv_cube("right_front_leg", [10.2, 0.05, 7.45], [10.7, 2.45, 7.95], {}, CLAYMORE_UV["leg_dark"],
                {"origin": [10.45, 2.25, 7.7], "axis": "z", "angle": 22.5}),
        uv_cube("right_rear_leg", [9.7, 0.15, 7.45], [10.2, 2.35, 7.95], {}, CLAYMORE_UV["leg_metal"],
                {"origin": [10.0, 2.15, 7.7], "axis": "z", "angle": -22.5}),
    ]
    thermite = [
        uv_cube("heat_shield_backing", [5.35, 0, 5.35], [10.65, 0.34, 10.65],
                {"up": THERMITE_UV["base_top"], "down": THERMITE_UV["base_bottom"]}, THERMITE_UV["base_side"]),
        uv_cube("frame_north", [5.35, 0.34, 5.35], [10.65, 0.56, 6], {"up": THERMITE_UV["core_top"]}, THERMITE_UV["core_side"], shade=False),
        uv_cube("frame_south", [5.35, 0.34, 10], [10.65, 0.56, 10.65], {"up": THERMITE_UV["core_top"]}, THERMITE_UV["core_side"], shade=False),
        uv_cube("frame_west", [5.35, 0.34, 6], [6, 0.56, 10], {"up": THERMITE_UV["core_top"]}, THERMITE_UV["core_side"], shade=False),
        uv_cube("frame_east", [10, 0.34, 6], [10.65, 0.56, 10], {"up": THERMITE_UV["core_top"]}, THERMITE_UV["core_side"], shade=False),
        uv_cube("frame_cross_x", [7.72, 0.34, 6], [8.28, 0.56, 10], {"up": THERMITE_UV["core_top"]}, THERMITE_UV["core_side"], shade=False),
        uv_cube("frame_cross_z", [6, 0.34, 7.72], [10, 0.56, 8.28], {"up": THERMITE_UV["core_top"]}, THERMITE_UV["core_side"], shade=False),
        uv_cube("charge_nw", [6, 0.35, 6], [7.72, 0.5, 7.72],
                {"up": THERMITE_UV["charge_top"]}, THERMITE_UV["charge_side"]),
        uv_cube("charge_ne", [8.28, 0.35, 6], [10, 0.5, 7.72],
                {"up": THERMITE_UV["charge_top"]}, THERMITE_UV["charge_side"]),
        uv_cube("charge_sw", [6, 0.35, 8.28], [7.72, 0.5, 10],
                {"up": THERMITE_UV["charge_top"]}, THERMITE_UV["charge_side"]),
        uv_cube("charge_se", [8.28, 0.35, 8.28], [10, 0.5, 10],
                {"up": THERMITE_UV["charge_top"]}, THERMITE_UV["charge_side"]),
        uv_cube("igniter", [7.68, 0.56, 7.68], [8.32, 0.69, 8.32],
                {"up": THERMITE_UV["igniter_top"]}, THERMITE_UV["igniter_side"]),
    ]
    write_json(block_dir / "anti_personnel_mine.json",
               model(ap, "grenadier:block/mines/anti_personnel_mine"))
    write_json(block_dir / "anti_personnel_mine_buried.json",
               model(ap_buried, "grenadier:block/mines/anti_personnel_mine"))
    write_json(block_dir / "directional_fragmentation_mine.json",
               model(directional, "grenadier:block/mines/directional_fragmentation_mine"))
    write_json(block_dir / "thermite_mine.json",
               model(thermite, "grenadier:block/mines/thermite_mine"))
    ap_item_model = model(ap, "grenadier:block/mines/anti_personnel_mine")
    ap_item_model["display"] = ANTI_PERSONNEL_DISPLAY
    write_json(item_dir / "anti_personnel_mine.json", ap_item_model)
    thermite_item_model = model(thermite, "grenadier:block/mines/thermite_mine")
    thermite_item_model["display"] = THERMITE_DISPLAY
    write_json(item_dir / "thermite_mine.json", thermite_item_model)
    write_json(item_dir / "directional_fragmentation_mine.json",
               {"parent": "grenadier:block/directional_fragmentation_mine", "display": DIRECTIONAL_DISPLAY})

def make_blockstates():
    bs = ASSETS / "blockstates"
    write_json(bs / "anti_personnel_mine.json", {"variants": {
        "soft_ground=false": {"model": "grenadier:block/anti_personnel_mine"},
        "soft_ground=true": {"model": "grenadier:block/anti_personnel_mine_buried"}}})
    write_json(bs / "directional_fragmentation_mine.json", {"variants": {
        "facing=north": {"model": "grenadier:block/directional_fragmentation_mine"},
        "facing=east": {"model": "grenadier:block/directional_fragmentation_mine", "y": 90},
        "facing=south": {"model": "grenadier:block/directional_fragmentation_mine", "y": 180},
        "facing=west": {"model": "grenadier:block/directional_fragmentation_mine", "y": 270}}})
    write_json(bs / "thermite_mine.json", {"variants": {"": {"model": "grenadier:block/thermite_mine"}}})

def make_data():
    recipes = {
        "anti_personnel_mine": ([" I ", "GTG", " P "], {"I": "minecraft:iron_ingot", "G": "minecraft:gunpowder", "T": "minecraft:tripwire_hook", "P": "minecraft:stone_pressure_plate"}),
        "directional_fragmentation_mine": (["III", "GTG", " R "], {"I": "minecraft:iron_nugget", "G": "minecraft:gunpowder", "T": "minecraft:tripwire_hook", "R": "minecraft:redstone"}),
        "thermite_mine": (["RCR", "GTG", " I "], {"R": "minecraft:redstone", "C": "minecraft:fire_charge", "G": "minecraft:gunpowder", "T": "minecraft:tripwire_hook", "I": "minecraft:iron_ingot"}),
    }
    for name, (pattern, keys) in recipes.items():
        write_json(DATA / f"recipe/{name}.json", {"type": "minecraft:crafting_shaped", "category": "combat",
            "pattern": pattern, "key": {k: {"item": v} for k, v in keys.items()}, "result": {"id": f"grenadier:{name}", "count": 2}})
        write_json(DATA / f"loot_table/blocks/{name}.json", {"type": "minecraft:block", "pools": [{"rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"grenadier:{name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}], "random_sequence": f"grenadier:blocks/{name}"})
    write_json(DATA / "tags/entity_type/thermite_heavy_targets.json", {"replace": False, "values": [
        "minecraft:iron_golem", "minecraft:ravager", "minecraft:warden"]})

def update_languages():
    additions = {
        "en_us": {"block.grenadier.anti_personnel_mine": "Anti-Personnel Mine", "item.grenadier.anti_personnel_mine": "Anti-Personnel Mine",
                  "block.grenadier.directional_fragmentation_mine": "Directional Fragmentation Mine", "item.grenadier.directional_fragmentation_mine": "Directional Fragmentation Mine",
                  "block.grenadier.thermite_mine": "Thermite Mine", "item.grenadier.thermite_mine": "Thermite Mine"},
        "zh_cn": {"block.grenadier.anti_personnel_mine": "反步兵地雷", "item.grenadier.anti_personnel_mine": "反步兵地雷",
                  "block.grenadier.directional_fragmentation_mine": "定向破片地雷", "item.grenadier.directional_fragmentation_mine": "定向破片地雷",
                  "block.grenadier.thermite_mine": "热熔地雷", "item.grenadier.thermite_mine": "热熔地雷"},
        "zh_tw": {"block.grenadier.anti_personnel_mine": "反步兵地雷", "item.grenadier.anti_personnel_mine": "反步兵地雷",
                  "block.grenadier.directional_fragmentation_mine": "定向破片地雷", "item.grenadier.directional_fragmentation_mine": "定向破片地雷",
                  "block.grenadier.thermite_mine": "熱熔地雷", "item.grenadier.thermite_mine": "熱熔地雷"},
    }
    for lang, values in additions.items():
        path = ASSETS / f"lang/{lang}.json"
        data = json.loads(path.read_text(encoding="utf-8"))
        data.update(values)
        write_json(path, data)

def stabilize_subpixel_uvs(value):
    """Keep approved UV origins while preventing mipmapped sampling across sub-pixel spans."""
    for element in value.get("elements", []):
        for face in element.get("faces", {}).values():
            uv = face.get("uv")
            if not uv or len(uv) != 4:
                continue
            if 0.0 < abs(uv[2] - uv[0]) < 1.0:
                uv[2] = uv[0] + (1.0 if uv[2] >= uv[0] else -1.0)
            if 0.0 < abs(uv[3] - uv[1]) < 1.0:
                uv[3] = uv[1] + (1.0 if uv[3] >= uv[1] else -1.0)


def sync_blockbench_exports():
    """Install approved Blockbench exports and stabilize sub-pixel UV spans for Minecraft mipmaps."""
    export_root = ROOT / "tools/blockbench_exports/mines"
    texture_root = ASSETS / "textures/block/mines/blockbench"
    block_dir = ASSETS / "models/block"
    item_dir = ASSETS / "models/item"
    exports = {
        "anti_personnel_mine": ("ap", "anti_personnel_mine_review.json", ANTI_PERSONNEL_DISPLAY),
        "directional_fragmentation_mine": ("claymore", "claymore_review.json", DIRECTIONAL_DISPLAY),
        "thermite_mine": ("thermite", "thermite_mine_review.json", THERMITE_DISPLAY),
    }
    for target_name, (folder, model_name, display) in exports.items():
        source_dir = export_root / folder
        source_model = source_dir / model_name
        value = json.loads(source_model.read_text(encoding="utf-8"))
        value.pop("format_version", None)
        value.pop("credit", None)
        stabilize_subpixel_uvs(value)

        target_texture_dir = texture_root / folder
        target_texture_dir.mkdir(parents=True, exist_ok=True)
        for texture_file in source_dir.glob("*.png"):
            shutil.copyfile(texture_file, target_texture_dir / texture_file.name)

        for key, texture_name in list(value["textures"].items()):
            value["textures"][key] = f"grenadier:block/mines/blockbench/{folder}/{texture_name}"
        value["ambientocclusion"] = False
        write_json(block_dir / f"{target_name}.json", value)
        if target_name == "anti_personnel_mine":
            # Keep both placement states on the exact approved export.
            write_json(block_dir / "anti_personnel_mine_buried.json", value)

        item_value = deepcopy(value)
        blockbench_gui = deepcopy(value.get("display", {}).get("gui"))
        item_value["display"] = deepcopy(display)
        if not blockbench_gui:
            raise ValueError(f"Blockbench export {source_model} has no calibrated GUI display")
        item_value["display"]["gui"] = blockbench_gui
        write_json(item_dir / f"{target_name}.json", item_value)

def main():
    sync_blockbench_exports(); make_blockstates(); make_data(); update_languages()

if __name__ == "__main__": main()

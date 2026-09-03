from __future__ import annotations

import json
import math
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/grenadier"
OUT = ROOT / "build/previews/mine-assets.png"


def rotate(point, spec):
    if not spec:
        return point
    x, y, z = point
    ox, oy, oz = spec.get("origin", [8, 8, 8])
    angle = math.radians(spec.get("angle", 0))
    x -= ox; y -= oy; z -= oz
    c, s = math.cos(angle), math.sin(angle)
    axis = spec.get("axis")
    if axis == "x":
        y, z = y * c - z * s, y * s + z * c
    elif axis == "y":
        x, z = x * c + z * s, -x * s + z * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return x + ox, y + oy, z + oz


def project(point, scale, origin):
    x, y, z = point
    return (origin[0] + (x - z) * 0.866 * scale,
            origin[1] + (x + z) * 0.5 * scale - y * scale)


def face_vertices(start, end, side):
    x1, y1, z1 = start; x2, y2, z2 = end
    return {
        "up": [(x1, y2, z1), (x2, y2, z1), (x2, y2, z2), (x1, y2, z2)],
        "east": [(x2, y1, z1), (x2, y2, z1), (x2, y2, z2), (x2, y1, z2)],
        "south": [(x1, y1, z2), (x1, y2, z2), (x2, y2, z2), (x2, y1, z2)],
    }[side]


def load_model(path):
    model = json.loads(path.read_text(encoding="utf-8"))
    parent = model.get("parent")
    if parent and parent.startswith("grenadier:"):
        parent_path = ASSETS / "models" / (parent.split(":", 1)[1] + ".json")
        base = load_model(parent_path)
        base.update(model)
        return base
    return model


def texture_for(model):
    ref = model["textures"].get("skin") or model["textures"].get("materials")
    rel = ref.split(":", 1)[1]
    return Image.open(ASSETS / "textures" / (rel + ".png"))


def sampled_color(texture, uv, shade):
    x1, y1, x2, y2 = [round(v * texture.width / 16) for v in uv]
    crop = texture.crop((min(x1, x2), min(y1, y2), max(x1, x2), max(y1, y2))).convert("RGBA")
    pixels = [p for p in crop.getdata() if p[3] > 0]
    if not pixels:
        return (255, 0, 255, 255)
    rgb = tuple(sum(p[i] for p in pixels) // len(pixels) for i in range(3))
    return tuple(max(0, min(255, round(c * shade))) for c in rgb) + (255,)


def render_model(draw, model_path, box, ground=False):
    model = load_model(model_path)
    texture = texture_for(model)
    cx = (box[0] + box[2]) / 2
    cy = (box[1] + box[3]) / 2
    scale = min((box[2] - box[0]) / 29, (box[3] - box[1]) / 22)
    origin = (cx, cy - 2 * scale)

    if ground:
        grass = [(0, 0, 0), (16, 0, 0), (16, 0, 16), (0, 0, 16)]
        draw.polygon([project(p, scale, origin) for p in grass], fill="#567d36", outline="#26361f")
        for i in range(2, 16, 2):
            a = project((i, 0.01, 0), scale, origin); b = project((i, 0.01, 16), scale, origin)
            draw.line((a, b), fill="#648943")
            a = project((0, 0.01, i), scale, origin); b = project((16, 0.01, i), scale, origin)
            draw.line((a, b), fill="#456b2d")

    faces = []
    for element in model.get("elements", []):
        start, end = element["from"], element["to"]
        for side, shade in (("east", .78), ("south", .64), ("up", 1.0)):
            face = element.get("faces", {}).get(side)
            if not face:
                continue
            verts = [rotate(v, element.get("rotation")) for v in face_vertices(start, end, side)]
            depth = sum(v[0] + v[1] + v[2] for v in verts) / 4
            layer = 1 if side == "up" else 0
            height = sum(v[1] for v in verts) / 4
            faces.append((layer, height, depth, verts, sampled_color(texture, face["uv"], shade)))
    for _, _, _, verts, color in sorted(faces):
        pts = [project(v, scale, origin) for v in verts]
        draw.polygon(pts, fill=color, outline="#151914")


def main():
    canvas = Image.new("RGB", (1200, 720), "#d8d8d8")
    draw = ImageDraw.Draw(canvas)
    names = ["anti_personnel_mine", "directional_fragmentation_mine", "thermite_mine"]
    labels = ["ANTI-PERSONNEL", "DIRECTIONAL", "THERMITE"]
    for col, (name, label) in enumerate(zip(names, labels)):
        x1 = 20 + col * 393; x2 = x1 + 373
        draw.rounded_rectangle((x1, 20, x2, 700), radius=8, fill="#eeeeee", outline="#1b1b1b", width=4)
        draw.text((x1 + 14, 34), label, fill="#202020")
        draw.text((x1 + 14, 66), "WORLD MODEL / 16x16 BLOCK", fill="#555555")
        render_model(draw, ASSETS / f"models/block/{name}.json", (x1 + 16, 88, x2 - 16, 390), ground=True)
        draw.text((x1 + 14, 414), "DEDICATED INVENTORY GEOMETRY", fill="#555555")
        slot = (x1 + 80, 446, x2 - 80, 668)
        draw.rectangle(slot, fill="#8b8b8b", outline="#303030", width=5)
        draw.line((slot[0] + 5, slot[1] + 5, slot[2] - 5, slot[1] + 5), fill="#d9d9d9", width=4)
        draw.line((slot[0] + 5, slot[1] + 5, slot[0] + 5, slot[3] - 5), fill="#d9d9d9", width=4)
        render_model(draw, ASSETS / f"models/item/{name}.json", (slot[0] + 16, slot[1] + 14, slot[2] - 16, slot[3] - 12))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(OUT)
    print(OUT)


if __name__ == "__main__":
    main()

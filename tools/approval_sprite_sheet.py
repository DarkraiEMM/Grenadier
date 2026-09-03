from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


OUT = Path(__file__).resolve().parents[1] / "docs" / "approval" / "grenadier_item_language_v1.png"

P = {
    "ink": "#171A19",
    "graphite": "#252927",
    "graphite_hi": "#373C38",
    "olive_shadow": "#2D351F",
    "olive_dark": "#3E4828",
    "olive": "#4F5B32",
    "olive_hi": "#71804A",
    "forest": "#2C4030",
    "sand_shadow": "#7E7864",
    "sand": "#A39D83",
    "sand_hi": "#C0B89A",
    "amber_dark": "#9F5E0D",
    "amber": "#D18B24",
    "amber_hi": "#F0AD31",
    "cyan_dark": "#5A9495",
    "cyan": "#A9DDDA",
    "cyan_hi": "#DDFBF5",
}


def rect(d, box, color):
    d.rectangle(box, fill=P[color])


def px(d, points, color):
    for point in points:
        d.point(point, fill=P[color])


def smoke():
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    rect(d, (6, 1, 9, 2), "graphite")
    rect(d, (5, 2, 10, 3), "ink")
    rect(d, (4, 4, 11, 13), "ink")
    rect(d, (5, 4, 10, 12), "graphite")
    rect(d, (5, 5, 5, 11), "graphite_hi")
    rect(d, (5, 4, 10, 5), "olive_hi")
    rect(d, (5, 6, 10, 7), "olive")
    rect(d, (5, 12, 10, 13), "olive_dark")
    rect(d, (6, 8, 9, 9), "sand")
    px(d, [(6, 8)], "sand_hi")
    px(d, [(9, 9)], "sand_shadow")
    rect(d, (11, 3, 12, 4), "sand")
    rect(d, (12, 4, 13, 6), "graphite_hi")
    return im


def incendiary():
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    rect(d, (6, 1, 9, 2), "graphite")
    rect(d, (5, 2, 10, 4), "ink")
    rect(d, (4, 4, 11, 5), "graphite_hi")
    rect(d, (3, 6, 12, 12), "ink")
    rect(d, (4, 5, 11, 12), "olive")
    rect(d, (4, 5, 11, 6), "olive_hi")
    rect(d, (4, 11, 11, 12), "olive_shadow")
    rect(d, (3, 7, 12, 8), "graphite")
    rect(d, (5, 9, 10, 10), "olive_dark")
    rect(d, (7, 7, 8, 8), "amber")
    px(d, [(7, 7)], "amber_hi")
    rect(d, (5, 13, 10, 13), "graphite")
    rect(d, (11, 3, 12, 4), "sand")
    return im


def flashbang():
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    rect(d, (6, 1, 9, 2), "graphite")
    rect(d, (5, 2, 10, 4), "ink")
    rect(d, (5, 4, 10, 13), "ink")
    rect(d, (6, 4, 9, 12), "sand")
    rect(d, (6, 4, 6, 11), "sand_hi")
    rect(d, (9, 5, 9, 12), "sand_shadow")
    px(d, [(7, 6), (8, 6), (7, 8), (8, 8), (7, 10), (8, 10)], "graphite")
    rect(d, (5, 12, 10, 13), "graphite_hi")
    rect(d, (6, 13, 9, 14), "ink")
    rect(d, (11, 3, 12, 4), "sand")
    rect(d, (12, 4, 12, 6), "graphite_hi")
    return im


def frag():
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    rect(d, (7, 1, 9, 2), "graphite")
    rect(d, (6, 2, 10, 4), "ink")
    rect(d, (5, 4, 11, 5), "olive_dark")
    rect(d, (4, 5, 12, 11), "ink")
    rect(d, (5, 5, 11, 12), "olive")
    rect(d, (6, 4, 10, 12), "olive")
    rect(d, (6, 5, 9, 5), "olive_hi")
    rect(d, (5, 7, 11, 7), "olive_dark")
    rect(d, (5, 10, 11, 10), "olive_shadow")
    rect(d, (7, 5, 7, 11), "olive_dark")
    rect(d, (10, 5, 10, 11), "olive_shadow")
    rect(d, (6, 12, 10, 13), "ink")
    rect(d, (10, 2, 12, 3), "sand")
    rect(d, (11, 3, 12, 7), "graphite_hi")
    return im


def impact():
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    rect(d, (6, 3, 10, 4), "amber_dark")
    rect(d, (5, 4, 11, 6), "amber")
    rect(d, (6, 4, 10, 4), "amber_hi")
    rect(d, (4, 6, 12, 11), "ink")
    rect(d, (5, 6, 11, 10), "olive")
    rect(d, (5, 6, 11, 6), "olive_hi")
    rect(d, (5, 10, 11, 11), "olive_shadow")
    rect(d, (4, 8, 5, 10), "graphite_hi")
    rect(d, (6, 8, 9, 9), "forest")
    rect(d, (11, 5, 12, 6), "sand")
    rect(d, (12, 6, 12, 8), "graphite_hi")
    return im


def beacon():
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    rect(d, (3, 4, 12, 12), "ink")
    rect(d, (2, 12, 13, 14), "ink")
    rect(d, (3, 12, 12, 13), "graphite_hi")
    rect(d, (4, 5, 11, 11), "olive")
    rect(d, (4, 5, 11, 5), "olive_hi")
    rect(d, (4, 11, 11, 11), "olive_shadow")
    rect(d, (5, 7, 10, 10), "graphite")
    rect(d, (6, 8, 9, 9), "sand")
    px(d, [(7, 9), (8, 9)], "amber")
    rect(d, (4, 3, 11, 4), "forest")
    rect(d, (5, 2, 10, 3), "graphite")
    rect(d, (6, 1, 9, 2), "cyan_dark")
    rect(d, (7, 1, 8, 2), "cyan_hi")
    px(d, [(3, 3), (12, 3)], "graphite_hi")
    return im


def main():
    sprites = [smoke(), incendiary(), flashbang(), frag(), impact(), beacon()]
    labels = ["烟雾弹", "燃烧弹", "闪光弹", "破片手榴弹", "冲击手榴弹", "战术信号机"]
    scale = 12
    cell = 216
    top = 42
    sheet = Image.new("RGB", (cell * 6 + 28, 292), "#B8B8B8")
    draw = ImageDraw.Draw(sheet)
    font_path = Path("C:/Windows/Fonts/msyh.ttc")
    font = ImageFont.truetype(str(font_path), 18) if font_path.exists() else ImageFont.load_default()
    for i, (sprite, label) in enumerate(zip(sprites, labels)):
        x = 14 + i * cell
        draw.rectangle((x, 14, x + 196, 230), fill="#C6C6C6", outline="#373737", width=5)
        enlarged = sprite.resize((16 * scale, 16 * scale), Image.Resampling.NEAREST)
        sheet.paste(enlarged, (x + 2, top), enlarged)
        box = draw.textbbox((0, 0), label, font=font)
        tw = box[2] - box[0]
        draw.text((x + (196 - tw) / 2, 248), label, fill="#252927", font=font)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    print(OUT)


if __name__ == "__main__":
    main()

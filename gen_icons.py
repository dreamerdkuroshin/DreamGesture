# -*- coding: utf-8 -*-
import os
import sys
from PIL import Image, ImageDraw

LOGO = os.path.join(os.path.dirname(os.path.abspath(__file__)), "dreamgesture_logo.png")

def resize_icon(img, size):
    return img.resize((size, size), Image.LANCZOS)

def make_round(img, size):
    resized = resize_icon(img, size)
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
    result = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    result.paste(resized, (0, 0), mask)
    return result

def make_adaptive_fg(img, size):
    visible = int(size * 72 / 108)
    resized = resize_icon(img, visible)
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    offset = (size - visible) // 2
    canvas.paste(resized, (offset, offset))
    return canvas

def ensured(path):
    os.makedirs(path, exist_ok=True)
    return path

ADAPTIVE_XML = (
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
    '    <background android:drawable="@color/ic_launcher_background"/>\n'
    '    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>\n'
    '</adaptive-icon>\n'
)

BG_COLOR_XML = (
    '<?xml version="1.0" encoding="utf-8"?>\n'
    '<resources>\n'
    '    <color name="ic_launcher_background">#0A0A1A</color>\n'
    '</resources>\n'
)

MIPMAP = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

ADAPTIVE = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}


def generate_android(img, res_dir, label):
    print(f"--- {label} ---")
    for d, s in MIPMAP.items():
        p = ensured(os.path.join(res_dir, d))
        resize_icon(img, s).save(os.path.join(p, "ic_launcher.png"), "PNG")
        make_round(img, s).save(os.path.join(p, "ic_launcher_round.png"), "PNG")
        print(f"  {d}: {s}x{s} (launcher + round)")

    for d, s in ADAPTIVE.items():
        p = ensured(os.path.join(res_dir, d))
        make_adaptive_fg(img, s).save(os.path.join(p, "ic_launcher_foreground.png"), "PNG")
        print(f"  {d}: {s}x{s} (adaptive foreground)")

    xml_dir = ensured(os.path.join(res_dir, "mipmap-anydpi-v26"))
    with open(os.path.join(xml_dir, "ic_launcher.xml"), "w") as f:
        f.write(ADAPTIVE_XML)
    with open(os.path.join(xml_dir, "ic_launcher_round.xml"), "w") as f:
        f.write(ADAPTIVE_XML)

    vals = ensured(os.path.join(res_dir, "values"))
    with open(os.path.join(vals, "ic_launcher_background.xml"), "w") as f:
        f.write(BG_COLOR_XML)

    print("  adaptive icon XML created")


def generate_desktop(img, icons_dir):
    print("--- Desktop (Windows / macOS / Linux) ---")
    ensured(icons_dir)

    # PNG icons
    for name, s in [("32x32.png", 32), ("128x128.png", 128), ("128x128@2x.png", 256)]:
        resize_icon(img, s).save(os.path.join(icons_dir, name), "PNG")
        print(f"  {name}: {s}x{s}")

    # Linux 512x512
    resize_icon(img, 512).save(os.path.join(icons_dir, "icon.png"), "PNG")
    print("  icon.png: 512x512 (Linux)")

    # Windows ICO
    ico_sizes = [16, 24, 32, 48, 64, 128, 256]
    ico_imgs = [resize_icon(img, s) for s in ico_sizes]
    ico_imgs[0].save(
        os.path.join(icons_dir, "icon.ico"),
        format="ICO",
        sizes=[(s, s) for s in ico_sizes],
        append_images=ico_imgs[1:],
    )
    print(f"  icon.ico: multi-size 16-256px (Windows)")

    # macOS ICNS
    try:
        icns_sizes = [16, 32, 64, 128, 256, 512]
        icns_imgs = [resize_icon(img, s) for s in icns_sizes]
        icns_imgs[0].save(
            os.path.join(icons_dir, "icon.icns"),
            format="ICNS",
            append_images=icns_imgs[1:],
        )
        print("  icon.icns: multi-size 16-512px (macOS)")
    except Exception as e:
        print(f"  icon.icns skipped ({e}), icon.png serves as macOS fallback")


def main():
    if not os.path.exists(LOGO):
        print(f"ERROR: Logo not found at {LOGO}")
        return 1

    img = Image.open(LOGO).convert("RGBA")
    print(f"Loaded logo: {img.size[0]}x{img.size[1]}")
    print()

    base = os.path.dirname(os.path.abspath(__file__))

    # 1. Android - GestureShare
    generate_android(
        img,
        os.path.join(base, "GestureShare", "app", "src", "main", "res"),
        "Android (GestureShare)",
    )
    print()

    # 2. Android - GestureShare-unified
    generate_android(
        img,
        os.path.join(base, "GestureShare-unified", "android", "app", "src", "main", "res"),
        "Android (GestureShare-unified)",
    )
    print()

    # 3. Desktop - Tauri (Windows/macOS/Linux)
    generate_desktop(
        img,
        os.path.join(base, "GestureShare-unified", "desktop", "tauri-app", "src-tauri", "icons"),
    )
    print()

    # 4. Master logo copies in docs
    print("--- Master Logo Copies ---")
    for docs in [
        os.path.join(base, "GestureShare", "docs"),
        os.path.join(base, "GestureShare-unified", "docs"),
    ]:
        ensured(docs)
        resize_icon(img, 512).save(os.path.join(docs, "logo.png"), "PNG")
        print(f"  {docs}")

    print()
    print("ALL ICONS GENERATED SUCCESSFULLY!")
    return 0


if __name__ == "__main__":
    sys.exit(main())

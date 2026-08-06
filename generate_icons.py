"""
Generate all platform icons from the DreamGesture logo.
Places icons in correct locations for:
  - Android (GestureShare): mipmap-mdpi through mipmap-xxxhdpi
  - Android (GestureShare-unified): same structure
  - Desktop (Tauri): 32x32, 128x128, 128x128@2x, icon.ico, icon.icns
"""

import os
import struct
import io
from PIL import Image

LOGO_PATH = os.path.join(os.path.dirname(__file__), "dreamgesture_logo.png")

# ── Android icon sizes ──────────────────────────────────────────────
ANDROID_MIPMAP_SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# Adaptive icon foreground is 108dp at each density
ANDROID_ADAPTIVE_SIZES = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}

# ── Tauri/Desktop icon files ────────────────────────────────────────
TAURI_PNG_SIZES = {
    "32x32.png": 32,
    "128x128.png": 128,
    "128x128@2x.png": 256,
}


def ensure_dir(path):
    os.makedirs(path, exist_ok=True)


def resize_icon(img, size):
    """Resize image to a square icon with high-quality downsampling."""
    return img.resize((size, size), Image.LANCZOS)


def create_round_mask(size):
    """Create a circular mask for round icons."""
    from PIL import ImageDraw
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size - 1, size - 1), fill=255)
    return mask


def make_round_icon(img, size):
    """Create a circular icon."""
    resized = resize_icon(img, size)
    mask = create_round_mask(size)
    result = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    result.paste(resized, (0, 0), mask)
    return result


def make_adaptive_foreground(img, size):
    """
    Create adaptive icon foreground.
    The foreground is 108dp but the visible area is 72dp (inner 66.67%).
    We center the logo within the larger canvas.
    """
    visible_size = int(size * 72 / 108)
    resized = resize_icon(img, visible_size)
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    offset = (size - visible_size) // 2
    canvas.paste(resized, (offset, offset))
    return canvas


def create_ico(img, output_path):
    """Create a Windows .ico file with multiple sizes."""
    sizes = [16, 24, 32, 48, 64, 128, 256]
    icon_images = []
    for s in sizes:
        resized = resize_icon(img, s)
        icon_images.append(resized)
    icon_images[0].save(
        output_path,
        format="ICO",
        sizes=[(s, s) for s in sizes],
        append_images=icon_images[1:],
    )


def create_icns(img, output_path):
    """
    Create a macOS .icns file.
    Pillow supports saving ICNS directly.
    """
    sizes = [16, 32, 64, 128, 256, 512, 1024]
    icon_images = []
    for s in sizes:
        resized = resize_icon(img, s)
        icon_images.append(resized)
    # Pillow's ICNS writer uses the first image and append_images
    icon_images[0].save(
        output_path,
        format="ICNS",
        append_images=icon_images[1:],
    )


def generate_android_icons(img, res_dir):
    """Generate all Android mipmap icons."""
    for mipmap_dir, size in ANDROID_MIPMAP_SIZES.items():
        dir_path = os.path.join(res_dir, mipmap_dir)
        ensure_dir(dir_path)

        # Standard launcher icon
        icon = resize_icon(img, size)
        icon.save(os.path.join(dir_path, "ic_launcher.png"), "PNG")

        # Round launcher icon
        round_icon = make_round_icon(img, size)
        round_icon.save(os.path.join(dir_path, "ic_launcher_round.png"), "PNG")

        print(f"  ✓ {mipmap_dir}: {size}x{size} (launcher + round)")

    # Adaptive icon foreground
    for mipmap_dir, size in ANDROID_ADAPTIVE_SIZES.items():
        dir_path = os.path.join(res_dir, mipmap_dir)
        ensure_dir(dir_path)
        fg = make_adaptive_foreground(img, size)
        fg.save(os.path.join(dir_path, "ic_launcher_foreground.png"), "PNG")
        print(f"  ✓ {mipmap_dir}: {size}x{size} (adaptive foreground)")


def generate_adaptive_icon_xml(res_dir):
    """Create the adaptive icon XML files."""
    # ic_launcher.xml
    xml_dir = os.path.join(res_dir, "mipmap-anydpi-v26")
    ensure_dir(xml_dir)

    launcher_xml = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
'''
    with open(os.path.join(xml_dir, "ic_launcher.xml"), "w") as f:
        f.write(launcher_xml)

    round_xml = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
'''
    with open(os.path.join(xml_dir, "ic_launcher_round.xml"), "w") as f:
        f.write(round_xml)

    # Background color resource
    values_dir = os.path.join(res_dir, "values")
    ensure_dir(values_dir)

    colors_file = os.path.join(values_dir, "ic_launcher_background.xml")
    if not os.path.exists(colors_file):
        colors_xml = '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#0A0A1A</color>
</resources>
'''
        with open(colors_file, "w") as f:
            f.write(colors_xml)

    print(f"  ✓ Adaptive icon XML (mipmap-anydpi-v26)")


def generate_tauri_icons(img, icons_dir):
    """Generate all Tauri/Desktop icons (Windows, macOS, Linux)."""
    ensure_dir(icons_dir)

    # PNG icons for Linux & general use
    for filename, size in TAURI_PNG_SIZES.items():
        icon = resize_icon(img, size)
        icon.save(os.path.join(icons_dir, filename), "PNG")
        print(f"  ✓ {filename}: {size}x{size}")

    # Windows .ico
    ico_path = os.path.join(icons_dir, "icon.ico")
    create_ico(img, ico_path)
    print(f"  ✓ icon.ico (Windows: 16-256px multi-size)")

    # macOS .icns
    icns_path = os.path.join(icons_dir, "icon.icns")
    try:
        create_icns(img, icns_path)
        print(f"  ✓ icon.icns (macOS: 16-1024px)")
    except Exception as e:
        print(f"  ⚠ icon.icns skipped (Pillow ICNS support limited): {e}")
        # Fallback: save a 512x512 PNG as icon.png for macOS
        icon512 = resize_icon(img, 512)
        icon512.save(os.path.join(icons_dir, "icon.png"), "PNG")
        print(f"  ✓ icon.png (macOS fallback: 512x512)")

    # Extra: 512x512 icon for Linux AppImage/DEB
    icon512 = resize_icon(img, 512)
    icon512.save(os.path.join(icons_dir, "icon.png"), "PNG")
    print(f"  ✓ icon.png (Linux: 512x512)")


def main():
    if not os.path.exists(LOGO_PATH):
        print(f"ERROR: Logo not found at {LOGO_PATH}")
        print("Please place the DreamGesture logo as 'dreamgesture_logo.png' in the project root.")
        return

    img = Image.open(LOGO_PATH).convert("RGBA")
    print(f"Loaded logo: {img.size[0]}x{img.size[1]}")
    print()

    # ── 1. Android (GestureShare) ──
    print("═══ Android (GestureShare) ═══")
    android_res = os.path.join(
        os.path.dirname(__file__),
        "GestureShare", "app", "src", "main", "res"
    )
    generate_android_icons(img, android_res)
    generate_adaptive_icon_xml(android_res)
    print()

    # ── 2. Android (GestureShare-unified) ──
    print("═══ Android (GestureShare-unified) ═══")
    unified_res = os.path.join(
        os.path.dirname(__file__),
        "GestureShare-unified", "android", "app", "src", "main", "res"
    )
    generate_android_icons(img, unified_res)
    generate_adaptive_icon_xml(unified_res)
    print()

    # ── 3. Desktop (Tauri — Windows/macOS/Linux) ──
    print("═══ Desktop (Windows / macOS / Linux) ═══")
    tauri_icons = os.path.join(
        os.path.dirname(__file__),
        "GestureShare-unified", "desktop", "tauri-app", "src-tauri", "icons"
    )
    generate_tauri_icons(img, tauri_icons)
    print()

    # ── 4. Store the master logo in docs for reference ──
    print("═══ Master Logo Copies ═══")
    for docs_dir in [
        os.path.join(os.path.dirname(__file__), "GestureShare", "docs"),
        os.path.join(os.path.dirname(__file__), "GestureShare-unified", "docs"),
    ]:
        ensure_dir(docs_dir)
        master_path = os.path.join(docs_dir, "logo.png")
        icon512 = resize_icon(img, 512)
        icon512.save(master_path, "PNG")
        print(f"  ✓ {master_path}")

    print()
    print("✅ All icons generated successfully!")


if __name__ == "__main__":
    main()

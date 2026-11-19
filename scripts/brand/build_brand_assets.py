"""Build Hisab Sathi app and store icons from the Canva master logo.

Source: scripts/brand/hisab-sathi-logo.png, exported from the Canva design
"Hisab Sathi app icon logo" (cream bank building with रु on near-black).

The cream mark is cut out of the dark background (anti-aliased), then recoloured and placed
for each target, so the logo looks identical everywhere and stays a clean silhouette when
Android tints it (notification icon, themed icons, tinted in-app logo).

Writes:
  res/drawable-nodpi/ic_launcher_foreground.png   adaptive launcher foreground (+ monochrome)
  res/drawable-nodpi/ic_logo_{original,anarchy,zenith}_fg.png   alternate launcher icons
  res/drawable-nodpi/hisabsathi.png                tinted in-app logo / notification icon
  res/drawable-nodpi/hisabsathi_{variant}.png      alternate-icon previews (Appearance screen)
  res/drawable-nodpi/lead_developer.webp           About-screen image
  res/mipmap-*/ic_launcher{,_round,_foreground}.webp   legacy launcher fallbacks
  app/src/main/ic_launcher-playstore.png, fastlane .../images/icon.png

Requires ImageMagick 7 (`magick`). Usage from the repo root:
    python3 scripts/brand/build_brand_assets.py
"""
import os, subprocess, tempfile

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
SRC = os.path.join(ROOT, "scripts/brand/hisab-sathi-logo.png")
RES = os.path.join(ROOT, "app/src/main/res")
TMP = tempfile.mkdtemp(prefix="hisab-brand-")

CREAM, DARK = "#FFFFF0", "#1A1A1F"
VARIANTS = {  # name: (mark colour, background colour) — backgrounds match values/colors.xml
    "original": ("#FFFFF0", "#1F1F1F"),
    "anarchy": ("#DC143C", "#F5EEE5"),   # Nepal flag crimson on sand
    "zenith": ("#1F1F1F", "#E6E6E6"),
}

def magick(*args):
    subprocess.run(["magick", *args], check=True)

# 1. White mark with anti-aliased alpha: dark background -> transparent, cream -> opaque.
ALPHA = os.path.join(TMP, "alpha.png")
MARK = os.path.join(TMP, "mark.png")
magick(SRC, "-colorspace", "gray", "-level", "12%,88%", ALPHA)
w, h = map(int, subprocess.run(["magick", "identify", "-format", "%w %h", SRC],
                               capture_output=True, text=True, check=True).stdout.split())
magick("-size", f"{w}x{h}", "xc:white", ALPHA, "-alpha", "off", "-compose", "CopyOpacity",
       "-composite", "-trim", "+repage", MARK)
mw, mh = map(int, subprocess.run(["magick", "identify", "-format", "%w %h", MARK],
                                 capture_output=True, text=True, check=True).stdout.split())
half_diag = ((mw / 2) ** 2 + (mh / 2) ** 2) ** 0.5

def place(out, canvas, radius_frac, colour, bg=None, shape="square"):
    """Mark fitted so its bounding circle has radius radius_frac * canvas/2, centred."""
    scale = (radius_frac * canvas / 2) / half_diag
    tw, th = round(mw * scale), round(mh * scale)
    coloured = os.path.join(TMP, "c.png")
    magick(MARK, "-fill", colour, "-colorize", "100", "-resize", f"{tw}x{th}", coloured)
    if bg is None:
        base = ["-size", f"{canvas}x{canvas}", "xc:none"]
    elif shape == "circle":
        base = ["-size", f"{canvas}x{canvas}", "xc:none", "-fill", bg, "-draw",
                f"circle {canvas/2},{canvas/2} {canvas/2},0"]
    elif shape == "rounded":
        r = round(canvas * 0.215)
        base = ["-size", f"{canvas}x{canvas}", "xc:none", "-fill", bg, "-draw",
                f"roundrectangle 0,0 {canvas-1},{canvas-1} {r},{r}"]
    else:
        base = ["-size", f"{canvas}x{canvas}", f"xc:{bg}"]
    # Strip timestamps/metadata so re-running the script only changes files whose pixels changed.
    extra = ["-strip", "-define", "png:exclude-chunks=date,time"]
    if out.endswith(".webp"):
        extra += ["-define", "webp:lossless=true"]
    magick(*base, coloured, "-gravity", "center", "-compose", "over", "-composite", *extra, out)

def remove(path):
    if os.path.exists(path):
        os.remove(path)

NODPI = os.path.join(RES, "drawable-nodpi")
FG = 0.61   # adaptive icons: keep the mark inside the 66dp safe zone of the 108dp canvas

# Launcher foreground (also used as the monochrome layer for themed icons).
place(os.path.join(NODPI, "ic_launcher_foreground.png"), 432, FG, CREAM)
remove(os.path.join(RES, "drawable/ic_launcher_foreground.xml"))

# Tinted in-app logo / notification small icon: white silhouette, nearly full canvas.
place(os.path.join(NODPI, "hisabsathi.png"), 512, 0.97, "#FFFFFF")
remove(os.path.join(RES, "drawable/hisabsathi.xml"))

# Outline version of the mark (used with the filled one in the scrolling logo pattern
# behind onboarding and the About header): a ring traced around the silhouette's edge.
OUTLINE = os.path.join(TMP, "outline.png")
magick(MARK, "-alpha", "extract", "-morphology", "EdgeOut", "Disk:10", "-threshold", "50%",
       "-blur", "0x1", OUTLINE)
magick("-size", f"{mw}x{mh}", "xc:white", OUTLINE, "-alpha", "off", "-compose", "CopyOpacity",
       "-composite", OUTLINE)
_mark_backup = MARK
MARK = OUTLINE
place(os.path.join(NODPI, "hisabsathi_outline.png"), 512, 0.97, "#FFFFFF")
MARK = _mark_backup

for name, (fg, bg) in VARIANTS.items():
    place(os.path.join(NODPI, f"ic_logo_{name}_fg.png"), 432, FG, fg)
    remove(os.path.join(RES, f"drawable/ic_logo_{name}_fg.xml"))
    place(os.path.join(NODPI, f"hisabsathi_{name}.png"), 512, 0.80, fg)
    remove(os.path.join(RES, f"drawable/hisabsathi_{name}.xml"))

# Legacy mipmaps (minSdk 26 uses the adaptive icons, these are fallbacks).
for d, px in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
    mm = os.path.join(RES, f"mipmap-{d}")
    place(os.path.join(mm, "ic_launcher.webp"), px, 0.80, CREAM, DARK, "rounded")
    place(os.path.join(mm, "ic_launcher_round.webp"), px, 0.78, CREAM, DARK, "circle")
    place(os.path.join(mm, "ic_launcher_foreground.webp"), round(px * 108 / 48), FG, CREAM)

# Store icons and the About-screen image.
place(os.path.join(ROOT, "app/src/main/ic_launcher-playstore.png"), 512, 0.80, CREAM, DARK)
place(os.path.join(ROOT, "fastlane/metadata/android/en-US/images/icon.png"), 512, 0.76, CREAM, DARK, "rounded")
place(os.path.join(NODPI, "lead_developer.webp"), 512, 0.72, CREAM, DARK, "circle")
print("brand assets written")

#!/usr/bin/env python3
"""Generate the Upgrade Table GUI texture for Lootbound RPG."""

from PIL import Image, ImageDraw

# Texture and GUI dimensions
TEXTURE_WIDTH, TEXTURE_HEIGHT = 256, 256
GUI_WIDTH, GUI_HEIGHT = 176, 210

img = Image.new('RGBA', (TEXTURE_WIDTH, TEXTURE_HEIGHT), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# GUI background
draw.rectangle([0, 0, GUI_WIDTH-1, GUI_HEIGHT-1], fill=(198, 198, 198, 255))
draw.rectangle([0, 0, GUI_WIDTH-1, GUI_HEIGHT-1], outline=(0, 0, 0, 255))

# 3D borders
draw.line([(1, 1), (GUI_WIDTH-2, 1)], fill=(255, 255, 255, 255))
draw.line([(1, 1), (1, GUI_HEIGHT-2)], fill=(255, 255, 255, 255))
draw.line([(GUI_WIDTH-2, 1), (GUI_WIDTH-2, GUI_HEIGHT-2)], fill=(85, 85, 85, 255))
draw.line([(1, GUI_HEIGHT-2), (GUI_WIDTH-2, GUI_HEIGHT-2)], fill=(85, 85, 85, 255))

def draw_slot(x, y, size=18):
    """Draw a Minecraft-style item slot."""
    draw.line([(x, y), (x + size - 1, y)], fill=(55, 55, 55, 255))
    draw.line([(x, y), (x, y + size - 1)], fill=(55, 55, 55, 255))
    draw.line([(x + 1, y + size - 1), (x + size - 1, y + size - 1)], fill=(255, 255, 255, 255))
    draw.line([(x + size - 1, y + 1), (x + size - 1, y + size - 1)], fill=(255, 255, 255, 255))
    draw.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=(139, 139, 139, 255))

def draw_separator(y):
    """Draw a horizontal separator line with 3D effect."""
    draw.line([(7, y), (GUI_WIDTH - 8, y)], fill=(85, 85, 85, 255))
    draw.line([(7, y + 1), (GUI_WIDTH - 8, y + 1)], fill=(255, 255, 255, 255))

def draw_arrow(x, y):
    """Draw a right-pointing arrow."""
    color = (85, 85, 85, 255)
    draw.rectangle([x, y + 3, x + 14, y + 5], fill=color)
    for i in range(5):
        draw.line([(x + 15, y + 4 - i), (x + 15, y + 4 + i)], fill=color)
        if i < 4:
            draw.line([(x + 16 + i, y + 4 - (4-i)), (x + 16 + i, y + 4 + (4-i))], fill=color)

# === SEPARATORS ===
# Below title
draw_separator(18)

# Below slots area (before info)
draw_separator(55)

# Below status area (before inventory)
draw_separator(108)

# === UPGRADE AREA ===
# Equipment slot at (27, 35)
draw_slot(26, 34)

# Arrow
draw_arrow(48, 36)

# Stone slot at (76, 35)
draw_slot(75, 34)

# === PLAYER INVENTORY at y=128 ===
for row in range(3):
    for col in range(9):
        x = 7 + col * 18
        y = 127 + row * 18
        draw_slot(x, y)

# === HOTBAR at y=186 ===
for col in range(9):
    x = 7 + col * 18
    y = 185
    draw_slot(x, y)

# Save
output_path = r"C:\Users\Philippe\Documents\Programming\MinecraftMods\LootboundRPG\src\main\resources\assets\lootbound_rpg\textures\gui\upgrade_table.png"
img.save(output_path)
print(f"Saved GUI texture ({GUI_WIDTH}x{GUI_HEIGHT}) to {output_path}")

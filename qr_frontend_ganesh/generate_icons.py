import os
from PIL import Image, ImageDraw

img_path = r'C:\Users\Samir\.gemini\antigravity\brain\26c3e349-4c77-43e6-9391-5659b87d4df4\media__1775763155178.jpg'
try:
    img = Image.open(img_path).convert('RGB')
except Exception as e:
    print(f'Error opening image: {e}')
    exit(1)

width, height = img.size

# Crop to remove watermark (trim bottom and right slightly)
# Let's crop 10% from bottom and right
crop_w = int(width * 0.95)
crop_h = int(height * 0.95)
img = img.crop((0, 0, crop_w, crop_h))

# Make it a perfect square after crop
min_dim = min(img.size)
diff_x = img.size[0] - min_dim
diff_y = img.size[1] - min_dim
img = img.crop((diff_x // 2, diff_y // 2, img.size[0] - diff_x // 2, img.size[1] - diff_y // 2))

res_dir = r'c:\QR-Tracking-System\qr_frontend_ganesh\app\src\main\res'

def make_round(im):
    mask = Image.new('L', im.size, 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0) + im.size, fill=255)
    result = im.copy()
    result.putalpha(mask)
    return result

sizes = {
    'mdpi': {'legacy': 48, 'adaptive': 108},
    'hdpi': {'legacy': 72, 'adaptive': 162},
    'xhdpi': {'legacy': 96, 'adaptive': 216},
    'xxhdpi': {'legacy': 144, 'adaptive': 324},
    'xxxhdpi': {'legacy': 192, 'adaptive': 432}
}

for density, dims in sizes.items():
    d_dir = os.path.join(res_dir, f'mipmap-{density}')
    os.makedirs(d_dir, exist_ok=True)
    
    # Adaptive Foreground
    fg_size = dims['adaptive']
    fg = img.resize((fg_size, fg_size), Image.Resampling.LANCZOS)
    fg.save(os.path.join(d_dir, 'ic_launcher_foreground.png'))
    
    # Legacy Square
    leg_size = dims['legacy']
    leg = img.resize((leg_size, leg_size), Image.Resampling.LANCZOS)
    leg.save(os.path.join(d_dir, 'ic_launcher.png'))
    
    # Legacy Round
    leg_round = make_round(leg)
    leg_round.save(os.path.join(d_dir, 'ic_launcher_round.png'))

print('Generated all mipmap files.')

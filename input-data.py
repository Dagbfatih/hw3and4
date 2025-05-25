import json
import os

# GeoJSON dosyasını oku
with open("export.geojson", "r") as f:
    data = json.load(f)

# Çıktı klasörünü hazırla
os.makedirs("inputData", exist_ok=True)

count = 1

# Her bina için .txt dosyası oluştur
for feature in data["features"]:
    geometry = feature["geometry"]
    if geometry["type"] == "Polygon":
        coords = geometry["coordinates"][0]  # dış halka

        with open(f"inputData/{count}.txt", "w") as out:
            for x, y in coords:
                out.write(f"{x} {y}\n")
        count += 1

    if count > 100:
        break

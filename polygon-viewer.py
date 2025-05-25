import matplotlib.pyplot as plt
import os
import random

def plot_polygon_from_file(filepath):
    with open(filepath, "r") as f:
        lines = f.readlines()
    
    x = []
    y = []
    
    for line in lines:
        parts = line.strip().split()
        if len(parts) == 2:
            x.append(float(parts[0]))
            y.append(float(parts[1]))
    
    # çokgeni kapatmak için ilk noktayı sona ekle
    x.append(x[0])
    y.append(y[0])
    
    plt.figure(figsize=(5,5))
    plt.plot(x, y, marker='o')
    plt.title(f"Polygon: {os.path.basename(filepath)}")
    plt.axis('equal')
    plt.grid(True)
    plt.show()

# inputData klasöründeki tüm .txt dosyalarını al
files = [f for f in os.listdir("inputData") if f.endswith(".txt")]
random_files = random.sample(files, 3)

# Rastgele 3 dosyayı çiz
for file in random_files:
    plot_polygon_from_file(os.path.join("inputData", file))

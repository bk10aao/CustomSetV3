import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
from matplotlib.lines import Line2D
import os
import numpy as np
import csv


def load_csv(filepath):
    """Load semicolon-separated CSV file with robust float/int parsing."""
    data = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f, delimiter=';')
        for row in reader:
            size = int(float(row['Size']))
            data[size] = {
                k: float(v) for k, v in row.items() if k != 'Size'
            }
    return data


def format_y_axis(value, pos):
    """Format y-axis labels cleanly with commas or decimals for sub-nanosecond values."""
    if value == 0:
        return '0'
    elif value < 10:
        return f'{value:.1f}'
    return f'{int(value):,}'


FILES = {
    'V2': 'CustomSetV2_performance.csv',
    'V3': 'CustomSetV3_performance.csv',
}

OUTPUT_DIR = "V2_V3"
os.makedirs(OUTPUT_DIR, exist_ok=True)

COLORS = {
    'V2': '#9B6EF3',    # Neon Purple for V2
    'V3': '#FF5C5c',    # Neon Coral / Red for V3
    'grid': '#252525',
}

# Comprehensive dictionary mapping all 22 CSV columns
OPERATIONS = {
    'add(E)': 'add',
    'addAll(Collection)': 'addAll',
    'clear()': 'clear',
    'clone()': 'clone',
    'constructor()': 'constructor',
    'constructor(int)': 'constructorCapacity',
    'constructor(int, double)': 'constructorCapacityLoadFactor',
    'constructor(Collection)': 'constructorCollection',
    'contains(Object)': 'contains',
    'containsAll(Collection)': 'containsAll',
    'equals(Object)': 'equals',
    'hashCode()': 'hashCode',
    'isEmpty()': 'isEmpty',
    'iterator()': 'iterator',
    'spliterator()': 'spliterator',
    'remove(Object)': 'remove',
    'removeAll(Collection)': 'removeAll',
    'retainAll(Collection)': 'retainAll',
    'size()': 'size',
    'toArray()': 'toArray',
    'toArray(T[])': 'toArrayT',
    'toString()': 'toString',
}

datasets = {}
canonical_sizes = set()
for name, path in FILES.items():
    data = load_csv(path)
    datasets[name] = data
    canonical_sizes.update(data.keys())
canonical_sizes = sorted(list(canonical_sizes))

x_positions = list(range(len(canonical_sizes)))

# 22 operations total -> 6 rows x 4 columns grid = 24 subplots with transparent background
fig, axes = plt.subplots(6, 4, figsize=(22, 28), dpi=150)
fig.patch.set_alpha(0)
axes_flat = axes.flatten()

legend_elements = [
    Line2D([0], [0], marker='o', color='none', markerfacecolor=COLORS['V2'], markeredgecolor=COLORS['V2'],
           markeredgewidth=1.5, markersize=8, label='V2', linestyle='none'),
    Line2D([0], [0], marker='o', color='none', markerfacecolor=COLORS['V3'], markeredgecolor=COLORS['V3'],
           markeredgewidth=1.5, markersize=8, label='V3', linestyle='none')
]

for idx, (op_key, op_label) in enumerate(OPERATIONS.items()):
    ax = axes_flat[idx]
    ax.set_facecolor('none')

    for name, data in datasets.items():
        values = [
            data[s][op_key] if s in data and op_key in data[s] else np.nan
            for s in canonical_sizes
        ]
        color = COLORS[name]
        ax.plot(x_positions, values, color=color, linewidth=1.5, zorder=2)
        ax.scatter(x_positions, values, color=color, s=28, marker='o', edgecolors=color, linewidths=1.2, zorder=3)

    ax.grid(True, color=COLORS['grid'], linewidth=0.7, linestyle='-', zorder=0)
    ax.set_axisbelow(True)
    ax.set_xticks(x_positions)
    ax.set_xticklabels([f'{s:,}' for s in canonical_sizes], color='white', fontsize=9, rotation=25, ha='right')
    ax.tick_params(axis='x', colors='white', length=0, pad=6)
    ax.set_xlim(-0.4, len(canonical_sizes) - 0.6)

    ax.yaxis.set_major_formatter(mticker.FuncFormatter(format_y_axis))
    ax.tick_params(axis='y', colors='white', length=0, pad=6)
    for label in ax.get_yticklabels():
        label.set_color('white')
        label.set_fontsize(9)

    for spine in ax.spines.values():
        spine.set_visible(False)

    ax.set_title(op_key, color='white', fontsize=12, fontweight='bold', pad=10)

# Hide any unused subplots in the grid
for idx in range(len(OPERATIONS), len(axes_flat)):
    axes_flat[idx].set_visible(False)

leg = fig.legend(
    handles=legend_elements,
    loc='upper center',
    bbox_to_anchor=(0.5, 0.99),
    ncol=2,
    frameon=False,
    fontsize=14,
    handletextpad=0.8,
    columnspacing=3.0
)
for text in leg.get_texts():
    text.set_color('white')
    text.set_fontsize(14)

plt.tight_layout(rect=[0, 0, 1, 0.97])
output_path = os.path.join(OUTPUT_DIR, 'grid.png')
fig.savefig(
    output_path,
    dpi=150,
    transparent=True,
    bbox_inches='tight',
    facecolor='none',
    edgecolor='none'
)
plt.close(fig)
print("V2 vs V3 grid chart created with transparent background successfully!")
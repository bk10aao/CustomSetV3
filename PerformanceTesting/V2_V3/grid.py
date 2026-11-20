import csv
import os
import matplotlib
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
from matplotlib.lines import Line2D
import numpy as np

matplotlib.use('Agg')

BASE_DIR = os.path.dirname(os.path.abspath(__file__))


def load_csv(filepath):
    """Load semicolon-separated CSV file with robust float/int parsing."""
    data = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f, delimiter=';')
        for row in reader:
            raw_size = (row.get('Size') or '').strip()
            if not raw_size:
                continue
            size = int(float(raw_size))
            parsed = {}
            for k, v in row.items():
                if k is None or k == 'Size':
                    continue
                val = (v or '').strip()
                parsed[k] = float(val) if val else np.nan
            data[size] = parsed
    return data


def format_y_axis(value, pos):
    """Format y-axis labels cleanly with commas or decimals for sub-nanosecond values."""
    if value == 0:
        return '0'
    elif value < 10:
        return f'{value:.1f}'
    return f'{int(value):,}'


FILES = {
    'V2': os.path.join(BASE_DIR, 'CustomSetV2_performance.csv'),
    'V3': os.path.join(BASE_DIR, 'CustomSetV3_performance.csv'),
}

OUTPUT_DIR = BASE_DIR
os.makedirs(OUTPUT_DIR, exist_ok=True)

COLORS = {
    'V2': '#9B6EF3',    # Neon Purple for V2
    'V3': '#FF5C5c',    # Neon Coral / Red for V3
    'grid': '#252525',
}

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
canonical_sizes = sorted(canonical_sizes)

x_positions = list(range(len(canonical_sizes)))

fig, axes = plt.subplots(6, 4, figsize=(22, 28), dpi=150)
fig.patch.set_alpha(0)
axes_flat = axes.flatten()

legend_elements = [
    Line2D([0], [0], marker='o', color='none', markerfacecolor=COLORS['V2'],
           markeredgecolor=COLORS['V2'], markeredgewidth=1.5, markersize=8,
           label='V2', linestyle='none'),
    Line2D([0], [0], marker='o', color='none', markerfacecolor=COLORS['V3'],
           markeredgecolor=COLORS['V3'], markeredgewidth=1.5, markersize=8,
           label='V3', linestyle='none'),
]

for idx, (op_key, op_label) in enumerate(OPERATIONS.items()):
    ax = axes_flat[idx]
    ax.set_facecolor('none')

    for name, data in datasets.items():
        values = [
            data[s].get(op_key, np.nan) if s in data else np.nan
            for s in canonical_sizes
        ]
        color = COLORS[name]
        ax.plot(x_positions, values, color=color, linewidth=1.5, zorder=2)
        ax.scatter(x_positions, values, color=color, s=28, marker='o',
                   edgecolors=color, linewidths=1.2, zorder=3)

    ax.grid(True, color=COLORS['grid'], linewidth=0.7, linestyle='-', zorder=0)
    ax.set_axisbelow(True)
    ax.set_xticks(x_positions)
    ax.set_xticklabels([f'{s:,}' for s in canonical_sizes], color='white',
                       fontsize=9, rotation=25, ha='right')
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
    columnspacing=3.0,
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
    edgecolor='none',
)
plt.close(fig)

file_size = os.path.getsize(output_path) if os.path.exists(output_path) else 0
if file_size == 0:
    raise RuntimeError(f"Generated image at {output_path} is 0 bytes.")

print(f"V2 vs V3 grid chart created successfully ({file_size:,} bytes) at {output_path}")
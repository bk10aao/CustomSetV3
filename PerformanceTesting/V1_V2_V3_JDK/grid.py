#!/usr/bin/env python3
"""
Generate a multi-panel grid performance chart comparing V1, V2, V3, and JDK HashSet
across all benchmarked operations. Saves as a high-resolution PNG.
"""

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
from matplotlib.lines import Line2D
import csv
import os
import sys
import numpy as np

# ──────────────────────────────────────────────────────────────────────────────
# Configuration
# ──────────────────────────────────────────────────────────────────────────────

FILES = {
    'V1': 'CustomSetV1_performance.csv',
    'V2': 'CustomSetV2_performance.csv',
    'V3': 'CustomSetV3_performance.csv',
    'JDK': 'HashSet_performance.csv',
}

OUTPUT_FILE = 'grid.png'

COLORS = {
    'V1': '#2ECFBF',       # Neon Teal
    'V2': '#9B6EF3',       # Neon Purple
    'V3': '#FF5C5c',       # Neon Coral / Red for V3
    'JDK': '#FFD166',      # Neon Yellow / Gold for HashSet
    'grid': '#252525',
}

DPI = 300

# Exact mapping matching CSV column headers
OPERATIONS = {
    'add(E)': 'add',
    'addAll(Collection)': 'addAll',
    'clear()': 'clear',
    'clone()': 'clone',
    'constructor()': 'constructor',
    'constructor(Collection)': 'constructorCollection',
    'constructor(int)': 'constructorInt',
    'constructor(int, double)': 'constructorIntDouble',
    'contains(Object)': 'contains',
    'containsAll(Collection)': 'containsAll',
    'equals(Object)': 'equals',
    'hashCode()': 'hashCode',
    'isEmpty()': 'isEmpty',
    'iterator()': 'iterator',
    'remove(Object)': 'remove',
    'removeAll(Collection)': 'removeAll',
    'retainAll(Collection)': 'retainAll',
    'size()': 'size',
    'spliterator()': 'spliterator',
    'toArray()': 'toArray',
    'toArray(T[])': 'toArrayT',
    'toString()': 'toString',
}


# ──────────────────────────────────────────────────────────────────────────────
# CSV Loading
# ──────────────────────────────────────────────────────────────────────────────

def load_csv(filepath):
    """Load semicolon-separated CSV file and return dict: {size: {op_name: time_value}}"""
    data = {}
    with open(filepath, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f, delimiter=';')
        for row in reader:
            size = int(float(row['Size']))
            data[size] = {
                k: float(v) if v not in (None, '') else np.nan
                for k, v in row.items() if k != 'Size'
            }
    return data


def format_y_axis(value, pos):
    """Format y-axis labels compactly."""
    if value == 0:
        return '0'
    if value >= 1_000_000:
        return f'{value/1_000_000:.1f}M'
    elif value >= 1_000:
        return f'{value/1_000:.0f}k'
    return f'{value:.1f}' if isinstance(value, float) and value < 10 else f'{int(value):,}'


# ──────────────────────────────────────────────────────────────────────────────
# Main Grid Generation
# ──────────────────────────────────────────────────────────────────────────────

def main():
    datasets = {}
    canonical_sizes = set()

    for name, path in FILES.items():
        if not os.path.exists(path):
            print(f"Warning: Optional file '{path}' not found. Skipping {name}.")
            continue
        print(f"Loading {path}...")
        data = load_csv(path)
        datasets[name] = data
        canonical_sizes.update(data.keys())

    if not datasets:
        print("Error: No performance datasets found in the directory.")
        sys.exit(1)

    canonical_sizes = sorted(list(canonical_sizes))
    print(f"Unified sizes for x-axis: {canonical_sizes}")

    # Set up grid dimensions: 4 columns x 6 rows (24 slots for 22 operations)
    n_ops = len(OPERATIONS)
    ncols = 4
    nrows = (n_ops + ncols - 1) // ncols

    fig, axes = plt.subplots(nrows=nrows, ncols=ncols, figsize=(18, 22), dpi=DPI, facecolor='none')
    fig.patch.set_alpha(0)
    axes_flat = axes.flatten()

    x_positions = list(range(len(canonical_sizes)))
    legend_elements = [
        Line2D([0], [0], marker='o', color='none', markerfacecolor=COLORS[name],
               markeredgecolor=COLORS[name], markeredgewidth=1.5, markersize=8, label=name, linestyle='none')
        for name in datasets.keys()
    ]

    for idx, (operation_key, operation_label) in enumerate(OPERATIONS.items()):
        ax = axes_flat[idx]
        ax.set_facecolor('none')

        for name, data in datasets.items():
            values = [
                data[s][operation_key] if s in data and operation_key in data[s] else np.nan
                for s in canonical_sizes
            ]
            color = COLORS[name]
            ax.plot(x_positions, values, color=color, linewidth=1.2, zorder=2)
            ax.scatter(x_positions, values, color=color, s=20, marker='o', edgecolors=color, linewidths=1.0, zorder=3)

        ax.grid(True, color=COLORS['grid'], linewidth=0.6, linestyle='-', zorder=0)
        ax.set_axisbelow(True)

        ax.set_xticks(x_positions)
        # Show tick labels only on bottom row to reduce clutter
        if idx >= (nrows - 1) * ncols:
            ax.set_xticklabels([f'{s:,}' for s in canonical_sizes], color='white', fontsize=8, rotation=25, ha='right')
        else:
            ax.set_xticklabels([])

        ax.tick_params(axis='x', colors='white', length=0, pad=4)
        ax.set_xlim(-0.4, len(canonical_sizes) - 0.6)

        ax.yaxis.set_major_formatter(mticker.FuncFormatter(format_y_axis))
        ax.tick_params(axis='y', colors='white', length=0, pad=4)
        for label in ax.get_yticklabels():
            label.set_color('white')
            label.set_fontsize(8)

        for spine in ax.spines.values():
            spine.set_visible(False)

        ax.set_title(operation_key, color='white', fontsize=11, fontweight='bold', pad=6)

    # Hide any unused subplots in the last row
    for idx in range(n_ops, len(axes_flat)):
        fig.delaxes(axes_flat[idx])

    # Add global figure title and legend at the top
    fig.suptitle('CustomSet Performance Comparison Grid (V1 vs V2 vs V3 vs JDK)',
                 color='white', fontsize=16, fontweight='bold', y=0.98)

    fig.legend(
        handles=legend_elements,
        loc='upper center',
        bbox_to_anchor=(0.5, 0.95),
        ncol=len(datasets),
        frameon=False,
        fontsize=12,
        handlelength=1.5,
        handletextpad=0.6,
        columnspacing=3.0
    )

    plt.tight_layout(rect=[0, 0.02, 1, 0.93])
    fig.savefig(
        OUTPUT_OUTPUT_FILE := OUTPUT_FILE,
        dpi=DPI,
        transparent=True,
        bbox_inches='tight',
        facecolor='none',
        edgecolor='none'
    )
    plt.close(fig)
    print(f"\n✓ Grid comparison chart successfully generated and saved to '{OUTPUT_FILE}'!")


if __name__ == '__main__':
    main()
#!/usr/init/env python3
"""
Generate performance comparison charts between CustomSet V3 and JDK HashSet.
Saves PNG files into the local directory "V3_JDK".
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
    'V3': 'CustomSetV3_performance.csv',
    'JDK': 'HashSet_performance.csv',
}

OUTPUT_DIR = "V3_JDK"

COLORS = {
    'V3': '#FF5C5c',  # Neon Coral / Red for V3
    'JDK': '#FFD166',   # Neon Yellow / Gold for HashSet
    'grid': '#252525',
}

FIGURE_SIZE = (12, 6.2)
DPI = 150

OPERATIONS = {
    'add(E)': 'add',
    'addAll(Collection)': 'addAll',
    'clear()': 'clear',
    'clone()': 'clone',
    'constructor()': 'constructor',
    'constructor(Collection)': 'constructorCollection',
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
            size = int(row['Size'])
            data[size] = {
                k: int(v) for k, v in row.items() if k != 'Size'
            }
    return data


# ──────────────────────────────────────────────────────────────────────────────
# Chart Generation
# ──────────────────────────────────────────────────────────────────────────────

def format_y_axis(value, pos):
    """Format y-axis labels with comma separators."""
    if value == 0:
        return '0'
    return f'{int(value):,}'


def create_chart(operation_key, operation_label, datasets, canonical_sizes, output_path):
    """
    Create a head-to-head performance chart comparing CustomSet V3 and JDK HashSet.
    """
    fig, ax = plt.subplots(figsize=FIGURE_SIZE, dpi=DPI)
    fig.patch.set_alpha(0)
    ax.set_facecolor('none')

    x_positions = list(range(len(canonical_sizes)))
    legend_elements = []

    for name, data in datasets.items():
        values = [
            data[s][operation_key] if s in data and operation_key in data[s] else np.nan
            for s in canonical_sizes
        ]
        color = COLORS[name]

        ax.plot(x_positions, values, color=color, linewidth=1.8, zorder=2)
        ax.scatter(
            x_positions, values,
            color=color, s=40, marker='o',
            edgecolors=color, linewidths=1.5, zorder=3
        )

        legend_elements.append(
            Line2D(
                [0], [0],
                marker='o', color='none',
                markerfacecolor=color, markeredgecolor=color,
                markeredgewidth=1.5, markersize=8,
                label=name, linestyle='none'
            )
        )

    # ── Grid & Formatting ─────────────────────────────────────────────────────
    ax.grid(True, color=COLORS['grid'], linewidth=0.8, linestyle='-', zorder=0)
    ax.set_axisbelow(True)

    ax.set_xticks(x_positions)
    ax.set_xticklabels([f'{s:,}' for s in canonical_sizes], color='white', fontsize=10)
    ax.tick_params(axis='x', colors='white', length=0, pad=8)
    ax.set_xlim(-0.4, len(canonical_sizes) - 0.6)

    ax.yaxis.set_major_formatter(mticker.FuncFormatter(format_y_axis))
    ax.tick_params(axis='y', colors='white', length=0, pad=8)
    for label in ax.get_yticklabels():
        label.set_color('white')
        label.set_fontsize(10)

    for spine in ax.spines.values():
        spine.set_visible(False)

    ax.set_xlabel('Size', color='white', fontsize=12, labelpad=12)
    ax.set_ylabel('Time (ns)', color='white', fontsize=11, labelpad=10)
    ax.set_title(operation_key, color='white', fontsize=15, fontweight='bold', pad=14)

    # ── Legend ────────────────────────────────────────────────────────────────
    leg = ax.legend(
        handles=legend_elements,
        loc='upper center',
        bbox_to_anchor=(0.5, -0.26),
        ncol=2,
        frameon=False,
        fontsize=12,
        handlelength=1.5,
        handletextpad=0.6,
        columnspacing=2.5
    )

    for text in leg.get_texts():
        text.set_color('white')
        text.set_fontsize(12)

    plt.tight_layout(rect=[0, 0.18, 1, 1])
    fig.savefig(
        output_path,
        dpi=DPI,
        transparent=True,
        bbox_inches='tight',
        facecolor='none',
        edgecolor='none'
    )
    plt.close(fig)


# ──────────────────────────────────────────────────────────────────────────────
# Main
# ──────────────────────────────────────────────────────────────────────────────

def main():
    datasets = {}
    canonical_sizes = set()

    for name, path in FILES.items():
        if not os.path.exists(path):
            print(f"Error: Required file '{path}' not found.")
            sys.exit(1)
        print(f"Loading {path}...")
        data = load_csv(path)
        datasets[name] = data
        canonical_sizes.update(data.keys())
        print(f"  Loaded {len(data)} sizes for {name}")

    canonical_sizes = sorted(list(canonical_sizes))
    print(f"\nUnified sizes for x-axis: {canonical_sizes}")

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    print(f"\nGenerating V3 vs JDK comparison charts in '{OUTPUT_DIR}/'...")

    for csv_col, chart_label in OPERATIONS.items():
        output_path = os.path.join(OUTPUT_DIR, f'{chart_label}.png')
        create_chart(csv_col, chart_label, datasets, canonical_sizes, output_path)
        print(f"  ✓ {chart_label}.png")

    print(f"\n✓ All V3 vs JDK comparison charts saved successfully to '{OUTPUT_DIR}'!")


if __name__ == '__main__':
    main()
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter
from scipy.stats import gmean

# 1. Load data for V2 and V3
v2_df = pd.read_csv('CustomSetV2_performance.csv', sep=';')
v3_df = pd.read_csv('CustomSetV3_performance.csv', sep=';')

common_sizes = set(v2_df['Size']).intersection(set(v3_df['Size']))
v2_df = v2_df[v2_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)
v3_df = v3_df[v3_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)

# Exclude extreme outliers: clear() and retainAll(Collection)
excluded_methods = {
    'clear()',
    'retainAll(Collection)'
}

all_methods = [
    col for col in v3_df.columns
    if col != 'Size' and col in v2_df.columns and col.lower() != 'cleartime'
]

v2_df_fixed = v2_df.copy()
v3_df_fixed = v3_df.copy()
for col in all_methods:
    v2_df_fixed[col] = pd.to_numeric(v2_df_fixed[col], errors='coerce').fillna(1).replace(0, 1)
    v3_df_fixed[col] = pd.to_numeric(v3_df_fixed[col], errors='coerce').fillna(1).replace(0, 1)

ratios = []
labels = []
colors = []
omitted_notes = []

v2_win_color = '#9B6EF3'    # Neon Purple for V2
v3_win_color = '#FF5C5c'    # Neon Coral / Red for V3

for m in all_methods:
    g_v2 = gmean(v2_df_fixed[m])
    g_v3 = gmean(v3_df_fixed[m])

    if g_v3 < g_v2:
        speedup = g_v2 / g_v3
        signed_val = speedup - 1.0
        faster_ver = 'V3'
        color = v3_win_color
    else:
        speedup = g_v3 / g_v2
        signed_val = -(speedup - 1.0)
        faster_ver = 'V2'
        color = v2_win_color

    if m in excluded_methods:
        omitted_notes.append(f"{m}: {speedup:,.2f}x ({faster_ver} faster)")
    else:
        ratios.append(signed_val)
        labels.append(m)
        colors.append(color)

sorted_indices = np.argsort(ratios)
sorted_ratios = [ratios[idx] for idx in sorted_indices]
sorted_labels = [labels[idx] for idx in sorted_indices]
sorted_colors = [colors[idx] for idx in sorted_indices]

min_ratio = min(sorted_ratios)
max_ratio = max(sorted_ratios)
span = max(max_ratio - min_ratio, 0.1)
pad = span * 0.22

left_limit = (min_ratio - pad) if min_ratio < 0 else -pad * 0.25
right_limit = (max_ratio + pad) if max_ratio > 0 else pad * 0.25

fig_height = max(6, len(labels) * 0.45)
fig, ax = plt.subplots(figsize=(12, fig_height), facecolor='none')
ax.set_facecolor('none')

bars = ax.barh(range(len(sorted_labels)), sorted_ratios, color=sorted_colors, alpha=0.9, height=0.6)
ax.axvline(x=0, color='#ffffff', linewidth=1.2)

ax.set_xlim(left_limit, right_limit)

# Add multiplier label next to each bar
label_offset = (right_limit - left_limit) * 0.012
for idx, val in enumerate(sorted_ratios):
    mult = abs(val) + 1.0
    text_str = '1.00x' if abs(val) < 1e-4 else f'{mult:.2f}x'
    if val >= 0:
        ax.text(val + label_offset, idx, text_str,
                va='center', ha='left', color='#ffffff', fontsize=9.5, fontweight='bold')
    else:
        ax.text(val - label_offset, idx, text_str,
                va='center', ha='right', color='#ffffff', fontsize=9.5, fontweight='bold')

def format_multiplier(x, _):
    if abs(x) < 1e-6:
        return 'Tie'
    mult = abs(x) + 1.0
    return f'{mult:.2f}x' if span < 2.0 else f'{mult:.1f}x'

ax.xaxis.set_major_formatter(FuncFormatter(format_multiplier))
ax.tick_params(axis='x', colors='#ffffff', labelsize=11, pad=6)

ax.set_ylim(-0.5, len(labels) - 0.5)
ax.set_yticks(range(len(sorted_labels)))
ax.set_yticklabels(sorted_labels, color='#ffffff', fontsize=10)

ax.set_title('Overall Relative Performance Comparison (V3 vs V2)\n(Geometric Mean Across All Sizes)',
             fontsize=14, fontweight='bold', pad=15, color='#ffffff')

zero_rel_x = (0.0 - left_limit) / (right_limit - left_limit)
ax.text(zero_rel_x - 0.02, -0.075, '← V2 Faster', transform=ax.transAxes,
        ha='right', va='top', color='#ffffff', fontsize=11, fontweight='bold')
ax.text(zero_rel_x, -0.075, '|', transform=ax.transAxes,
        ha='center', va='top', color='#ffffff', fontsize=11, fontweight='bold')
ax.text(zero_rel_x + 0.02, -0.075, 'V3 Faster →', transform=ax.transAxes,
        ha='left', va='top', color='#ffffff', fontsize=11, fontweight='bold')

ax.text(0.5, -0.13, 'Relative Speedup Multiplier (Tie = 1.0x)',
        transform=ax.transAxes, ha='center', va='top', fontsize=12, color='#ffffff')

if omitted_notes:
    note_text = "* Omitted extreme outliers due to high degree of difference — " + " | ".join(omitted_notes)
    fig.text(0.5, 0.012, note_text,
             ha='center', va='bottom', color='#ffffff', fontsize=10, fontstyle='italic')

ax.grid(True, axis='x', linestyle='--', alpha=0.3, color='#888888')
ax.tick_params(colors='#ffffff', which='both', length=0)

for spine in ax.spines.values():
    spine.set_edgecolor('#555555')

plt.subplots_adjust(left=0.22, right=0.97, top=0.90, bottom=0.17)
<<<<<<< HEAD
plt.savefig('geometric.png', dpi=300, transparent=True, facecolor='none', edgecolor='none')
=======
plt.savefig('geometric_v3_v2.png', dpi=300, transparent=True, facecolor='none', edgecolor='none')
>>>>>>> refactor
plt.close()
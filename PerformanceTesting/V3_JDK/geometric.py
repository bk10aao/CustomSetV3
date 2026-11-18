import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter
from scipy.stats import gmean

# 1. Load data with robust float parsing (fixed paths to local workspace)
v3_df = pd.read_csv('CustomSetV3_performance.csv', sep=';')
jdk_df = pd.read_csv('HashSet_performance.csv', sep=';')

common_sizes = set(v3_df['Size']).intersection(set(jdk_df['Size']))
v3_df = v3_df[v3_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)
jdk_df = jdk_df[jdk_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)

excluded_methods = {
    'retainAll(Collection)'
}

all_methods = [
    col for col in v3_df.columns
    if col != 'Size' and col.lower() != 'cleartime'
]

v3_df_fixed = v3_df.copy()
jdk_df_fixed = jdk_df.copy()
for col in all_methods:
    v3_df_fixed[col] = pd.to_numeric(v3_df_fixed[col], errors='coerce').fillna(1).replace(0, 1)
    jdk_df_fixed[col] = pd.to_numeric(jdk_df_fixed[col], errors='coerce').fillna(1).replace(0, 1)

ratios = []
labels = []
colors = []
omitted_notes = []

v3_win_color = '#FF5C5c'    # Neon Coral / Red for V3
jdk_win_color = '#FFD166'   # Neon Yellow / Gold for JDK HashSet

for m in all_methods:
    g_v3 = gmean(v3_df_fixed[m])
    g_jdk = gmean(jdk_df_fixed[m])

    if g_v3 < g_jdk:
        speedup = g_jdk / g_v3
        signed_val = speedup - 1.0
        faster_ver = 'V3'
        color = v3_win_color
    else:
        speedup = g_v3 / g_jdk
        signed_val = -(speedup - 1.0)
        faster_ver = 'JDK'
        color = jdk_win_color

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
pad = span * 0.22  # Extra breathing room for bar value labels

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

# Dynamic uncapped tick formatting
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

ax.set_title('Overall Relative Performance Comparison (V3 vs JDK)\n(Geometric Mean Across All Sizes)',
             fontsize=14, fontweight='bold', pad=15, color='#ffffff')

# Bottom directional labels aligned to the 0 (Tie) centerline + axis title + footnote
zero_rel_x = (0.0 - left_limit) / (right_limit - left_limit)
ax.text(zero_rel_x - 0.02, -0.075, '← JDK Faster', transform=ax.transAxes,
        ha='right', va='top', color='#ffffff', fontsize=11, fontweight='bold')
ax.text(zero_rel_x, -0.075, '|', transform=ax.transAxes,
        ha='center', va='top', color='#ffffff', fontsize=11, fontweight='bold')
ax.text(zero_rel_x + 0.02, -0.075, 'V3 Faster →', transform=ax.transAxes,
        ha='left', va='top', color='#ffffff', fontsize=11, fontweight='bold')

ax.text(0.5, -0.13, 'Relative Speedup Multiplier (Tie = 1.0x)',
        transform=ax.transAxes, ha='center', va='top', fontsize=12, color='#ffffff')

if omitted_notes:
    note_text = "* Omitted extreme outlier due to high degree of difference — " + " | ".join(omitted_notes)
    fig.text(0.5, 0.012, note_text,
             ha='center', va='bottom', color='#ffffff', fontsize=10, fontstyle='italic')

ax.grid(True, axis='x', linestyle='--', alpha=0.3, color='#888888')
ax.tick_params(colors='#ffffff', which='both', length=0)

for spine in ax.spines.values():
    spine.set_edgecolor('#555555')

plt.subplots_adjust(left=0.22, right=0.97, top=0.90, bottom=0.17)
plt.savefig('geometric_v3_jdk.png', dpi=300, transparent=True, facecolor='none', edgecolor='none')
plt.close()

print("Geometric chart with bar value labels generated successfully!")
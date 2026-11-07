import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import gmean

# 1. Load data
v3_df = pd.read_csv('CustomSetV3_performance.csv', sep=';')
v2_df = pd.read_csv('CustomSetV2_performance.csv', sep=';')

common_sizes = set(v3_df['Size']).intersection(set(v2_df['Size']))
v3_df = v3_df[v3_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)
v2_df = v2_df[v2_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)

methods = [col for col in v3_df.columns if col != 'Size' and col.lower() != 'cleartime']

v3_df_fixed = v3_df.copy()
v2_df_fixed = v2_df.copy()
for col in methods:
    v3_df_fixed[col] = v3_df_fixed[col].replace(0, 1)
    v2_df_fixed[col] = v2_df_fixed[col].replace(0, 1)

ratios = []
labels = []
colors = []

v3_win_color = '#FF5C5c'    # Neon Coral / Red for V3
v2_win_color = '#9B6EF3'   # Neon Purple for V2

for m in methods:
    g_v3 = gmean(v3_df_fixed[m])
    g_v2 = gmean(v2_df_fixed[m])

    if g_v3 < g_v2:
        # V3 is faster (took less time)
        speedup = g_v2 / g_v3
        ratios.append(speedup - 1)
        colors.append(v3_win_color)
    else:
        # V2 is faster
        speedup = g_v3 / g_v2
        ratios.append(-(speedup - 1))
        colors.append(v2_win_color)
    labels.append(m)

sorted_indices = np.argsort(ratios)
sorted_ratios = [ratios[idx] for idx in sorted_indices]
sorted_labels = [labels[idx] for idx in sorted_indices]
sorted_colors = [colors[idx] for idx in sorted_indices]

# Asymmetric limits
min_ratio = min(sorted_ratios)
max_ratio = max(sorted_ratios)

left_limit = min(min_ratio - 0.2, -0.5)
right_limit = max(max_ratio + 0.2, 1.0)

fig_height = max(6, len(methods) * 0.45)
fig, ax = plt.subplots(figsize=(12, fig_height), facecolor='none')
ax.set_facecolor('none')

bars = ax.barh(range(len(sorted_labels)), sorted_ratios, color=sorted_colors, alpha=0.9, height=0.6)
ax.axvline(x=0, color='#ffffff', linewidth=1.2)

ax.set_xlim(left_limit, right_limit)

ticks = [0.0]
for t in [-4.0, -3.0, -2.0, -1.0, 1.0, 2.0, 3.0, 4.0]:
    if left_limit <= t <= right_limit:
        ticks.append(t)
ticks = sorted(list(set(ticks)))

ax.set_xticks(ticks)
ax.set_xticklabels([f'{abs(t)+1:.1f}x' if abs(t) > 0.05 else 'Tie' for t in ticks],
                   color='#ffffff', fontsize=11)

ax.set_ylim(-0.5, len(methods) - 0.5)
ax.set_yticks(range(len(sorted_labels)))
ax.set_yticklabels(sorted_labels, color='#ffffff', fontsize=10)

ax.set_title('Overall Relative Performance Comparison (V3 vs V2)\n(Geometric Mean Across All Sizes)',
             fontsize=14, fontweight='bold', pad=15, color='#ffffff')
ax.set_xlabel('← V2  |  Relative Speedup Factor  |  V3 Faster →',
              fontsize=12, labelpad=10, color='#ffffff')

ax.grid(True, axis='x', linestyle='--', alpha=0.3, color='#888888')
ax.tick_params(colors='#ffffff', which='both', length=0)

for spine in ax.spines.values():
    spine.set_edgecolor('#555555')

plt.tight_layout()
plt.savefig('geometric_v3_v2.png', dpi=300, transparent=True)
plt.close()
print("Geometric chart V3 vs V2 updated successfully!")
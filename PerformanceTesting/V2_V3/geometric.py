import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.stats import gmean

# 1. Load data with robust float parsing (Comparing V2 vs V3)
v2_df = pd.read_csv('CustomSetV2_performance.csv', sep=';')
v3_df = pd.read_csv('CustomSetV3_performance.csv', sep=';')

common_sizes = set(v2_df['Size']).intersection(set(v3_df['Size']))
v2_df = v2_df[v2_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)
v3_df = v3_df[v3_df['Size'].isin(common_sizes)].sort_values('Size').reset_index(drop=True)

# Define methods to include, explicitly omitting 'clear()' and 'retainAll(Collection)'
excluded_methods = {
    'clear()',
    'retainAll(Collection)'
}

methods = [
    col for col in v3_df.columns
    if col != 'Size' and col not in excluded_methods
]

v2_df_fixed = v2_df.copy()
v3_df_fixed = v3_df.copy()
for col in methods:
    v2_df_fixed[col] = pd.to_numeric(v2_df_fixed[col], errors='coerce').fillna(1).replace(0, 1)
    v3_df_fixed[col] = pd.to_numeric(v3_df_fixed[col], errors='coerce').fillna(1).replace(0, 1)

data = []
for m in methods:
    g_v2 = gmean(v2_df_fixed[m])
    g_v3 = gmean(v3_df_fixed[m])
    # Speedup factor of V3 over V2 (V2 time / V3 time)
    speedup = g_v2 / g_v3 if g_v3 > 0 else 1.0
    data.append({'method': m, 'speedup': speedup})

df_res = pd.DataFrame(data).sort_values('speedup', ascending=True)

speeds = df_res['speedup'].values
methods_list = df_res['method'].values
colors = ['#FF5C5c' if s >= 1.0 else '#9B6EF3' for s in speeds]

fig_height = max(6, len(methods_list) * 0.45)
fig, ax = plt.subplots(figsize=(11, fig_height), facecolor='none')
ax.set_facecolor('none')

bars = ax.barh(range(len(methods_list)), speeds - 1.0, color=colors, alpha=0.9, height=0.6)
ax.axvline(x=0, color='#ffffff', linewidth=1.2)

max_s = max(speeds) - 1.0
ax.set_xlim(-0.02, max(max_s * 1.15, 0.4))

ticks = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5]
ticks = [t for t in ticks if t <= (max_s * 1.15 if max_s > 0 else 0.5)]
ax.set_xticks(ticks)
ax.set_xticklabels([f'{t+1.0:.1f}x' for t in ticks], color='#ffffff', fontsize=11)

ax.set_ylim(-0.5, len(methods_list) - 0.5)
ax.set_yticks(range(len(methods_list)))
ax.set_yticklabels(methods_list, color='#ffffff', fontsize=10)

ax.set_title('V3 Speedup Factor Over V2\n(Geometric Mean Across All Sizes)',
             fontsize=14, fontweight='bold', pad=15, color='#ffffff')
ax.set_xlabel('Relative Speedup Multiplier (V2 Time / V3 Time)',
              fontsize=12, labelpad=10, color='#ffffff')

# Add note about omitted methods
plt.figtext(0.5, 0.02, '* Note: clear() and retainAll(Collection) were omitted due to high degree of difference.',
            ha='center', color='#aaaaaa', fontsize=10, fontstyle='italic')

ax.grid(True, axis='x', linestyle='--', alpha=0.3, color='#888888')
ax.tick_params(colors='#ffffff', which='both', length=0)

for spine in ax.spines.values():
    spine.set_edgecolor('#555555')

plt.tight_layout(rect=[0, 0.04, 1, 1])
plt.savefig('geometric.png', dpi=300, transparent=True, facecolor='none', edgecolor='none')
plt.close()

print("Geometric chart updated to unidirectional layout successfully!")
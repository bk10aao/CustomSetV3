import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns

# 1. Load data
v3_df = pd.read_csv('CustomSetV3_performance.csv', sep=';').sort_values('Size').reset_index(drop=True)
v2_df = pd.read_csv('CustomSetV2_performance.csv', sep=';').sort_values('Size').reset_index(drop=True)

# Define desired vertical order (Top to Bottom)
ordered_methods = [
    'add(E)', 'addAll(Collection)', 'clear()', 'clone()',
    'constructor()', 'constructor(Collection)', 'contains(Object)',
    'containsAll(Collection)', 'equals(Object)', 'hashCode()',
    'isEmpty()', 'iterator()', 'remove(Object)', 'removeAll(Collection)',
    'retainAll(Collection)', 'size()', 'toArray()', 'toArray(T[])', 'toString()'
]

# Get common sizes and filter operations
common_sizes = sorted(list(set(v3_df['Size']).intersection(set(v2_df['Size']))))
available_methods = [col for col in v3_df.columns if col != 'Size']
methods = [m for m in ordered_methods if m in available_methods]

# 2. Construct DataFrames
heatmap_data = []
text_labels = []

for m in methods:
    row_vals = []
    row_annots = []
    for size in common_sizes:
        v3_val = v3_df.loc[v3_df['Size'] == size, m].values[0]
        v2_val = v2_df.loc[v2_df['Size'] == size, m].values[0]

        v3_c, v2_c = (1 if v == 0 else v for v in (v3_val, v2_val))

        ratio = np.log2(v2_c / v3_c)  # Positive = V3 faster (V2 time / V3 time)
        row_vals.append(ratio)

        factor = v2_c / v3_c if v2_c > v3_c else v3_c / v2_c
        prefix = "+" if v3_c < v2_c else "-"  # "+" indicates V3 took less time
        row_annots.append(f"{prefix}{factor:.1f}x")

    heatmap_data.append(row_vals)
    text_labels.append(row_annots)

df_heatmap = pd.DataFrame(heatmap_data, index=methods, columns=common_sizes)
df_annot = pd.DataFrame(text_labels, index=methods, columns=common_sizes)

# 3. Lock the ordering via Categorical Indexing
df_heatmap.index = pd.Categorical(df_heatmap.index, categories=methods, ordered=True)
df_heatmap = df_heatmap.sort_index()
df_annot = df_annot.reindex(df_heatmap.index)

# 4. Render Heatmap
fig, ax = plt.subplots(figsize=(14, 11), facecolor='none')
ax.set_facecolor('none')

clipped_data = np.clip(df_heatmap.values, -3.0, 3.0)

sns.heatmap(df_heatmap,
            annot=df_annot.values,
            fmt="",
            cmap=sns.diverging_palette(15, 240, as_cmap=True),
            center=0,
            ax=ax,
            cbar_kws={'label': '← V2 Faster  |  Relative Speedup Scale  |  V3 Faster →'},
            linewidths=0.8,
            linecolor='#555555',
            annot_kws={'size': 9, 'weight': 'bold'})

# 5. Styling
ax.set_title('CustomSet V3 vs CustomSet V2 Performance Speedup Matrix', color='#ffffff', fontsize=16, fontweight='bold', pad=20)
ax.set_ylabel('Set Interface Methods', color='#aaaaaa', fontsize=13, labelpad=10)
ax.set_xlabel('Collection Size (Elements)', color='#aaaaaa', fontsize=13, labelpad=10)

ax.tick_params(colors='#ffffff', labelsize=10)
plt.xticks(rotation=45)
plt.yticks(rotation=0)

cbar = ax.collections[0].colorbar
cbar.ax.tick_params(colors='#ffffff', labelsize=10)
cbar.ax.yaxis.label.set_color('#ffffff')

plt.tight_layout()
plt.savefig('heatmap_v3_v2.png', dpi=300, transparent=True)
plt.close()
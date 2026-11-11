import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

# 1. Load data with robust float parsing (Comparing V2 vs V3)
v2_df = (
    pd.read_csv('CustomSetV2_performance.csv', sep=';')
    .sort_values('Size')
    .reset_index(drop=True)
)
v3_df = (
    pd.read_csv('CustomSetV3_performance.csv', sep=';')
    .sort_values('Size')
    .reset_index(drop=True)
)

# Define desired vertical order (Top to Bottom) including constructors, excluding clear()
ordered_methods = [
    'add(E)',
    'addAll(Collection)',
    'clone()',
    'constructor()',
    'constructor(Collection)',
    'constructor(int)',
    'constructor(int, double)',
    'contains(Object)',
    'containsAll(Collection)',
    'equals(Object)',
    'hashCode()',
    'isEmpty()',
    'iterator()',
    'spliterator()',
    'remove(Object)',
    'removeAll(Collection)',
    'retainAll(Collection)',
    'size()',
    'toArray()',
    'toArray(T[])',
    'toString()',
]

# Get common sizes and filter operations
common_sizes = sorted(list(set(v2_df['Size']).intersection(set(v3_df['Size']))))
available_methods = [col for col in v3_df.columns if col != 'Size']
methods = [m for m in ordered_methods if m in available_methods]

# 2. Construct DataFrames
heatmap_data = []
text_labels = []

for m in methods:
  row_vals = []
  row_annots = []
  for size in common_sizes:
    v2_val = float(v2_df.loc[v2_df['Size'] == size, m].values[0])
    v3_val = float(v3_df.loc[v3_df['Size'] == size, m].values[0])

    v2_c, v3_c = (1e-6 if v == 0 else v for v in (v2_val, v3_val))

    ratio = np.log2(v2_c / v3_c)  # Positive = V3 faster
    row_vals.append(ratio)

    factor = v2_c / v3_c if v2_c > v3_c else v3_c / v2_c
    prefix = '+' if v3_c < v2_c else '-'
    row_annots.append(f'{prefix}{factor:.1f}x')

  heatmap_data.append(row_vals)
  text_labels.append(row_annots)

df_heatmap = pd.DataFrame(heatmap_data, index=methods, columns=common_sizes)
df_annot = pd.DataFrame(text_labels, index=methods, columns=common_sizes)

# 3. Lock the ordering via Categorical Indexing
df_heatmap.index = pd.Categorical(
    df_heatmap.index, categories=methods, ordered=True
)
df_heatmap = df_heatmap.sort_index()
df_annot = df_annot.reindex(df_heatmap.index)

# 4. Render Heatmap
fig, ax = plt.subplots(figsize=(14, 11), facecolor='none')
ax.set_facecolor('none')

# Diverging palette: Red (V2 faster) to Blue (V3 faster)
sns.heatmap(
    df_heatmap,
    annot=df_annot.values,
    fmt='',
    cmap=sns.diverging_palette(10, 240, as_cmap=True),
    center=0,
    ax=ax,
    cbar_kws={
        'label': '← V2 Faster  |  Relative Speedup Scale  |  V3 Faster (Blue) →'
    },
    linewidths=0.8,
    linecolor='#555555',
    annot_kws={'size': 9, 'weight': 'bold'},
)

# 5. Styling
ax.set_title(
    'CustomSet V3 vs V2 Performance Speedup Matrix',
    color='#ffffff',
    fontsize=16,
    fontweight='bold',
    pad=20,
)
ax.set_ylabel(
    'Set Interface Methods', color='#aaaaaa', fontsize=13, labelpad=10
)
ax.set_xlabel(
    'Collection Size (Elements)', color='#aaaaaa', fontsize=13, labelpad=10
)

ax.tick_params(colors='#ffffff', labelsize=10)
plt.xticks(rotation=45)
plt.yticks(rotation=0)

cbar = ax.collections[0].colorbar
cbar.ax.tick_params(colors='#ffffff', labelsize=10)
cbar.ax.yaxis.label.set_color('#ffffff')

fig.text(
    0.5,
    0.02,
    (
        'Note: clear() omitted due to extreme scale differences obscuring'
        ' standard method results'
    ),
    ha='center',
    va='bottom',
    color='#aaaaaa',
    fontsize=10,
    fontstyle='italic',
)

plt.tight_layout(rect=[0, 0.05, 1, 1])
plt.savefig('heatmap_v2_v3_with_constructors.png', dpi=300, transparent=True)
plt.close()
print('V2 vs V3 Heatmap with constructors generated successfully!')
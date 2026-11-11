import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

# 1. Load data
v1_df = (
    pd.read_csv('CustomSetV1_performance.csv', sep=';')
    .sort_values('Size')
    .reset_index(drop=True)
)
v3_df = (
    pd.read_csv('CustomSetV3_performance.csv', sep=';')
    .sort_values('Size')
    .reset_index(drop=True)
)

# Define desired vertical order (Top to Bottom), strictly excluding all constructors
ordered_methods = [
    'add(E)',
    'addAll(Collection)',
    'clear()',
    'clone()',
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

common_sizes = sorted(list(set(v1_df['Size']).intersection(set(v3_df['Size']))))
available_methods = [col for col in v3_df.columns if col != 'Size']
methods = [m for m in ordered_methods if m in available_methods]

heatmap_data = []
text_labels = []

for m in methods:
  row_vals = []
  row_annots = []
  for size in common_sizes:
    v1_val = float(v1_df.loc[v1_df['Size'] == size, m].values[0])
    v3_val = float(v3_df.loc[v3_df['Size'] == size, m].values[0])

    v1_c, v3_c = (1e-6 if v == 0 else v for v in (v1_val, v3_val))

    ratio = np.log2(v1_c / v3_c)  # Positive = V3 faster
    row_vals.append(ratio)

    factor = v1_c / v3_c if v1_c > v3_c else v3_c / v1_c
    prefix = '+' if v3_c < v1_c else '-'
    row_annots.append(f'{prefix}{factor:.1f}x')

  heatmap_data.append(row_vals)
  text_labels.append(row_annots)

df_heatmap = pd.DataFrame(heatmap_data, index=methods, columns=common_sizes)
df_annot = pd.DataFrame(text_labels, index=methods, columns=common_sizes)

df_heatmap.index = pd.Categorical(
    df_heatmap.index, categories=methods, ordered=True
)
df_heatmap = df_heatmap.sort_index()
df_annot = df_annot.reindex(df_heatmap.index)

# Adjust figure size and bottom margin to accommodate the note below
fig, ax = plt.subplots(figsize=(14, 9.5), facecolor='none')
ax.set_facecolor('none')

# Diverging palette: Red (V1 faster) to Blue (V3 faster)
sns.heatmap(
    df_heatmap,
    annot=df_annot.values,
    fmt='',
    cmap=sns.diverging_palette(10, 240, as_cmap=True),
    center=0,
    ax=ax,
    cbar_kws={
        'label': '← V1 Faster  |  Relative Speedup Scale  |  V3 Faster (Blue) →'
    },
    linewidths=0.8,
    linecolor='#555555',
    annot_kws={'size': 9, 'weight': 'bold'},
)

ax.set_title(
    'CustomSet V3 vs V1 Performance Speedup Matrix',
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

# Add note below the plot
fig.text(
    0.5,
    0.02,
    (
        'Note: Constructors omitted due to extreme scale differences obscuring'
        ' standard method results'
    ),
    ha='center',
    va='bottom',
    color='#aaaaaa',
    fontsize=10,
    fontstyle='italic',
)

plt.tight_layout(rect=[0, 0.05, 1, 1])
plt.savefig('heatmap_v3_v1_note_below.png', dpi=300, transparent=True)
plt.close()
print('Heatmap with note below generated successfully!')
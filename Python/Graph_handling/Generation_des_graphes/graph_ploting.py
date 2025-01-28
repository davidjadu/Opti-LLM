import pandas as pd
import matplotlib.pyplot as plt
import networkx as nx

# Define parameters
sigma_list = [1, 5, 21]
node_list = [5, 20, 50, 100, 200]

# Load the main CSV file
data = pd.read_csv("data/metadata.csv")

# Filter the data for average edges = 5
filtered_data = data[data['average edges'] == 5]

# Create the plot grid
fig, axes = plt.subplots(len(node_list), len(sigma_list), figsize=(15, 15), constrained_layout=True)

# Iterate over sigma and num_nodes combinations
for row_idx, num_nodes in enumerate(node_list):
    for col_idx, sigma in enumerate(sigma_list):
        # Filter the graph_id for current sigma and num_nodes
        subset = filtered_data[(filtered_data['sigma'] == sigma) & (filtered_data['num_nodes'] == num_nodes)]

        # Check if there is a matching graph
        if not subset.empty:
            graph_id = subset.iloc[0]['graph_id']  # Take the first match

            # Load the graph CSV file
            graph_file = f"data/graphs/graph_{int(graph_id)}.csv"
            graph_data = pd.read_csv(graph_file)

            # Create the graph using NetworkX
            G = nx.Graph()

            for _, row in graph_data.iterrows():
                G.add_edge(row['Node1'], row['Node2'], weight=row['Weight'])

            # for node in range(num_nodes):
            #     if node not in G:
            #         G.add_node(node)

            # Plot the graph
            ax = axes[row_idx, col_idx]
            pos = nx.spring_layout(G, seed=42)  # Layout for the graph
            nx.draw(G, pos, ax=ax, with_labels=False, node_size=100, font_size=8, edge_color='gray', alpha = 0.5)
            ax.set_title(f"$\\sigma$={sigma}, nodes={num_nodes}")
        else:
            # If no graph found, leave the subplot blank
            ax = axes[row_idx, col_idx]
            ax.set_title(f"$\\sigma$={sigma}, nodes={num_nodes}")
            ax.axis('off')

# Add overall figure title
fig.suptitle("Graphs with Average Edges = 5", fontsize=16)

# Save and show the figure
plt.savefig("graphs_grid.png")
plt.show()
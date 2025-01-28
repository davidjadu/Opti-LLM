import matplotlib.pyplot as plt
import networkx as nx
import numpy as np

def visualize_graph_from_adjacency_matrix(adj_matrix_path):
    # Load adjacency matrix
    adjacency_matrix = np.load(adj_matrix_path)

    # Create graph from adjacency matrix
    G = nx.from_numpy_array(adjacency_matrix)

    # Draw the graph with weights
    pos = nx.spring_layout(G)  # Positions for all nodes

    # Define node colors: green for 0-th, red for 199-th, blue for others
    node_colors = []
    for node in G.nodes():
        if node == 0:
            node_colors.append("green")
        elif node == len(G.nodes) - 1:
            node_colors.append("red")
        else:
            node_colors.append("lightblue")

    # Draw nodes, edges, and labels
    nx.draw(
        G, pos, with_labels=True,
        node_size=500, node_color=node_colors,
        font_size=10
    )
    edge_labels = nx.get_edge_attributes(G, "weight")
    nx.draw_networkx_edge_labels(G, pos, edge_labels=edge_labels)

    # Show the graph
    plt.title("Graph Visualization with Highlighted Nodes")
    plt.show()

# Visualize a sample graph
visualize_graph_from_adjacency_matrix("data/graph_9.npy")

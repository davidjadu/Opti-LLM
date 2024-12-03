import networkx as nx
from networkx import NetworkXNoPath
import numpy as np
import os
import random
import torch
from tqdm import tqdm

def generate_graphs(num_graphs, num_nodes, average_edges, weight_range, output_dir, sigma):
    """
    Generate a collection of TSP graphs and save them.

    Args:
        num_graphs (int): Number of graphs to generate.
        num_nodes (int): Number of nodes in each graph.
        edge_probability (float): Probability of edge creation (0 < p <= 1 for incomplete graphs, p = 1 for complete).
        weight_range (tuple): Range of edge weights (min, max).
        output_dir (str): Directory to save the graphs.
    """
    os.makedirs(output_dir, exist_ok=True)

    # To record one edge we need three the numbers. The path is clearly shorter than num_nodes
    estimated_length = 3*average_edges*num_nodes + num_nodes

    padding = num_nodes + weight_range[1]-weight_range[0]
    breaking = padding + 1


    edge_list = torch.full((num_graphs, estimated_length), padding)

    for i in tqdm(range(num_graphs)):
        # Create a random graph
        # G = nx.gnp_random_graph(n=num_nodes, p=edge_probability, directed=False)

        # # Add weights to the edges
        # for u, v in G.edges():
        #     G[u][v]['weight'] = random.randint(weight_range[0], weight_range[1])

        G, p = get_valid_graph(num_nodes, average_edges, weight_range, sigma)

        j=0
        randomize_edges_list=list(G.edges)
        random.shuffle(randomize_edges_list)
        for u, v in randomize_edges_list:
            edge_list[i,3*j] = u
            edge_list[i,3*j+1] = v
            edge_list[i,3*j+2] = G[u][v]['weight'] + num_nodes-1 # So the weights are saved with different keys
            j+=1

        edge_list[i,3*j] = breaking

        for k, x in enumerate(p):
            edge_list[i,3*j+k] = x

        # adjacency_matrix = nx.to_numpy_array(G, weight='weight')
        # np.save(os.path.join(output_dir, f"graph_{i}.npy"), adjacency_matrix)

    print('generation done')
    output_file = output_dir + f"{num_graphs=}_{num_nodes=}_{sigma=}.pt"

    torch.save(edge_list,output_file)

        
    # Optionally save as an edge list
    edge_list_path = os.path.join(output_dir, f"graph_{i}.txt")
    with open(edge_list_path, 'w') as f:
        for u, v, data in G.edges(data=True):
            f.write(f"{u} {v} {data['weight']}\n")

    print(f"Graph {i} saved with {len(G.nodes)} nodes and {len(G.edges)} edges.")
    try:
        print(nx.dijkstra_path(G,source=0,target=num_nodes-1,weight='weight'))
    except:
        print("No path found")


def get_valid_graph(num_nodes, average_edges, weight_range, sigma):
    while True:
        G = nx.Graph()
        G.add_nodes_from(range(num_nodes))

        for n in range(num_nodes):
            for m in range(n + 1, num_nodes):  # Avoid self-loops and duplicate edges
                prob = average_edges* 1/np.sqrt(np.pi*sigma**2) * np.exp(-((m - n) ** 2) / (2 * sigma ** 2))
                if np.random.rand() < prob:  # Add edge with probability 'prob'
                    G.add_edge(n, m)

        for u, v in G.edges():
            G[u][v]['weight'] = random.randint(weight_range[0], weight_range[1])

        try:
            shortest_path = nx.dijkstra_path(G,source=0,target=num_nodes-1,weight='weight')
            return G, shortest_path
        except NetworkXNoPath:
            pass



if __name__=='__main__':
    # Parameters
    num_graphs = 10            # Number of graphs to generate
    num_nodes = 50           # Number of nodes in each graph
    average_edges = 2    # Probability of edge creation (use 1.0 for a complete graph)
    weight_range = (1, 100)        # Weight range (min, max)
    sigma = 10
    output_dir = f"data/gaussian_graphs"      # Directory to save the graphs

    # Generate and save graphs
    generate_graphs(num_graphs, num_nodes, average_edges, weight_range, output_dir, sigma)

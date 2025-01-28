import os
import csv

class Graph():
    def __init__(self, graph_id: int):
        self.graph_id = graph_id
        self.metadata = self.get_metadata()
        self.file_path = f"{os.getenv('PATH_TO_DATA')}/graphs/graph_{self.graph_id}.csv"
        self.graph = self.get_graph_from_csv(self.file_path)

    def get_metadata(self):
        metadata_file_path = f"{os.getenv('PATH_TO_DATA')}/metadata.csv"
        with open(metadata_file_path, mode='r', newline='', encoding='utf-8') as file:
            csv_reader = csv.DictReader(file)
            for row in csv_reader:
                if row['graph_id'] == str(self.graph_id):
                    return row
        return None

    def read_graph_from_csv(self, file_path):
        graph = {}
        with open(file_path, 'r') as csvfile:
            csvreader = csv.DictReader(csvfile)
            for row in csvreader:
                node1 = int(row['Node1'])
                node2 = int(row['Node2'])
                weight = int(row['Weight'])

                if node1 not in graph:
                    graph[node1] = []
                if node2 not in graph:
                    graph[node2] = []

                graph[node1].append((node2, weight))
                graph[node2].append((node1, weight))
        return graph

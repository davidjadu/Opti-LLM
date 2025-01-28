import heapq
import csv
import os

class Explainer:
    def __init__(self, graph_id):
        self.file_path = f"{os.getenv('PATH_TO_DATA')}/graphs/graph_{graph_id}.csv"
        self.graph = self.read_graph_from_csv(self.file_path)

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

    def shortest_path_explanation(self, start, target):
        # Priority queue for Dijkstra's algorithm
        priority_queue = [(0, start, [])]  # (distance, current_node, path)
        visited = set()
        distances = {node: float('inf') for node in self.graph}
        distances[start] = 0

        explanation = []

        length = 0
        plural = ""

        while priority_queue:
            current_distance, current_node, path = heapq.heappop(priority_queue)

            length = length + 1
            if current_node in visited:
                continue

            # Mark as visited
            visited.add(current_node)
            path = path + [current_node]

            if current_node == target:
                explanation.append(f"Every path that has been traveled has bigger distance than the one to {target}, therefore we reached the target node {target} with a total distance of {current_distance}.")
                explanation.append(f"FINAL ANSWER: Path: {' -> '.join(map(str, path))}.")
                return explanation

            # Add explanation for visiting this node
            if current_node not in self.graph:
                explanation.append(f"Node {current_node} is not connected anywhere, so we skip it.")
                continue

            explanation.append(f"From node {path[-1] if len(path) > 1 else start},"+plural+" we can visit nodes "
                                f"{', '.join(str(neighbor) for neighbor, _ in self.graph[current_node])} because the edges "
                                f"{', '.join(f'{current_node},{neighbor},{weight}' for neighbor, weight in self.graph[current_node])} "
                                f"are in the graph.")
            plural = " the node with the smallest length in the queue,"

            # Check if we reached the target node

            not_updated = []
            for neighbor, weight in self.graph.get(current_node, []):
                distance = current_distance + weight
                if distance < distances[neighbor]:
                    distances[neighbor] = distance
                    heapq.heappush(priority_queue, (distance, neighbor, path))

                    explanation.append(f"Updating distance for node {neighbor}: path {' -> '.join(map(str, path))} -> {neighbor}, "
                                        f"distance = {distance-weight} + {weight} = {distance}.")
                else :
                    not_updated.append(neighbor)
            if not_updated:
                explanation.append(f"The distance to nodes {', '.join(str(neighbor) for neighbor in not_updated)} is already lower through another path, so we don't update it.")
            # explanation.append("The queue is now: " + str(priority_queue))
        explanation.append("We ran out of nodes in the queue; hence, no path exists to the target node.")
        return explanation

class Solution():
    def __init__(self,graph_id):
        self.graph_id = graph_id
        self.metadata = self.get_metadata()

    def get_metadata(self):
        metadata_file_path = f"{os.getenv('PATH_TO_DATA')}/metadata.csv"
        with open(metadata_file_path, mode='r', newline='', encoding='utf-8') as file:
            csv_reader = csv.DictReader(file)
            for row in csv_reader:
                if row['graph_id'] == str(self.graph_id):
                    return row
        return None

    def get(self):
        start = 0
        target = int(self.metadata['num_nodes']) - 1
        explanation = Explainer(self.graph_id).shortest_path_explanation(start,target)
        explanation_str = "\n".join(explanation)
        return explanation_str

class Question():
    def __init__(self,graph_id):
        self.graph_id = graph_id
        self.metadata = self.get_metadata()
        self.graph = self.get_graph_data(self.graph_id)

    def get_graph_data(self,graph_id: int):
        graph_file_path = f"{os.getenv('PATH_TO_DATA')}/graphs/graph_{graph_id}.csv"
        graph_data = ""
        with open(graph_file_path, mode='r', newline='', encoding='utf-8') as file:
            csv_reader = csv.reader(file)
            next(csv_reader)
            for row in csv_reader:
                graph_data += " ".join(row) + "\n"
        return graph_data

    def get_metadata(self):
        metadata_file_path = f"{os.getenv('PATH_TO_DATA')}/metadata.csv"
        with open(metadata_file_path, mode='r', newline='', encoding='utf-8') as file:
            csv_reader = csv.DictReader(file)
            for row in csv_reader:
                if row['graph_id'] == str(self.graph_id):
                    return row
        return None

    def get(self):
        start = 0
        target = int(self.metadata['num_nodes']) - 1
        question = f"Find a path from node {start} to node {target} in the following graph:\n{self.graph}"
        return question

if __name__ == "__main__":
    question = Question(961)
    print(f"""Question: {Question(graph_id=72).get()}Solution: {Solution(graph_id=72).get()}
          \n\nQuestion: {Question(graph_id=70).get()}Solution: {Solution(graph_id=70).get()}
          \n\nQuestion: {Question(graph_id=71).get()}""")
import csv
import pandas as pd
import networkx as nx
import numpy as np
import sys
import matplotlib.pyplot as plt
import os

class DataHandler():
    def __init__(self,file_path):
        self.file_path = file_path
        self.data_responses = self.read_csv_to_dict(file_path)
        self.expected_results = self.get_metadata()
        self.data_graded_responses = {}

    def read_csv_to_dict(self,file_path):
        result_dict = {}
        # csv.field_size_limit(10**7)
        with open(file_path, mode='r', newline='', encoding='utf-8') as csvfile:
            reader = csv.reader(csvfile)
            next(reader)  # Skip the header row
            for row in reader:
                graph_id = int(row[0])
                if graph_id>=2800:
                    break
                response = row[1]
                result_dict[graph_id] = self.clean_data(response)
        return result_dict

    def get_metadata(self):
        metadata_file_path = f"{os.getenv('PATH_TO_DATA')}/metadata.csv"
        expected_results = {}
        with open(metadata_file_path, mode='r', newline='', encoding='utf-8') as file:
            csv_reader = csv.DictReader(file)
            for row in csv_reader:
                expected_results[int(row["graph_id"])] = (int(row['num_nodes']),row["dijkstra path"],row["dijkstra length"])
        return expected_results

    def clean_data(self,response):
        original_data = response.split("\n")
        clean = []
        for item in original_data:
            if item:
                cleaned_item = item.replace("'", "")
                first_item = cleaned_item.split(' -> ')[0]
                if first_item.isdigit():
                    # Convert the list of numbers from strings to integers
                    numbers = [int(x.strip(" ")) if x.strip(" ").isdigit() else x for x in cleaned_item.split("->")]
                    clean.append(numbers)

                else:
                    # Keep the comment as it is
                    comment = cleaned_item.strip(" ")
                    if comment == "or No path found":
                        comment="No path found"
                    if comment != "or":
                        clean.append(comment)
        return clean

    def grade_trayectories(self,graph_id):

        llm_results = self.data_responses[graph_id]
        expected = self.expected_results[graph_id]
        graded_responses = []
        for llm_result in llm_results:
            graded_responses.append((llm_result, self.grade_single_trayectory(graph_id, expected, llm_result)))
        self.data_graded_responses[graph_id]=graded_responses
        return graded_responses

    def grade_single_trayectory(self, graph_id, expected_result, llm_result):
        if expected_result[1] == "-" and llm_result == "No path found":
            return 2

        elif expected_result[1] == llm_result:
            return 1

        elif llm_result == "No path found":
            return 3

        elif isinstance(llm_result, str):
            print(f"{graph_id=}",llm_result)
            return "Comment"

        else:
            file_path = f"{os.getenv('PATH_TO_DATA')}/graphs/graph_{graph_id}.csv"

            df = pd.read_csv(file_path)

            G = nx.Graph()
            for _, row in df.iterrows():
                G.add_edge(row['Node1'], row['Node2'], weight=row['Weight'])

            # Check edges and compute results
            missing_edges = []
            path_length = 0

            for i in range(len(llm_result) - 1):
                u, v = llm_result[i], llm_result[i + 1]
                if G.has_edge(u, v):
                    path_length += G[u][v]['weight']
                else:
                    missing_edges.append((u, v))

            # Prepare the result
            miss_number = len(missing_edges)

            if llm_result[0]!=0:
                miss_number+=1
            if llm_result[0]!=expected_result[0]-1:
                miss_number+=1
            if miss_number>0:
                return -np.log(1+miss_number/len(expected_result[1]))
                # return -miss_number/len(expected_result[1])
            else:
                return 1 - (path_length-int(expected_result[2]))/int(expected_result[2])

    def run(self):
        results = {}
        for graph_id in self.data_responses.keys():
            results[graph_id] = [x[1] for x in self.grade_trayectories(graph_id) if isinstance(x[1], (int, float))]

        flattened_data = [(key, val) for key, values in results.items() for val in values]
        keys, values = zip(*flattened_data)

        colors = ['red' if val == 2 and len(results[key]) == 1 else 'blue' for key,val in zip(keys,values)]

        plt.figure(figsize=(10, 6), dpi=600)  # Increase the dpi for higher resolution
        plt.scatter(keys, values, c=colors, alpha=0.7)
        plt.xlabel('graph_id', fontsize=14)
        plt.ylabel('score', fontsize=14)
        node_list = [5,10,15,20,50]
        total_size = len(node_list)*560
        # Add shaded regions to represent different graph sizes
        for i in range(0, total_size, 560):
            plt.axvspan(i, i + 560, color=plt.cm.viridis(i / total_size), alpha=0.3)
            plt.text(i + 280, min(values) *0.85, f'{node_list[i//560]} nodes', rotation=90, verticalalignment='center',fontsize=14)
        plt.axhline(y=1, color='red', linestyle='--')

        plt.title('Zero-shot testing results', fontsize=16)
        plt.grid(True)
        # plt.show()

        # Save the figure with the name of the file it comes from
        file_name = self.file_path.split('/')[-1].split('\\')[-1].split('.')[0]
        print(file_name)
        plt.savefig(f"{os.getenv('PATH_TO_DATA')}/results/{file_name}_scatter_plot.png")

        return results



if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python testing_protocol.py <file_path>")
        sys.exit(1)

    file_path = sys.argv[1]
    data_handler = DataHandler(file_path)
    data_handler.run()


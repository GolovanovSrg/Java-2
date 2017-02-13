import numpy as np
import pandas as pd
import matplotlib.pylab as plt
from os import listdir
from os.path import isfile, join

folder_paths = [("./change_array_size", " array_size"), ("./change_delta_time", " time_between_requests"), ("./change_n_clients", " n_clients")]
metric_names = [" array_processing_time", " request_processing_time", " client_time"]

for path, x_name in folder_paths:
    for metric in metric_names:
        archs = []
        file_names = [f for f in listdir(path) if isfile(join(path, f))]
        for file_name in file_names:
            file_path = join(path, file_name)
            data = pd.read_csv(file_path)
            archs += [data.ix[0, "architecture"]]
            plt.plot(data[x_name], data[metric])

        plt.legend(archs, loc=2)
        plt.title(metric + "(change " + x_name + ")")
        plt.show()
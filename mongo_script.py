'''
Created on 17 giu 2021

@author: lorenzo
'''

from pymongo import MongoClient
import pprint, pandas

#ADDRESS="localhost"
#PORT="27017"
ADDRESS="infra.licit.local"
PORT="31184"
DB="promenade"
COLLECTION="areas"
INPUT_FILE="areas_full_with_nodes.csv"
DELIMITER=","

'''*** Reading input file***'''
df = pandas.read_csv(INPUT_FILE, delimiter=DELIMITER, dtype=str)
df

'''*** Connecting to mongo instance***'''
client = MongoClient("mongodb://"+ ADDRESS + ":" + PORT + "/")
db = client[DB]
areas = db[COLLECTION]

'''*** Adding new documents***'''

for i in range(0, len(df.index)):
    print(f'Area {i}/{len(df.index)}')
    line_area = df['GEOMETRY'][i][10:-2]
    coordinates_list = []
    line_area_splitted = line_area.split(',')
    for pair in line_area_splitted:
        coordinates = pair.strip().split(' ')
        coordinates_list.append([float(coordinates[0]),float(coordinates[1])])
    area = {"insee_com": int(df['INSEE_COM'][i]), "nom_com": df['NOM_COM'][i], "polygon": { "type": "Polygon", "coordinates":[coordinates_list]}, "nodes": int(df['nodes'][i])}
    areas.insert_one(area)


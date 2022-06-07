package data.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Sorts;
import data.BetweennessDAO;
import data.NormStrategy;
import data.model.Area;
import data.model.Intersection;
import org.bson.Document;
import util.ConfigurationSingleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class MongoBetweennesDAO implements BetweennessDAO {
    public static final long START_REF_EPOCH = 1536184800;
    public static final long BETWEENNESS_CALCUALTION_DELTA = 900;

    ConfigurationSingleton conf = ConfigurationSingleton.getInstance();
    private final MongoConnectionManager connectionManager;

    public MongoBetweennesDAO(MongoConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ArrayList<ArrayList<Intersection>> updateBetweennessValue(ArrayList<ArrayList<Intersection>> intersectionsList, Long endTimestamp, String normStrategy, HashMap<String, Area> allAreas) {

        HashMap<String, Double> maxValues = new HashMap<>();
        HashMap<String, HashMap<Long, Intersection>> intersectionMapCollection = new HashMap<>();
        for (ArrayList<Intersection> intersections : intersectionsList) {
            HashMap<Long, Intersection> intersectionMap = new HashMap<>();
            String areaName = intersections.get(0).getAreaName();
            for (Intersection i : intersections) {
                intersectionMap.put(i.getOsmid(), i);
            }
            intersectionMapCollection.put(areaName, intersectionMap);
            MongoCollection<Document> collection = connectionManager.getDatabase().getCollection(areaName + "-BC");
            int n = intersections.size();
            int skip = (int) ((((endTimestamp - START_REF_EPOCH) / BETWEENNESS_CALCUALTION_DELTA) - 1) * n);
            FindIterable<Document> iterable = collection.find().sort(Sorts.ascending("_id")).skip(skip).limit(n);
//            documentsMap.put(areaName, iterable);
            double max = -1;
            for (Document d : iterable) {
                double bc = Double.parseDouble(d.getString("bc"));
                if (bc > max) {
                    max = bc;
                }
                Long osmid = d.getLong("node");

                intersectionMap.get(osmid).setBetweenness(bc);
                System.out.println("d = " + d);
            }
            maxValues.put(areaName, max);
        }
        double normFactor = -1;

        switch (normStrategy) {
            case NormStrategy.AREA:
                for (String areaname : intersectionMapCollection.keySet()) {
                    normFactor = maxValues.get(areaname);
                    HashMap<Long, Intersection> intersectionsMap = intersectionMapCollection.get(areaname);
                    for (Long k : intersectionsMap.keySet()) {
                        Intersection i = intersectionsMap.get(k);
                        i.setBetweenness(i.getBetweenness() / normFactor);
                    }
                }
                break;
            case NormStrategy.VIEWPORT:
                normFactor = Collections.max(maxValues.values());
                for (String areaname : intersectionMapCollection.keySet()) {
                    HashMap<Long, Intersection> intersectionsMap = intersectionMapCollection.get(areaname);
                    for (Long k : intersectionsMap.keySet()) {
                        Intersection i = intersectionsMap.get(k);
                        i.setBetweenness(i.getBetweenness() / normFactor);
                    }
                }
                break;
            case NormStrategy.GLOBAL:
                MongoIterable<String> list = connectionManager.getDatabase().listCollectionNames();
                for (String name : list) {
                    if (name.endsWith("-BC")) {
                        MongoCollection<Document> collection = connectionManager.getDatabase().getCollection(name);
                        int n = allAreas.get(name.substring(0, name.length()-3)).getNodes();
                        int skip = (int) ((((endTimestamp - START_REF_EPOCH) / BETWEENNESS_CALCUALTION_DELTA) - 1) * n);
                        FindIterable<Document> iterable = collection.find().sort(Sorts.ascending("_id")).skip(skip).limit(n);
                        for (Document d : iterable) {
                            double bc = Double.parseDouble(d.getString("bc"));
                            if (bc > normFactor) {
                                normFactor = bc;
                            }
                        }
                    }
                }
                for (String areaname : intersectionMapCollection.keySet()) {
                    HashMap<Long, Intersection> intersectionsMap = intersectionMapCollection.get(areaname);
                    for (Long k : intersectionsMap.keySet()) {
                        Intersection i = intersectionsMap.get(k);
                        i.setBetweenness(i.getBetweenness() / normFactor);
                    }
                }
                break;
            default:
                return null;
        }

        ArrayList<ArrayList<Intersection>> result = new ArrayList<>();

        for (String areaname : intersectionMapCollection.keySet()) {
            HashMap<Long, Intersection> map = intersectionMapCollection.get(areaname);
            ArrayList<Intersection> intersections = new ArrayList<>(map.values());
            result.add(intersections);
        }

        return result;
    }


}

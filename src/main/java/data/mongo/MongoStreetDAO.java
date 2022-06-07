package data.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import data.model.Street;
import org.bson.Document;
import org.bson.conversions.Bson;
import util.ConfigurationSingleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;


public class MongoStreetDAO {

    ConfigurationSingleton conf = ConfigurationSingleton.getInstance();
    private final MongoConnectionManager connectionManager;

    public MongoStreetDAO(MongoConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ArrayList<Street> updateWeightValue(ArrayList<Street> streets, String areaName, Long endTimestamp) {
        HashMap<Long, Street> streetHashMap = new HashMap<>();
        for (Street s : streets) {
            streetHashMap.put(s.getLinkId(), s);
        }
        MongoCollection<Document> collection = connectionManager.getDatabase().getCollection(areaName + "-Northbound");

        Bson match = match(and(gt("domainAggTimestamp", (endTimestamp - MongoParams.TRAVELTIME_CALCUALTION_DELTA) * 1000),
                lt("domainAggTimestamp", endTimestamp * 1000)));
        Bson project = project(fields(excludeId(), include("linkid", "avgTravelTime")));
        Bson group = group("$linkid", avg("aggregatedTT", "$avgTravelTime"));


        AggregateIterable<Document> aggregate = collection.aggregate(Arrays.asList(match, project, group));
//                .into(new ArrayList<>());

//        BasicDBObject getQuery = new BasicDBObject();
//        getQuery.put("domainAggTimestamp", new BasicDBObject("$gt", (endTimestamp - MongoParams.TRAVELTIME_CALCUALTION_DELTA) * 1000).append("$lt", endTimestamp * 1000));
//        FindIterable<Document> documents = collection.find(getQuery);
        for (Document d : aggregate) {
            Long linkid = Long.valueOf(d.getString("_id"));
//            Double tt = d.getDouble("avgTravelTime");
            Double tt = d.getDouble("aggregatedTT");
            Street s = streetHashMap.get(linkid);
            if(s != null){
                s.setWeight(tt);
            }
        }

        return new ArrayList<>(streetHashMap.values());
    }
}

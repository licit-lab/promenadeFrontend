package data.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Polygon;
import com.mongodb.client.model.geojson.Position;
import data.AreaDAO;
import data.model.Area;
import util.ConfigurationSingleton;

import java.util.ArrayList;
import java.util.HashMap;

public class MongoAreaDAO implements AreaDAO {

    ConfigurationSingleton conf = ConfigurationSingleton.getInstance();
    private final MongoConnectionManager connectionManager;

    public MongoAreaDAO(MongoConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ArrayList<Area> findAreasfromCorners(float upperLeftLon, float upperLeftLat, float lowerRightLon, float lowerRightLat) {
        ArrayList<Position> positions = new ArrayList<>();
        positions.add(new Position(upperLeftLon, upperLeftLat));
        positions.add(new Position(lowerRightLon, upperLeftLat));
        positions.add(new Position(lowerRightLon, lowerRightLat));
        positions.add(new Position(upperLeftLon, lowerRightLat));
        positions.add(new Position(upperLeftLon, upperLeftLat));

        Polygon polygon = new Polygon(positions);

        FindIterable<Area> result = connectionManager.getCollection(conf.getProperty("mongo.collection.areas.name"), Area.class).find(Filters.geoIntersects("polygon", polygon));
        ArrayList<Area> areas = new ArrayList<>();
        for(Area a : result){
            areas.add(a);
        }
        return areas;
    }

    public HashMap<String, Area> getAllAreas(){
        FindIterable<Area> result = connectionManager.getCollection(conf.getProperty("mongo.collection.areas.name"), Area.class).find();
        HashMap<String, Area> areas = new HashMap<>();
        for(Area a : result){
            areas.put(a.getNom_com(), a);
        }
        return areas;
    }

}

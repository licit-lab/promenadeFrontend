package logic.areas;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;

import data.mongo.MongoAreaDAO;
import data.model.Area;
import data.mongo.MongoConnectionManager;
import util.ConfigurationSingleton;

@RequestScoped
public class AreaLogic implements AreaLogicLocal {
    ConfigurationSingleton conf = ConfigurationSingleton.getInstance();
    MongoConnectionManager connectionManager;
    MongoAreaDAO areaDB;
    @PostConstruct
    public void init() {
        String hostname = conf.getProperty("mongo.hostname");
        String port = conf.getProperty("mongo.port");
        String database = conf.getProperty("mongo.database.areas.name");
        connectionManager = new MongoConnectionManager(hostname, port, database);
        areaDB = new MongoAreaDAO(connectionManager);
    }

    @Override
    public ArrayList<Area> getAreaFromCorners(float upperLeftLon, float upperLeftLat, float lowerRightLon, float lowerRightLat) {

        ArrayList<Area> result = areaDB.findAreasfromCorners(upperLeftLon, upperLeftLat, lowerRightLon, lowerRightLat);
        return result;
    }

    @PreDestroy
    public void preDestroy() {
        connectionManager.close();
    }
}

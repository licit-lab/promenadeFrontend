package logic.intersections;

import data.NormStrategy;
import data.model.Area;
import data.model.Intersection;
import data.mongo.MongoAreaDAO;
import data.mongo.MongoBetweennesDAO;
import data.mongo.MongoConnectionManager;
import data.neo4j.Neo4jRoadNetworkDAO;
import logic.streets.StreetsLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ConfigurationSingleton;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.HashMap;

@RequestScoped
public class IntersectionLogic implements IntersectionLogicLocal{
    ConfigurationSingleton conf = ConfigurationSingleton.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(StreetsLogic.class);
    Neo4jRoadNetworkDAO graphDatabase;

    private MongoBetweennesDAO betweennessDB;

    private MongoAreaDAO areaDB;

    @PostConstruct
    public void init() {
        String uri = conf.getProperty("neo4j-core.bolt-uri");
        String user = conf.getProperty("neo4j-core.user");
        String password = conf.getProperty("neo4j-core.password");
        graphDatabase = new Neo4jRoadNetworkDAO(uri, user, password);
        logger.info("TrafficMonitoringService.connect");
        graphDatabase.openConnection();

        String mongoHostname = conf.getProperty("mongo.hostname");
        String mongoPort = conf.getProperty("mongo.port");
        String MongoBCDatabase = conf.getProperty("mongo.database.bc.name");
        betweennessDB = new MongoBetweennesDAO(new MongoConnectionManager(mongoHostname, mongoPort, MongoBCDatabase));

        String MongoAreaDatabase = conf.getProperty("mongo.database.areas.name");
        areaDB = new MongoAreaDAO(new MongoConnectionManager(mongoHostname, mongoPort, MongoAreaDatabase));
    }
    @Override
    public ArrayList<Intersection> getTopCritical(int topK) {
        return graphDatabase.getTopCriticalNodes(topK);
    }

    @Override
    public ArrayList<Intersection> getIntersectionsByArea(String areaName) {
        return graphDatabase.getIntersectionsFromArea(areaName);
    }
    @Override
    public ArrayList<ArrayList<Intersection>> updateBetweennessValue(ArrayList<ArrayList<Intersection>> intersections, Long endTimestamp, String normStrategy){
        HashMap<String, Area> allAreas = null;
        if(normStrategy.equals(NormStrategy.GLOBAL)) {
            allAreas = areaDB.getAllAreas();
        }
        return betweennessDB.updateBetweennessValue(intersections, endTimestamp, normStrategy, allAreas);
    }
}

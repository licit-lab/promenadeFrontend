package logic.streets;

import data.model.Coordinate;
import data.model.Street;
import data.mongo.MongoAreaDAO;
import data.mongo.MongoConnectionManager;
import data.mongo.MongoStreetDAO;
import data.neo4j.Neo4jRoadNetworkDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ConfigurationSingleton;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.Set;

@RequestScoped
public class StreetsLogic implements StreetsLogicLocal {
    ConfigurationSingleton conf = ConfigurationSingleton.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(StreetsLogic.class);
    Neo4jRoadNetworkDAO database;
    MongoConnectionManager connectionManager;
    MongoStreetDAO streetDAO;

//    public RoadNetworkLogic(){
//        String uri = conf.getProperty("neo4j-core.bolt-uri");
//        String user = conf.getProperty("neo4j-core.user");
//        String password = conf.getProperty("neo4j-core.password");
//    }


    @PostConstruct
    public void init() {
        String uri = conf.getProperty("neo4j-core.bolt-uri");
        String user = conf.getProperty("neo4j-core.user");
        String password = conf.getProperty("neo4j-core.password");
        database = new Neo4jRoadNetworkDAO(uri, user, password);
        logger.info("TrafficMonitoringService.connect");
        database.openConnection();
        String hostname = conf.getProperty("mongo.hostname");
        String port = conf.getProperty("mongo.port");
        String database = conf.getProperty("mongo.database.streets.name");
        connectionManager = new MongoConnectionManager(hostname, port, database);
        streetDAO = new MongoStreetDAO(connectionManager);
    }

//    /**
//     * Called after the EJB construction.
//     * Open the connection to the database.
//     */
//    @PostConstruct
//    public void connect() {
//        logger.info("TrafficMonitoringService.connect");
//        database.openConnection();
//    }

    /**
     * Called before the EJB destruction.
     * Close the connection to the database.
     */
    @PreDestroy
    public void preDestroy() {
        logger.info("TrafficMonitoringService.preDestroy");
        database.closeConnection();
    }

    @Override
    public ArrayList<Street> getStreetsFromArea(String areaname, int zoom, int decimateSkip, String epochTimestamp) {

        ArrayList<Street> streets = database.getStreetsFromArea(areaname, zoom);
        if (decimateSkip > 0) {
            decimateStreets(streets, decimateSkip);
        }
        if (epochTimestamp == null) {
            for(Street s : streets){
                s.setWeight(s.getFftt());
            }
        }else{
            Long endTimestamp = Long.parseLong(epochTimestamp);
            streetDAO.updateWeightValue(streets, areaname, endTimestamp);
        }
        return streets;
    }

    @Override
    public ArrayList<Street> getStreetsFromLinkIds(Set<Long> streetIdsSet) {
        return database.getStreetsFromLinkIds(streetIdsSet);
    }

    @Override
    public void decimateStreets(ArrayList<Street> streets, int decimateSkip) {
        for (Street s : streets) {
            ArrayList<Coordinate> geometry = s.getGeometry();
            ArrayList<Coordinate> geometryFiltered = new ArrayList<>();
            int geometrySize = geometry.size();
            geometryFiltered.add(geometry.get(0));
            for (int i = 1; i < geometrySize-1; i = i + decimateSkip) {
                geometryFiltered.add(geometry.get(i));
            }
            geometryFiltered.add(geometry.get(geometrySize-1));
            s.setGeometry(geometryFiltered);
        }
    }
}

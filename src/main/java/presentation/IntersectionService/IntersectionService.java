package presentation.IntersectionService;

import data.model.Intersection;
import data.mongo.MongoAreaDAO;
import data.mongo.MongoConnectionManager;
import logic.intersections.IntersectionLogicLocal;
import presentation.MyResposeBuilder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("/intersections")
public class IntersectionService implements IntersectionServiceApi{
    //TODO change DAO
    @Inject
    private IntersectionLogicLocal intersectionLogic;




    @Override
    public Response topCritical(int topK) {
        return MyResposeBuilder.createResponse(Response.Status.OK, intersectionLogic.getTopCritical(topK));
    }

    @Override
    public Response getNodeBetweennessByArea(String areas, Long endTimestamp, String normStrategy) {
        if(endTimestamp == 0){
            return MyResposeBuilder.createResponse(Response.Status.BAD_REQUEST, "Malformed request");
        }

        String[] areasSplitted = areas.split(",");
        ArrayList<ArrayList<Intersection>> intersectionsByAreaList = new ArrayList<>();
        for (String a: areasSplitted){
            ArrayList<Intersection> i = intersectionLogic.getIntersectionsByArea(a);
            intersectionsByAreaList.add(i);
        }

        intersectionLogic.updateBetweennessValue(intersectionsByAreaList, endTimestamp, normStrategy);

        return MyResposeBuilder.createResponse(Response.Status.OK, intersectionsByAreaList);

    }


}

package presentation.AreaService;


import data.model.Area;
import logic.areas.AreaLogicLocal;
import mil.nga.sf.geojson.Feature;
import mil.nga.sf.geojson.FeatureCollection;
import mil.nga.sf.geojson.Polygon;
import mil.nga.sf.geojson.Position;
import presentation.MyResposeBuilder;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/areas")
public class AreaService implements AreaServiceAPI {

    @Inject
    private AreaLogicLocal areaLogic;



    public Response getAreaFromCorners(String mode, String upperLeft, String lowerRight) {

        if (upperLeft != null && lowerRight != null) {
            //TODO: check strings
            String[] upperLeftSplitted = upperLeft.split(",");
            float upperLeftLon = Float.parseFloat(upperLeftSplitted[1].trim());
            float upperLeftLat = Float.parseFloat(upperLeftSplitted[0].trim());

            String[] lowerRightSplitted = lowerRight.split(",");
            float lowerRightLon = Float.parseFloat(lowerRightSplitted[1].trim());
            float lowerRightLat = Float.parseFloat(lowerRightSplitted[0].trim());

            ArrayList<Area> areas =  areaLogic.getAreaFromCorners(upperLeftLon, upperLeftLat, lowerRightLon, lowerRightLat);

            switch (mode){
                case "name":
                    ArrayList<String> areaNames = new ArrayList<>();
                    for (Area a : areas)
                        areaNames.add(a.getNom_com());
                    return MyResposeBuilder.createResponse(Response.Status.OK, areaNames);
                case "geojson":
                    ArrayList<Feature> features = new ArrayList<>();
                    for (Area a : areas){
                        List<List<Position>> coordinates = new ArrayList<>();
                        List<Position> positions = new ArrayList<>();
                        coordinates.add(positions);
                        List<com.mongodb.client.model.geojson.Position> exterior = a.getPolygon().getExterior();
                        for(com.mongodb.client.model.geojson.Position pos : exterior){
                            List<Double> coord = pos.getValues();
                            positions.add(new Position(coord.get(0), coord.get(1)));
                        }
                        Feature f = new Feature(new Polygon(coordinates));
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("name", a.getNom_com());
                        f.setProperties(properties);
                        features.add(f);
                    }
                    return MyResposeBuilder.createResponse(Response.Status.OK, new FeatureCollection(features));
                default:
                    return MyResposeBuilder.createResponse(Response.Status.BAD_REQUEST);

            }
        }
        return MyResposeBuilder.createResponse(Response.Status.BAD_REQUEST);
    }


}

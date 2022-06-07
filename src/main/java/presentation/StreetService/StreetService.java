package presentation.StreetService;

import data.model.Coordinate;
import data.model.Street;
import logic.streets.StreetsLogic;
import mil.nga.sf.geojson.*;
import presentation.MyResposeBuilder;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/streets")
public class StreetService implements StreetServiceAPI{
    @Inject
    private StreetsLogic streetsLogic;


    @Override
    public Response searchStreets(String areaname, int zoom, String type, int decimateSkip, String upperLeft, String lowerRight) {
        if (upperLeft == null && lowerRight == null) {
            ArrayList<Street> streetsFromArea = streetsLogic.getStreetsFromArea(areaname, zoom, decimateSkip);
            if (type.equals("geojson")) {
                FeatureCollection featureCollection = convertStreetsToFeatureCollection(streetsFromArea);
                return MyResposeBuilder.createResponse(Response.Status.OK, featureCollection);
            }

            return MyResposeBuilder.createResponse(Response.Status.OK, streetsFromArea);
        } else if (upperLeft != null && lowerRight != null) {
            String[] upperLeftSplitted = upperLeft.split(",");
            float upperLeftLon = Float.parseFloat(upperLeftSplitted[1].trim());
            float upperLeftLat = Float.parseFloat(upperLeftSplitted[0].trim());

            String[] lowerRightSplitted = lowerRight.split(",");
            float lowerRightLon = Float.parseFloat(lowerRightSplitted[1].trim());
            float lowerRightLat = Float.parseFloat(lowerRightSplitted[0].trim());
            ArrayList<Street> streetsFromArea = streetsLogic.getStreetsFromArea(areaname, zoom, decimateSkip);
            streetsFromArea = filterStreets(streetsFromArea, upperLeftLon, upperLeftLat, lowerRightLon, lowerRightLat);

            if (type.equals("geojson")) {
                FeatureCollection featureCollection = convertStreetsToFeatureCollection(streetsFromArea);
                return MyResposeBuilder.createResponse(Response.Status.OK, featureCollection);
            }

            return MyResposeBuilder.createResponse(Response.Status.OK, streetsFromArea);
        } else {
            return MyResposeBuilder.createResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error");
        }
    }



    private ArrayList<Street> filterStreets(ArrayList<Street> streetsFromArea, float upperLeftLon, float upperLeftLat, float lowerRightLon, float lowerRightLat) {
        ArrayList<Street> filteredStreet = new ArrayList<>();
        for (Street s : streetsFromArea) {
            for (Coordinate c : s.getGeometry()) {
                if (c.getLatitude() >= lowerRightLat &&
                        c.getLatitude() <= upperLeftLat &&
                        c.getLongitude() >= upperLeftLon &&
                        c.getLongitude() <= lowerRightLon) {
                    filteredStreet.add(s);
                    break;
                }
            }
        }
        return filteredStreet;
    }

    @Override
    public Response getStreetsFromLinkIds(String ids) {
        Set<Long> streetIds = new HashSet<>();
        String[] splitted = ids.split(",");
        for (String s : splitted) {
            streetIds.add(Long.valueOf(s));
        }
        return MyResposeBuilder.createResponse(Response.Status.OK, streetsLogic.getStreetsFromLinkIds(streetIds));
    }


    public FeatureCollection convertStreetsToFeatureCollection(ArrayList<Street> streets) {
        FeatureCollection featureCollection = new FeatureCollection();
        for (Street s : streets) {
            ArrayList<Position> positions = new ArrayList<>();
            for (Coordinate c : s.getGeometry()) {
                Position p = new Position(c.getLongitude(), c.getLatitude());
                positions.add(p);
            }
//			"properties": {
//				"name": "FRMT-DALY (ROUTE 5/6)",
//						"color": "#4db848"
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", s.getName());
            properties.put("id", s.getLinkId());
            double fftt = s.getLenght() / s.getFfs();
            properties.put("fftt", fftt);
//            properties.put("weight", fftt + ((Math.random() - 0.5) * 4 * fftt));
            properties.put("weight", 0);
//            if (s.getFfs() > 20)
//                properties.put("color", "#1199dd");
//            else {
//                properties.put("color", "#d21f1b");
//            }
//            properties.put("color", "#fc0040");

            Feature feature = new Feature(new LineString(positions));
            feature.setProperties(properties);
            featureCollection.addFeature(feature);
        }
        return featureCollection;
    }


}

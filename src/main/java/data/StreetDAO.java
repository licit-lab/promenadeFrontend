package data;

import data.model.Coordinate;
import data.model.Street;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public interface StreetDAO {
    Street getStreet(long osmidS, long osmidD);

    Street getStreet(long id);

    ArrayList<Street> getStreetsFromLinkIds(Set<Long> streetIdsSet);

    ArrayList<Street> getStreetsFromArea(String areaname, int zoom);

    ArrayList<Coordinate> getStreetGeometry(long osmidS, long osmidD);

    HashMap<Integer, Street> getStreets(long osmid);
}

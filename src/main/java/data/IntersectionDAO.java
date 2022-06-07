package data;

import data.model.Coordinate;
import data.model.Intersection;

public interface IntersectionDAO {
    Intersection getIntersection(long osmid);

    Intersection getIntersectionLight(long osmid);

    Intersection getNearestIntersection(Coordinate position);
}

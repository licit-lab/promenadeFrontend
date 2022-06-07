package presentation.IntersectionService;

import data.NormStrategy;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface IntersectionServiceApi {

    @GET
    @Path("/topCritical")
    Response topCritical(@DefaultValue("-1") @QueryParam("top") int topK);

    @GET
    @Path("/betweenness")
    Response getNodeBetweennessByArea(@QueryParam("areas") String areas,
                                      @QueryParam("endTimestamp") @DefaultValue("0") Long endTimestamp,
                                      @QueryParam("normStrategy") @DefaultValue(NormStrategy.AREA) String normStrategy);
}

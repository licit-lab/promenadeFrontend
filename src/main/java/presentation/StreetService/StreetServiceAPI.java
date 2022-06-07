package presentation.StreetService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface StreetServiceAPI {
    @GET
    @Path("/")
    public Response searchStreets(@QueryParam("areaname") String areaname,
                                  @QueryParam("zoom") int zoom,
                                  @DefaultValue ("street") @QueryParam("type") String type,
                                  @DefaultValue("0") @QueryParam("decimateSkip") int decimateSkip,
                                  @QueryParam("upperLeft") String upperLeft,
                                  @QueryParam("lowerRight") String lowerRight,
                                  @QueryParam("timestamp") String epochTimestamp);




    @GET
    @Path("/search")
    Response getStreetsFromLinkIds(@QueryParam("ids") String ids);}

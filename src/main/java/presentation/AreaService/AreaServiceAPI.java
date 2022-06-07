package presentation.AreaService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AreaServiceAPI {

    @GET
    @Path("/")
    public Response getAreaFromCorners(
            @DefaultValue("name") @QueryParam("mode") String mode,
    		@QueryParam("upperLeft") String upperLeft,
            @QueryParam("lowerRight") String lowerRight
    );


}

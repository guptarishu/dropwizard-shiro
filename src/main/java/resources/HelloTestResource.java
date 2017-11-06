package resources;

import core.User;
import io.dropwizard.auth.Auth;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/test")
public class HelloTestResource {

    @GET
    @Path("protect")
    @RequiresPermissions("investigator:view")
    public String resource1(@Auth User user) {
        return "ok2";
    }
}

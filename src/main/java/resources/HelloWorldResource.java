package resources;

import config.HelloWorldConfiguration;
import core.User;
import io.dropwizard.auth.Auth;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;


@Path("/helloworld")
public class HelloWorldResource {
    private static final transient Logger log = LoggerFactory.getLogger(HelloWorldResource.class);

    private final HelloWorldConfiguration helloWorldConfiguration;

    public HelloWorldResource(HelloWorldConfiguration helloWorldConfiguration) {
        this.helloWorldConfiguration = helloWorldConfiguration;
    }

    @GET
    @Path("protect")
    public String protectedByRequiresPermissions(@Auth User user) {

//        if (user.hasRole("analyst")) {
//            log.info("May the analyst be with you!");
//        } else {
//            log.info("Hello, mere mortal.");
//        }
//
//        if (user.hasRole("supervisor")) {
//            log.info("May the supervisor be with you!");
//        } else {
//            log.info("Hello, mere mortal.");
//        }

        //test a typed permission (not instance-level)
        if (user.isPermitted("usermanagement:view")) {
            log.info("You may view a usermanagement page only.");
        } else {
            log.info("Hello, mere mortal.");
        }

        //test a typed permission (not instance-level)
        if (user.isPermitted("usermanagement:update")) {
            log.info("Can update usermanagement page");
        } else {
            log.info("Hello, mere mortal.");
        }

        //a (very powerful) Instance Level permission:
        if (user.isPermitted("usermanagement:create")) {
            log.info("You may view only xyz in usermanagement page");
        } else {
            log.info("Hello, mere mortal");
        }

        return "ok";
    }

    @GET
    @Path("protect1")
    @RequiresRoles("analyst")
    public String protectedByRequiresPermissions1(@Auth User user) {
        return "ok1";
    }

    @GET
    @Path("protect2")
    @RequiresPermissions("usermanagement:update")
    public String protectedByRequiresPermissions2(@Auth User user) {
        return "ok2";
    }

    @GET
    @Path("protect3")
    @RequiresPermissions("usermanagement:create")
    public String protectedByRequiresPermissions3(@Auth User user) {
        return "ok3";
    }

    @GET
    @Path("protect4")
    @RequiresPermissions("usermanagement:view")
    public String protectedByRequiresPermissions4(@Auth User user) {
        return "ok4";
    }
}

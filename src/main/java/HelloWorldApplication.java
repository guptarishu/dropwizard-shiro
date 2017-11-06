import auth.ExampleAuthenticator;
import config.HelloWorldConfiguration;
import core.User;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.session.SessionHandler;
import org.secnod.dropwizard.shiro.ShiroBundle;
import org.secnod.dropwizard.shiro.ShiroConfiguration;
import org.secnod.shiro.jaxrs.ShiroExceptionMapper;
import resources.HelloWorldResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {

    private static final transient Logger log = LoggerFactory.getLogger(HelloWorldApplication.class);
        private final ShiroBundle<HelloWorldConfiguration> shiro = new ShiroBundle<HelloWorldConfiguration>() {
            @Override
            protected ShiroConfiguration narrow(HelloWorldConfiguration configuration) {
                return configuration.getShiroConfiguration();
        }
    };

    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        // nothing to do yet
        bootstrap.addBundle(shiro);
    }

    @Override
    public void run(HelloWorldConfiguration configuration, Environment environment) {
        final HelloWorldResource resource = new HelloWorldResource(configuration);
        environment.jersey().register(resource);

        environment.getApplicationContext().setSessionHandler(new SessionHandler());
        environment.jersey().register(new ShiroExceptionMapper());

        environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                .setAuthenticator(new ExampleAuthenticator())
                .setRealm("SUPER SECRET STUFF")
                .buildAuthFilter()));
        environment.jersey().register(new AuthValueFactoryProvider.Binder(User.class));

    }

}

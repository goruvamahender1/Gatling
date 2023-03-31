package videogamedb.simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import net.sf.saxon.om.Chain;

import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.core.CoreDsl.*;
public class VideoGameDbSimulation extends Simulation{
    private HttpProtocolBuilder httpProtocol=http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");
    private static final int USER_COUNT=Integer.parseInt(System.getProperty("USER","5"));
    private static final int RAMP_DURATION=Integer.parseInt(System.getProperty("RAMP_DURATION","10"));
    private static final int USER_DURATION=Integer.parseInt(System.getProperty("USER_DURATION","20"));
    private static ChainBuilder getAllVideoGame=
            exec(http("Get All video games").get("/videogame"));
    private static ChainBuilder getSpecificGame=
            exec(http("Get specific game").get("/videogame/2"));

    @Override
    public void before(){
        System.out.printf("User count %d users%n",USER_COUNT);
        System.out.printf("Ramp users duration %d seconds%n",RAMP_DURATION);
        System.out.printf("Max duration %d seconds%n",USER_DURATION);

    }

    private ScenarioBuilder scn=scenario("VideoGame Db - Section code")
            .forever().on(
            exec(getAllVideoGame)
            .pause(5)
            .exec(getSpecificGame)
            .pause(5)
            .exec(getAllVideoGame)
            );
    {
        setUp(
                scn.injectOpen(
                        nothingFor(5),
                        rampUsers(USER_COUNT).during(RAMP_DURATION)

                ).protocols(httpProtocol)
        ).maxDuration(USER_DURATION);
    }
}

package videogamedb.scriptfundamentals;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
public class MyFirstTest extends Simulation {

    // Http configuration
    private HttpProtocolBuilder httpProtocol=http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json");

    // Scenario Defination
    private ScenarioBuilder scn=scenario("My First Test")
            .exec(http("Get all games")
                    .get("/videogame"));

    // Load Simulation
    {
        setUp(
                scn.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }
}

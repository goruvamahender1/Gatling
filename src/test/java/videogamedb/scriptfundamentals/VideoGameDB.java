package videogamedb.scriptfundamentals;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import net.sf.saxon.om.Chain;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.core.CoreDsl.*;
public class VideoGameDB extends Simulation{

    private HttpProtocolBuilder httpProtocol=http.baseUrl("https://www.videogamedb.uk/api").acceptHeader("application/json").
            contentTypeHeader("application/json");

    private static ChainBuilder authenticate=
            exec(http("authentication").post("/authenticate").body(StringBody("{\n" +
                    "  \"password\": \"admin\",\n" +
                    "  \"username\": \"admin\"\n" +
                    "}"))
                    .check(jmesPath("token").saveAs("jwtToken")));

    private static ChainBuilder createVideogame=
            exec(http("Create a video game").post("/videogame").header("Authorization","Bearer #{jwtToken}")
                    .body(StringBody("{\n" +
                    "  \"category\": \"Platform\",\n" +
                    "  \"name\": \"Mario\",\n" +
                    "  \"rating\": \"Mature\",\n" +
                    "  \"releaseDate\": \"2012-05-04\",\n" +
                    "  \"reviewScore\": 85\n" +
                    "}")));

    private ScenarioBuilder scn=scenario("video game Db - Section 5 code")
            .exec(authenticate)
            .exec(createVideogame);
    {
        setUp(
                scn.injectOpen(atOnceUsers(1))
        ).protocols(httpProtocol);
    }
}

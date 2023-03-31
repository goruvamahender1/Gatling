package videogamedb.Finalsimulation;

import io.gatling.javaapi.http.*;
import io.gatling.javaapi.core.*;
import net.sf.saxon.serialize.CharacterMapIndex;

import static io.gatling.javaapi.http.HttpDsl.*;
import static io.gatling.javaapi.core.CoreDsl.*;

public class FinalScript extends Simulation{

    //HttpProtocol Builder
    private HttpProtocolBuilder httpProtocol=http
            .baseUrl("https://videogamedb.uk/api")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");
    //Runtime parameters
    private static final int USER_COUNT=Integer.parseInt(System.getProperty("USERS","5"));
    private static final int RAMP_DURATION=Integer.parseInt(System.getProperty("RAMP_DURATION","10"));
    private static final int TEST_DURATION=Integer.parseInt(System.getProperty("TEST_DURATION","20"));

    //Feeder for test - CSV, Json
    private static FeederBuilder.FileBased<Object> jsonFeeder=jsonFile("Data/jsonFile.json").random();

    //Before block
    @Override
    public void before(){
        System.out.printf("Running test with %d users%n",USER_COUNT);
        System.out.printf("Ramping up the users in %d seconds%n",RAMP_DURATION);
        System.out.printf("Running the Final test in %d seconds%n", TEST_DURATION);
    }

    //Making Http calls

     //Authenticating the data
    private static ChainBuilder authenticate =
            exec(http("Authenticate")
                    .post("/authenticate")
                    .body(StringBody("{\n" +
                            "  \"password\": \"admin\",\n" +
                            "  \"username\": \"admin\"\n" +
                            "}"))
                    .check(jmesPath("token").saveAs("jwtToken")));

      //Get All video games
    private static ChainBuilder getAllVideoGames =
            exec(http("Get all video games")
                    .get("/videogame"));

      //Creating a New game
    private static ChainBuilder createNewGame =
            feed(jsonFeeder)
                    .exec(http("Create New Game - #{name}")
                            .post("/videogame")
                            .header("Authorization", "Bearer #{jwtToken}")
                            .body(ElFileBody("bodies/newGameTemplate.json")).asJson());

    //Get last posted game

    private static ChainBuilder getLastPostedGame =
            exec(http("Get Last Posted Game - #{name}")
                    .get("/videogame/#{id}")
                    .check(jmesPath("name").isEL("#{name}")));

    //Delete last posted game

    private static ChainBuilder deleteLastPostedGame =
            exec(http("Delete game - #{name}")
                    .delete("/videogame/#{id}")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(bodyString().is("Video game deleted")));

    // SCENARIO OR USER JOURNEY
    // 1. Get all video games
    // 2. Create a new game
    // 3. Get details of newly created game
    // 4. Delete newly created game
    private ScenarioBuilder scn = scenario("Video game db - final simulation")
            .forever().on(
                    exec(getAllVideoGames)
                            .pause(2)
                            .exec(authenticate)
                            .pause(2)
                            .exec(createNewGame)
                            .pause(2)
                            .exec(getLastPostedGame)
                            .pause(2)
                            .exec(deleteLastPostedGame)
            );

    // LOAD SIMULATION
    {
        setUp(
                scn.injectOpen(
                        nothingFor(5),
                        rampUsers(USER_COUNT).during(RAMP_DURATION)
                ).protocols(httpProtocol)
        ).maxDuration(TEST_DURATION);
    }

    // AFTER BLOCK
    @Override
    public void after() {
        System.out.println("Stress test completed");
    }

}
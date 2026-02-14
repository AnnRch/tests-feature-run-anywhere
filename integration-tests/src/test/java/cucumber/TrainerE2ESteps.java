package cucumber;

import com.gymcrm.gym_crm_spring.dto.TrainingCreateRequest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class TrainerE2ESteps extends CucumberConfiguration {

    private Response lastErrorResponse;
    private String otherTrainerUsername;
    private String otherTrainerPassword;
    private String otherTrainingId;
    private String jwtToken;
    private String capturedPassword;
    private String actualUsername;
    private String actualTraineeUsername;
    private final String GYM_API = "http://localhost:8080";
    private final String WORKLOAD_API = "http://localhost:8081";

    @Given("a new trainee {string} registers on the Gym Service")
    public void registerTrainee(String username) {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(8);
        Map<String, String> request = Map.of(
                "firstName", "Natali",
                "lastName", "Ageeva" + uniqueSuffix,
                "dateOfBirth", "1995-05-20",
                "address", "123 Cucumber Lane"
        );

        Response response = given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(GYM_API + "/api/auth/register/trainee")
                .then()
                .statusCode(201)
                .extract().response();

        actualTraineeUsername = response.path("username");
        System.out.println("DEBUG: Registered actual trainee username: " + actualTraineeUsername);
    }

    @Given("a new trainer {string} registers on the Gym Service")
    public void registerTrainer(String alias) {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(8);
        Map<String, String> request = Map.of(
                "firstName", alias.contains(".") ? alias.split("\\.")[0] : alias,
                "lastName", "Levada" + uniqueSuffix,
                "specializationName", "Crossfit"
        );

        Response response = given()
                .contentType("application/json")
                .body(request)
                .when()
                .post(GYM_API + "/api/auth/register/trainer")
                .then()
                .statusCode(201)
                .extract().response();

        if (alias.equals("lilia.levada")) {
            actualUsername = response.path("username");
            capturedPassword = response.path("password");
        } else {
            otherTrainerUsername = response.path("username");
            otherTrainerPassword = response.path("password"); // Capture this!
        }
    }

    @When("I request the workload for the trainer with an expired or invalid token")
    public void requestWithInvalidToken() {
        lastErrorResponse = given()
                .header("Authorization", "Bearer invalid_token_xyz")
                .queryParam("username", "any_user")
                .when()
                .get(WORKLOAD_API + "/api/workload");
    }

    @Given("the {string} adds a {int}-minute training")
    public void otherTrainerAddsTraining(String alias, int duration) {
        if (otherTrainerUsername == null || actualTraineeUsername == null) {
            throw new IllegalStateException("Setup failed: Trainer or Trainee username is missing!");
        }

        String otherTrainerToken = given()
                .contentType("application/json")
                .body(Map.of(
                        "username", otherTrainerUsername,
                        "password", otherTrainerPassword
                ))
                .when()
                .post(GYM_API + "/api/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");

        TrainingCreateRequest request = new TrainingCreateRequest(
                actualTraineeUsername,
                otherTrainerUsername,
                "Other Training Session",
                LocalDate.now(),
                duration
        );

        given()
                .header("Authorization", "Bearer " + otherTrainerToken) // Use the temporary token
                .contentType("application/json")
                .body(request)
                .when()
                .post(GYM_API + "/api/training/add")
                .then()
                .statusCode(200);

        otherTrainingId = fetchLastTrainingIdWithToken(otherTrainerUsername, otherTrainerToken);
    }
    @When("the trainer {string} attempts to delete the training of {string}")
    public void deleteOtherTrainerTraining(String me, String other) {
        // 'otherTrainingId' would be fetched via an admin or during setup
        lastErrorResponse = given()
                .header("Authorization", "Bearer " + jwtToken) // My token
                .when()
                .delete(GYM_API + "/api/training/" + otherTrainingId);
    }

    @When("the trainer {string} adds a {int}-minute training named {string}")
    public void addNegativeDurationTraining(String username, int duration, String name) {
        Map<String, Object> request = Map.of(
                "traineeUsername", "natali.ageeva",
                "trainerUsername", actualUsername,
                "trainingName", name,
                "trainingDate", "2026-03-10",
                "trainingDuration", duration
        );

        lastErrorResponse = given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(request)
                .when()
                .post(GYM_API + "/api/training/add");
    }

    @Then("the trainer {string} logs in to obtain a JWT token")
    public void loginTrainer(String ignore) {
        Map<String, String> loginRequest = Map.of(
                "username", actualUsername,
                "password", capturedPassword
        );

        jwtToken = given()
                .contentType("application/json")
                .body(loginRequest)
                .when()
                .post(GYM_API + "/api/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
    }

    @Then("the workload for {string} should not change")
    public void verifyWorkloadNotChanged(String username) {
        Response response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .queryParam("username", actualUsername)
                .when()
                .get(WORKLOAD_API + "/api/workload");

        if (response.getStatusCode() == 200) {
            Object years = response.jsonPath().get("years");
            org.junit.jupiter.api.Assertions.assertTrue(years == null || ((java.util.List<?>)years).isEmpty(),
                    "Workload should have been empty but found data!");
        }
    }

    @Then("the response status should be {int}")
    public void verifyGenericResponseStatus(int expectedStatus) {
        lastErrorResponse.then().statusCode(expectedStatus);
    }

    @Then("the gym service should reject the request with status {int}")
    public void verifyGymServiceRejection(int expectedStatus) {
        lastErrorResponse.then().statusCode(expectedStatus);
    }

    @Then("the trainer {string} should eventually exist in the Workload Service")
    public void verifyTrainerExists(String ignore) {
        System.out.println("Using Token: " + jwtToken); // Verify this isn't null!
        System.out.println("Querying for: " + actualUsername);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() ->
                        given()
                                .header("Authorization", "Bearer " + jwtToken)
                                .queryParam("username", actualUsername)
                                .when()
                                .get(WORKLOAD_API + "/api/workload")
                                .then()
                                .log().ifValidationFails()
                                .statusCode(200)
                );
    }

    @When("the trainer {string} adds a {int}-minute training named {string} for {string}")
    public void addTraining(String ignore, int duration, String trainingName, String date) {
        TrainingCreateRequest request = new TrainingCreateRequest(
                actualTraineeUsername,
                actualUsername,
                trainingName,
                LocalDate.parse(date),
                duration
        );

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .body(request)
                .when()
                .post(GYM_API + "/api/training/add") // Ensure this matches your @PostMapping
                .then()
                .statusCode(200);
    }

    @Then("the training should be accepted by the Gym Service")
    public void the_training_should_be_accepted_by_the_gym_service() {
        System.out.println("Confirmed: Gym Service accepted the training.");
    }

    @Then("the workload for {string} should eventually show {int} minutes for {string} {int}")
    public void verifyWorkloadMinutes(String ignore, int expectedMinutes, String month, int year) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    Response response = given()
                            .header("Authorization", "Bearer " + jwtToken)
                            .queryParam("username", actualUsername)
                            .when()
                            .get(WORKLOAD_API + "/api/workload")
                            .then()
                            .statusCode(200)
                            .extract().response();

                    System.out.println("WORKLOAD RESPONSE: " + response.asString()); // <-- ADD THIS LOG

                    Integer actualMinutes = response.jsonPath().get(
                            "years.find { it.year == " + year + " }.months.find { it.month.equalsIgnoreCase('" + month + "') }.trainingSummaryDuration"
                    );

                    actualMinutes = (actualMinutes == null) ? 0 : actualMinutes;
                    org.junit.jupiter.api.Assertions.assertEquals(expectedMinutes, actualMinutes);
                });    }

    @When("the trainer {string} deletes the training named {string}")
    public void deleteTrainingByName(String ignore, String trainingName) {
        Response response = given()
                .header("Authorization", "Bearer " + jwtToken)
                .queryParam("username", actualUsername) // Required @NotBlank param
                .when()
                .get(GYM_API + "/api/trainer/trainings")
                .then()
                .statusCode(200)
                .extract().response();

        System.out.println("DEBUG Trainings List: " + response.asString());

        String id = response.jsonPath().getString("trainings.find { it.trainingName == '" + trainingName + "' }.id");

        if (id == null) {
            id = response.jsonPath().getString("find { it.trainingName == '" + trainingName + "' }.id");
        }

        if (id == null || id.equals("null")) {
            throw new RuntimeException("Training '" + trainingName + "' not found for " + actualUsername);
        }

        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(GYM_API + "/api/training/" + id)
                .then()
                .statusCode(204);
    }

    @When("the trainer profile {string} is deleted")
    public void deleteProfile(String ignore) {
        given()
                .header("Authorization", "Bearer " + jwtToken)
                .when()
                .delete(GYM_API + "/api/trainer/" + actualUsername)
                .then()
                .statusCode(200);
    }

    @Then("the workload for {string} should eventually be removed from the system")
    public void verifyWorkloadDeleted(String ignore) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    Response response = given()
                            .header("Authorization", "Bearer " + jwtToken)
                            .queryParam("username", actualUsername)
                            .when()
                            .get(WORKLOAD_API + "/api/workload");

                    int statusCode = response.getStatusCode();
                    if (statusCode != 404 && statusCode != 403) {
                        throw new AssertionError("Expected 404 or 403, but got " + statusCode);
                    }
                });
    }

    private String fetchLastTrainingIdWithToken(String trainerUsername, String token) {
        Response response = given()
                .header("Authorization", "Bearer " + token)
                .queryParam("username", trainerUsername)
                .when()
                .get(GYM_API + "/api/trainer/trainings")
                .then()
                .statusCode(200)
                .extract().response();

        return response.jsonPath().getString("trainings[0].id");
    }
}

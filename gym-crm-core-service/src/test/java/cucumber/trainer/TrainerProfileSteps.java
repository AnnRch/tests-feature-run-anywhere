package cucumber.trainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.gym_crm_spring.dto.TrainerActivationRequest;
import com.gymcrm.gym_crm_spring.dto.TrainerProfileUpdateRequest;
import com.gymcrm.gym_crm_spring.dto.TrainerRegistrationRequest;
import com.gymcrm.gym_crm_spring.facade.GymFacade;
import com.gymcrm.gym_crm_spring.messaging.WorkloadMessageProducer;
import com.gymcrm.gym_crm_spring.service.TrainerService;
import cucumber.TestContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public class TrainerProfileSteps {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TestContext testContext;

    private ResultActions latestResponse;

    @Autowired
    private Flyway flyway;

    @MockBean
    private WorkloadMessageProducer workloadMessageProducer;

    @Before
    public void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Given("the system is initialized with a trainer {string} and password {string}")
    public void theSystemIsInitialized(String username, String password) {
        if (trainerService.findByUsername(username).isEmpty()) {
            // Parse names from username (e.g., "other.trainer" -> "other", "trainer")
            String[] names = username.split("\\.");
            String firstName = names[0].substring(0, 1).toUpperCase() + names[0].substring(1);
            String lastName = (names.length > 1) ?
                    names[1].substring(0, 1).toUpperCase() + names[1].substring(1) : "Trainer";

            var registrationRequest = new TrainerRegistrationRequest(
                    firstName,
                    lastName,
                    "Yoga"
            );
            gymFacade.registerTrainer(registrationRequest);
        }
    }

    @Given("I am authenticated as {string}")
    public void iAmAuthenticatedAs(String username) {}

    @When("I request my trainer profile")
    public void iRequestMyTrainerProfile() throws Exception {
        testContext.setResponse(mockMvc.perform(get("/api/trainer/profile")
                        .with(user("lilia.levada").roles("TRAINER")))
                        .andDo(print()));
    }

    @Then("the trainer response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) throws Exception {
        testContext.getResponse().andExpect(status().is(statusCode));
    }

    @And("the profile should contain the username {string}")
    public void theProfileShouldContainTheUsername(String username) throws Exception {
        testContext.getResponse().andExpect(jsonPath("$.username").value(username));
    }

    @When("I update my profile with the following details:")
    public void iUpdateMyProfile(io.cucumber.datatable.DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);
        TrainerProfileUpdateRequest request = new TrainerProfileUpdateRequest(
                "lilia.levada",
                data.get("firstName"),
                data.get("lastName"),
                "Yoga",
                Boolean.valueOf(data.get("isActive"))
        );

        testContext.setResponse(mockMvc.perform(put("/api/trainer/profile")
                .with(user("lilia.levada"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @Then("the updated profile firstName should be {string}")
    public void theUpdatedProfileFirstNameShouldBe(String firstName) throws Exception {
        testContext.getResponse().andExpect(jsonPath("$.firstName").value(firstName));
    }

    @When("I send a request to set my activation status to {word}")
    public void iSendRequestToSetActivationStatus(String status) throws Exception {
        TrainerActivationRequest request = new TrainerActivationRequest(
                "lilia.levada",
                Boolean.parseBoolean(status)
        );

        testContext.setResponse(mockMvc.perform(patch("/api/trainer/activate")
                .with(user("lilia.levada"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @And("my profile status should be {word}")
    public void myProfileStatusShouldBe(String status) throws Exception {
        String content = testContext.getResponse().andReturn().getResponse().getContentAsString();

        if (content == null || content.trim().isEmpty()) {
            testContext.setResponse(mockMvc.perform(get("/api/trainer/profile")
                    .with(user("lilia.levada").roles("TRAINER"))));
        }
        testContext.getResponse().andExpect(jsonPath("$.isActive").value(Boolean.parseBoolean(status)));
    }

    @Given("I am an administrator")
    public void iAmAnAdministrator() {
        // Placeholder
    }

    @When("I delete the trainer profile for {string}")
    public void iDeleteTheTrainerProfileFor(String username) throws Exception {
        testContext.setResponse(mockMvc.perform(delete("/api/trainer/" + username)
                .with(user("admin").roles("ADMIN"))));
    }

    @And("the trainer {string} should no longer exist")
    public void theTrainerShouldNoLongerExist(String username) {
        org.junit.jupiter.api.Assertions.assertTrue(
                trainerService.findByUsername(username).isEmpty()
        );
    }

    @When("I request my trainer profile without credentials")
    public void iRequestProfileWithoutAuth() throws Exception {
        // No .with(user(...)) call to simulate anonymous access
        testContext.setResponse(mockMvc.perform(get("/api/trainer/profile")));
    }

    @When("I update trainer {string} profile with the following details:")
    public void iUpdateOtherTrainerProfile(String targetUsername, io.cucumber.datatable.DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);
        TrainerProfileUpdateRequest request = new TrainerProfileUpdateRequest(
                targetUsername,
                data.get("firstName"),
                data.get("lastName"),
                "Yoga",
                Boolean.valueOf(data.get("isActive"))
        );

        // Authenticated as lilia, but trying to modify someone else (targetUsername)
        testContext.setResponse(mockMvc.perform(put("/api/trainer/profile")
                .with(user("lilia.levada").roles("TRAINER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))));
    }

    @When("I attempt to delete the trainer profile for {string} as a trainer")
    public void iDeleteAsTrainer(String username) throws Exception {
        // Only ADMIN should be able to delete; TRAINER role should get 403
        testContext.setResponse(mockMvc.perform(delete("/api/trainer/" + username)
                .with(user("lilia.levada").roles("TRAINER"))));
    }

    @When("I request profile for {string} as an administrator")
    public void iRequestNonExistentProfile(String username) throws Exception {
        testContext.setResponse(mockMvc.perform(get("/api/trainer/" + username)
                .with(user("admin").roles("ADMIN"))));
    }
}

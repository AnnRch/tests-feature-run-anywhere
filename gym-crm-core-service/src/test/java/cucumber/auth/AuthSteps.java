package cucumber.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.gym_crm_spring.dto.ChangePasswordRequest;
import com.gymcrm.gym_crm_spring.dto.LoginRequest;
import com.gymcrm.gym_crm_spring.dto.TraineeRegistrationRequest;
import com.gymcrm.gym_crm_spring.dto.TrainerRegistrationRequest;
import com.gymcrm.gym_crm_spring.facade.GymFacade;
import com.gymcrm.gym_crm_spring.service.UserService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthSteps {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private GymFacade gymFacade;
    @Autowired
    private UserService userService;

    private ResultActions latestResponse;

    @BeforeAll
    public static void setup(){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @When("I register a trainee with the following details:")
    public void iRegisterTrainee(DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);
        var request = new TraineeRegistrationRequest(
                data.get("firstName"),
                data.get("lastName"),
                Optional.empty(),
                Optional.ofNullable(data.get("address"))
        );

        latestResponse = mockMvc.perform(post("/api/auth/register/trainee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @When("I register a trainer with the following details:")
    public void iRegisterTrainer(DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);
        var request = new TrainerRegistrationRequest(
                data.get("firstName"),
                data.get("lastName"),
                data.get("specializationName")
        );

        latestResponse = mockMvc.perform(post("/api/auth/register/trainer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Given("a trainee {string} exists with password {string}")
    public void aTraineeExistsWithPassword(String username, String password) {
        String[] parts = username.split("\\.");
        var regResponse = gymFacade.registerTrainee(new TraineeRegistrationRequest(
                parts[0], parts[1], Optional.empty(), Optional.empty()));

        gymFacade.changeLogin(new ChangePasswordRequest(
                regResponse.username(),
                regResponse.password(),
                password));
    }

    @When("I login with username {string} and password {string}")
    public void iLogin(String username, String password) throws Exception {
        var request = new LoginRequest(username, password);
        latestResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @When("I change password for {string} from {string} to {string}")
    public void iChangePassword(String username, String oldPass, String newPass) throws Exception {
        var request = new ChangePasswordRequest(username, oldPass, newPass);
        latestResponse = mockMvc.perform(put("/api/auth/change-login")
                        .with(user(username).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Then("the auth response status should be {int}")
    public void verifyAuthStatus(int statusCode) throws Exception {
        latestResponse.andExpect(status().is(statusCode));
    }

    @Then("the response should contain a username and password")
    public void verifyCredentials() throws Exception {
        latestResponse.andExpect(jsonPath("$.username", notNullValue()))
                .andExpect(jsonPath("$.password", notNullValue()));
    }

    @Then("the response should contain a JWT token")
    public void verifyJwtToken() throws Exception {
        latestResponse.andExpect(jsonPath("$.token", notNullValue()));
    }

    @When("I register a trainee with the following invalid details:")
    public void iRegisterTraineeInvalid(DataTable dataTable) throws Exception {
        var data = dataTable.asMaps().get(0);
        // Creating a request where firstName might be empty/null based on table
        var request = new TraineeRegistrationRequest(
                data.get("firstName"),
                data.get("lastName"),
                Optional.empty(),
                Optional.empty()
        );

        latestResponse = mockMvc.perform(post("/api/auth/register/trainee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @When("I attempt to change password for {string} from {string} to {string} without authentication")
    public void iChangePasswordWithoutAuth(String username, String oldPass, String newPass) throws Exception {
        var request = new ChangePasswordRequest(username, oldPass, newPass);

        latestResponse = mockMvc.perform(put("/api/auth/change-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}

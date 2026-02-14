package com.gymcrm.trainer_workload_service.cucumber.workload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.trainer_workload_service.cucumber.CucumberConfiguration;
import com.gymcrm.trainer_workload_service.cucumber.TestContext;
import com.gymcrm.trainer_workload_service.dto.ActionType;
import com.gymcrm.trainer_workload_service.dto.TrainerWorkloadRequest;
import com.gymcrm.trainer_workload_service.entity.MonthSummary;
import com.gymcrm.trainer_workload_service.entity.TrainerWorkload;
import com.gymcrm.trainer_workload_service.messaging.WorkloadMessageConsumer;
import com.gymcrm.trainer_workload_service.service.WorkloadService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainerWorkloadSteps extends CucumberConfiguration {
    @Autowired
    private MockMvc
            mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestContext testContext;

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Given("the workload system is clean")
    public void cleanSystem() {
        mongoTemplate.dropCollection(TrainerWorkload.class);
    }

    @Given("a workload exists for trainer {string} with the following data:")
    public void seedTrainerData(String username, DataTable dataTable) {
        TrainerWorkloadRequest initRequest = TrainerWorkloadRequest.builder()
                .username(username)
                .firstName("Lilia")
                .lastName("Levada")
                .isActive(true)
                .build();

        workloadService.createTrainerLogic(initRequest);

        dataTable.asMaps().forEach(row -> {
            TrainerWorkloadRequest updateRequest = TrainerWorkloadRequest.builder()
                    .username(username)
                    .trainingDate(LocalDate.of(
                            Integer.parseInt(row.get("year")),
                            java.time.Month.valueOf(row.get("month").toUpperCase()),
                            1))
                    .trainingDuration(Integer.parseInt(row.get("duration")))
                    .actionType(ActionType.ADD)
                    .build();

            workloadService.saveTrainerData(updateRequest);
        });
    }

    @When("I request the workload for trainer {string}")
    public void getWorkload(String username) throws Exception {
        testContext.setResponse(mockMvc.perform(get("/api/workload")
                .with(user("admin"))
                .param("username", username)
                .contentType(MediaType.APPLICATION_JSON)));
    }

    @When("I request the workload for trainer {string} without credentials")
    public void getWorkloadNoAuth(String username) throws Exception {
        // Performing GET without the .with(user(...)) post-processor
        testContext.setResponse(mockMvc.perform(get("/api/workload")
                .param("username", username)
                .contentType(MediaType.APPLICATION_JSON)));
    }

    @When("I request the workload with a missing username parameter")
    public void getWorkloadMissingParam() throws Exception {
        // Simulating a bad request by omitting the required "username" query parameter
        testContext.setResponse(mockMvc.perform(get("/api/workload")
                .with(user("admin"))
                .contentType(MediaType.APPLICATION_JSON)));
    }

    @Then("the workload response status should be {int}")
    public void verifyStatus(int status) throws Exception {
        testContext.getResponse().andExpect(status().is(status));
    }

    @And("the response should contain {string} and {string}")
    public void verifyNames(String firstName, String lastName) throws Exception {
        testContext.getResponse()
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.lastName").value(lastName));
    }

    @And("the response should show {int} minutes for {string} {int}")
    public void verifyMonthlySummary(int duration, String month, int year) throws Exception {
        testContext.getResponse()
                .andExpect(jsonPath("$.years[?(@.year == " + year + ")].months[?(@.month == '" + month.toUpperCase() + "')].trainingSummaryDuration")
                        .value(duration));
    }
}

package com.gymcrm.trainer_workload_service.entity;

import lombok.*;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.index.CompoundIndex;
//import org.springframework.data.mongodb.core.index.CompoundIndexes;
//import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Document(collection = "trainer_workload")
//@CompoundIndexes({
//        @CompoundIndex(
//                name = "idx_user_year",
//                def = "{'username': 1, 'years.year': 1}",
//                unique = true
//        ),
//        @CompoundIndex(
//                name = "idx_trainer_name",
//                def = "{'firstName': 1, 'lastName': 1}"
//        )
//})
public class TrainerWorkload {
//    @Id
    private String username;
    private String firstName;
    private String lastName;
    private String status;
    private List<YearSummary> years;
}
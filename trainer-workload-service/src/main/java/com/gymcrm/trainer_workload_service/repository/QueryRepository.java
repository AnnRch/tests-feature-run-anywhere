package com.gymcrm.trainer_workload_service.repository;

import com.gymcrm.trainer_workload_service.entity.TrainerWorkload;

public interface QueryRepository {
    TrainerWorkload findByUsername(String userName);
}

package com.qunite.api.data;

import com.qunite.api.domain.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {
  boolean existsByCreatorId(Long id);
}

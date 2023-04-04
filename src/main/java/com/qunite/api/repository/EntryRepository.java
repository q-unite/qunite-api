package com.qunite.api.repository;

import com.qunite.api.domain.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    long countByUserId(Long id);

    long countByQueueId(Long id);
}

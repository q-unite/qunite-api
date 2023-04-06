package com.qunite.api.data;

import com.qunite.api.domain.Entry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntryRepository extends JpaRepository<Entry, Long> {
    boolean existsByMemberId(Long id);

    boolean existsByQueueId(Long id);

    @Query("SELECT e.id FROM Entry e where e.queue.id = :queueId")
    List<Long> findEntriesIdByQueueId(Long queueId);
}

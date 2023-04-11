package com.qunite.api.data;

import com.qunite.api.domain.Entry;
import com.qunite.api.domain.EntryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends JpaRepository<Entry, EntryId> {
  boolean existsByMemberId(Long id);

  @Query("SELECT e.id FROM Entry e where e.queue.id = :queueId")
  List<EntryId> findEntriesIdsByQueueId(Long queueId);
}
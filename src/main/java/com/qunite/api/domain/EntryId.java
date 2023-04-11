package com.qunite.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class EntryId implements Serializable {
  @Column(name = "member_id")
  Long memberId;

  @Column(name = "queue_id")
  Long queueId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    EntryId entryId = (EntryId) o;
    return memberId != null && Objects.equals(memberId, entryId.memberId)
        && queueId != null && Objects.equals(queueId, entryId.queueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(memberId, queueId);
  }
}
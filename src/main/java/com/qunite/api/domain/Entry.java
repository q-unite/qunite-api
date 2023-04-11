package com.qunite.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@Table(name = "ENTRIES")
@ToString
@NoArgsConstructor
public class Entry {
  @EmbeddedId
  EntryId id = new EntryId();

  @ManyToOne
  @MapsId("memberId")
  @JoinColumn(name = "member_id")
  User member;

  @ManyToOne
  @MapsId("queueId")
  @JoinColumn(name = "queue_id")
  Queue queue;

  @Column(name = "created_at")
  Instant createdAt = Instant.now();

  @Column(name = "entry_index")
  Integer entryIndex;

  public Entry(User member, Queue queue) {
    this.member = member;
    this.queue = queue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Entry entry = (Entry) o;
    return id != null && Objects.equals(id, entry.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
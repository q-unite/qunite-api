package com.qunite.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "ENTRIES")
@ToString
@Getter
@Setter
@NoArgsConstructor
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    User member;

    @ManyToOne
    @JoinColumn(name = "queue_id")
    Queue queue;

    @Column(name = "created_at")
    Instant createdAt = Instant.now();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Entry that = (Entry) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

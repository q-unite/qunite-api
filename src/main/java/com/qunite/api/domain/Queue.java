package com.qunite.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "QUEUES")
@ToString
@NoArgsConstructor
public class Queue {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Getter
    @Setter
    @Column
    String name;

    @Getter
    @Setter
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "creator_id")
    User creator;

    @Access(AccessType.FIELD)
    @ToString.Exclude
    @OrderBy("id asc")
    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "QUEUES_MANAGERS",
            joinColumns = @JoinColumn(name = "queue_id"),
            inverseJoinColumns = @JoinColumn(name = "manager_id"))
    List<User> managers = new ArrayList<>();

    @Access(AccessType.FIELD)
    @OrderBy("createdAt asc, id asc")
    @ToString.Exclude
    @OneToMany(mappedBy = "queue", cascade = CascadeType.REMOVE)
    List<Entry> entries = new ArrayList<>();

    @Getter
    @Column(name = "created_at")
    Instant createdAt = Instant.now();

    public void addManager(User manager) {
        managers.add(manager);
        manager.managedQueues.add(this);
    }

    public void removeManager(User manager) {
        managers.remove(manager);
        manager.managedQueues.remove(this);
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
        entry.setQueue(this);
    }

    public void removeEntry(Entry entry) {
        entries.remove(entry);
        entry.member.entries.remove(entry);
        entry.setMember(null);
        entry.setQueue(null);
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<User> getManagers() {
        return Collections.unmodifiableList(managers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Queue queue = (Queue) o;
        return getId() != null && Objects.equals(getId(), queue.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

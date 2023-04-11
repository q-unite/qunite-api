package com.qunite.api.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Getter
@Setter
@Entity
@Table(name = "QUEUES")
@ToString
@NoArgsConstructor
public class Queue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column
  String name;

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
  Set<User> managers = new LinkedHashSet<>();

  @Access(AccessType.FIELD)
  @ToString.Exclude
  @OneToMany(mappedBy = "queue", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "entry_index")
  List<Entry> entries = new ArrayList<>();


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

  public Set<User> getManagers() {
    return Collections.unmodifiableSet(managers);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    Queue queue = (Queue) o;
    return getId() != null && Objects.equals(getId(), queue.getId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
package com.qunite.api.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "USERS")
@ToString
@NoArgsConstructor
public class User implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  String username;

  String email;

  String password;

  @Access(AccessType.FIELD)
  @ToString.Exclude
  @OrderBy("id asc")
  @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
  List<Queue> createdQueues = new ArrayList<>();

  @Access(AccessType.FIELD)
  @ToString.Exclude
  @OrderBy("id asc")
  @ManyToMany(mappedBy = "managers")
  Set<Queue> managedQueues = new LinkedHashSet<>();

  @Access(AccessType.FIELD)
  @ToString.Exclude
  @OrderBy("id asc")
  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  List<Entry> entries = new ArrayList<>();

  @Access(AccessType.FIELD)
  @ToString.Exclude
  @OrderBy("id asc")
  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  List<TokenPair> tokenPairs = new ArrayList<>();

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  public void addCreatedQueue(Queue queue) {
    createdQueues.add(queue);
    queue.setCreator(this);
  }

  public void removeCreatedQueue(Queue queue) {
    createdQueues.remove(queue);
    queue.setCreator(null);
  }

  public void addManagedQueue(Queue queue) {
    managedQueues.add(queue);
    queue.managers.add(this);
  }

  public void removeManagedQueue(Queue queue) {
    managedQueues.remove(queue);
    queue.managers.remove(this);
  }

  public void addEntry(Entry entry) {
    entries.add(entry);
    entry.setMember(this);
  }

  public void removeEntry(Entry entry) {
    entries.remove(entry);
    entry.setMember(null);
  }

  public void addTokenPair(TokenPair tokenPair) {
    this.tokenPairs.add(tokenPair);
    tokenPair.setOwner(this);
  }

  public void clearTokenPairs() {
    this.tokenPairs.clear();
  }

  public List<Queue> getCreatedQueues() {
    return Collections.unmodifiableList(createdQueues);
  }

  public Set<Queue> getManagedQueues() {
    return Collections.unmodifiableSet(managedQueues);
  }

  public List<Entry> getEntries() {
    return Collections.unmodifiableList(entries);
  }

  public List<TokenPair> getTokenPairs() {
    return Collections.unmodifiableList(tokenPairs);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    User user = (User) o;
    return getId() != null && Objects.equals(getId(), user.getId());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return Collections.emptyList();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
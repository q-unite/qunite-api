package com.qunite.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "token_pairs")
@ToString
@NoArgsConstructor
public class TokenPair {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @JoinColumn(name = "owner_id")
  @ManyToOne
  User owner;

  @Column(name = "access_token", length = 1275)
  String accessToken;

  @Column(name = "refresh_token", length = 1275)
  String refreshToken;

  @Column(name = "is_valid")
  boolean isValid = true;

  @Column(name = "created_at")
  Instant createdAt = Instant.now();
}

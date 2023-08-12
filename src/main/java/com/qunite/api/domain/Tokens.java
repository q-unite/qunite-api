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
@Table(name = "TOKENS")
@ToString
@NoArgsConstructor
public class Tokens {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @JoinColumn(name = "owner_id")
  @ManyToOne
  User owner;

  @Column(name = "access_token")
  String accessToken;

  @Column(name = "refresh_token")
  String refreshToken;

  @Column(name = "created_at")
  Instant createdAt = Instant.now();
}

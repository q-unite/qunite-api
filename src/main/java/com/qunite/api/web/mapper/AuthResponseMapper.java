package com.qunite.api.web.mapper;

import com.qunite.api.domain.TokenPair;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Value;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AuthResponseMapper {
  @Value("${jwt.access-token-expiration-time}")
  protected Integer expires;

  @Mapping(target = "expires", expression = "java(this.expires)")
  public abstract AuthenticationResponse toAuthResponse(TokenPair tokenPair);
}

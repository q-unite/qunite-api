package com.qunite.api.web.mapper;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthResponseMapper {

  @Mapping(source = "token", target = "accessToken")
  AuthenticationResponse toAuthResponse(DecodedJWT decodedJwt);
}

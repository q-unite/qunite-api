package com.qunite.api.web.mapper;

import com.qunite.api.domain.TokenPair;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthResponseMapper {

  AuthenticationResponse toAuthResponse(TokenPair tokenPair);
}

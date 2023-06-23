package com.qunite.api.security;

import com.qunite.api.data.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByEmailOrUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(username));
  }
}

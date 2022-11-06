package com.backend.escort.security.service;

import com.backend.escort.model.User;
import com.backend.escort.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    private static final String NOT_FOUND = "User associated with email %s not found";

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
      User user = userRepository.findByEmail(email).orElseThrow(
                ()-> new UsernameNotFoundException(
                        String.format(NOT_FOUND,email)
                )
        );
      return UserDetailsImp.generate(user);
    }
}

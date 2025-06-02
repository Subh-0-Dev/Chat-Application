package com.subh.DemoChat.Service;

import com.subh.DemoChat.Entity.UserEntity;
import com.subh.DemoChat.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetail implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<UserEntity> user=userRepository.findByEmail(email);
        if(user.isPresent()) {
            return User.builder()
                    .username(user.get().getEmail())
                    .password(user.get().getPassword())
                    .build();
        }else {
            throw new UsernameNotFoundException(email);
        }
    }

}

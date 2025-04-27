package com.project_management_system.controller;

import com.project_management_system.config.JwtProvider;
import com.project_management_system.model.User;
import com.project_management_system.repository.UserRepository;
import com.project_management_system.request.LoginRequest;
import com.project_management_system.response.AuthResponse;
import com.project_management_system.service.CustomUserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsImpl customUserDetailsImpl;

    @PostMapping("/signup")
    public ResponseEntity<User> createUserHandler(@RequestBody User user) throws Exception {
        User isUserExist=userRepository.findByEmail(user.getEmail());
        if(isUserExist!=null){
            throw new Exception("Email already exist with another account.");
        }
        User createdUser=new User();
        createdUser.setPassword(passwordEncoder.encode(user.getPassword()));
        createdUser.setEmail(user.getEmail());
        createdUser.setFullName(user.getFullName());

        User savedUser=userRepository.save(createdUser);
        Authentication authentication=new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt= JwtProvider.generateToken(authentication);

        AuthResponse authResponse=new AuthResponse();
        authResponse.setMessage("Signup successful");
        authResponse.setJwt(jwt);

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PostMapping("/signing")
    public ResponseEntity<AuthResponse> signing(@RequestBody LoginRequest loginRequest) throws Exception {
       String username=loginRequest.getEmail();
       String password=loginRequest.getPassword();

        Authentication authentication= authenticate(username,password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt= JwtProvider.generateToken(authentication);

        AuthResponse authResponse=new AuthResponse();
        authResponse.setMessage("Signup successful");
        authResponse.setJwt(jwt);

        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails=customUserDetailsImpl.loadUserByUsername(username);

        if(userDetails==null) throw new BadCredentialsException("Invalid username!");
        if(!passwordEncoder.matches(password, userDetails.getPassword())) throw new BadCredentialsException("Invalid password!");
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}

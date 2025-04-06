package org.example.smarthealthmanagmentsystem.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/patients/register").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/patients/getAllPatient").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/patients/getPatientById/**").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/patients/getMyDetails").hasAnyRole("PATIENT")
                .requestMatchers("/patients/edit").hasAnyRole("ADMIN", "DOCTOR")
                .requestMatchers("/appointments/scheduleAppointment").hasRole("DOCTOR")
                .requestMatchers("/appointments/deleteAppointment/**").hasRole("DOCTOR")
                .requestMatchers("/appointments/**").hasAnyRole("DOCTOR", "PATIENT", "ADMIN")
                .requestMatchers("/appointments/editAppointment").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/prescription/addPrescription").hasAnyRole("DOCTOR")
                .requestMatchers("/prescription/edit").hasAnyRole("DOCTOR")
                .requestMatchers("/prescription/getAllPrescriptions").hasAnyRole("DOCTOR", "ADMIN", "PATIENT")
                .requestMatchers("/medicalRecord/upload").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/users/**").hasAnyRole("ADMIN")
                .requestMatchers("/doctors/getAllDoctor", "/doctors/create").hasAnyRole("ADMIN")
                .requestMatchers("/doctors/me").hasAnyRole("DOCTOR")
                .requestMatchers("/users/**").hasAnyRole("ADMIN")
                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();

    }
}

package es.ubu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Asegurar esta importación
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Asegurar esta importación
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
////            .csrf(csrf -> csrf.disable()) // Desactiva CSRF si no es necesario
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers("/login", "/registro", "/css/**", "/js/**").permitAll() // Permite acceso a estas rutas
//                .anyRequest().authenticated() // Requiere autenticación para otras rutas
//            )
//            .formLogin(form -> form
//                .loginPage("/custom-login") // Usa tu página personalizada de login
//                .permitAll()
//            )
//            .logout(logout -> logout.permitAll()); // Permite el logout


		http
		    .authorizeHttpRequests(auth -> auth
		        .requestMatchers("/login", "/registro", "/css/**", "/js/**", "/images/**").permitAll() // Permitir /images/**
		        .requestMatchers("/admin/**").hasAuthority("ADMIN") // Proteger rutas /admin/**
		        .anyRequest().authenticated()
		    )
		    .formLogin(form -> form
		        .loginPage("/login") // Specifies the custom login page
		        .defaultSuccessUrl("/menuprincipal", true) // Redirects to /home on successful login
		        .failureUrl("/login?error=true") // Redirects to /login with an error parameter on failure
		        .permitAll()
		    )
		    .logout(logout -> logout
		        .logoutUrl("/logout")
		        .logoutSuccessUrl("/login?logout=true")
		        .permitAll()
		    );

        return http.build();
    }
	
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(usuarioDetailsService).passwordEncoder(passwordEncoder());
//    }

}
package com.backend.escort.security.config;

import com.backend.escort.security.jwt.EntryPoint;
import com.backend.escort.security.jwt.TokenFilter;
import com.backend.escort.security.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true
)
public class SecurityConfiguration  extends WebSecurityConfigurerAdapter{


    /*
    spring.datasource.url= jdbc:postgresql://ec2-54-85-56-210.compute-1.amazonaws.com:5432/ddm240jtu3pe7d
    spring.datasource.username= yddmykiveurvbm
    spring.datasource.password= d1b5150dfa6828986759b7d376721a50f6aaa7cc5e4c0edc14776df1be8b3799

    spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation= true
    spring.jpa.properties.hibernate.dialect= org.hibernate.dialect.PostgreSQLDialect
    # Hibernate ddl auto (create, create-drop, validate, update)
    spring.jpa.hibernate.ddl-auto= update
    spring.datasource.driver-class-name=org.postgresql.Driver
    */


    @Autowired
    UserService userService;

    @Autowired
    private EntryPoint entryPoint;

    @Bean
    public TokenFilter tokenFilter(){
        return new TokenFilter();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static final String[] AUTH_WHITELIST = {
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**"
    };

    // Permitting api methods
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(entryPoint).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers("/api/v1/admin/**").permitAll()
                .antMatchers("/api/v1/student/**").permitAll()
                .antMatchers("/api/v1/excel/**").permitAll()
                .antMatchers("/api/v1/org/**").permitAll()
                .antMatchers("/api/v1/reports/**").permitAll()
                .antMatchers("/api/v1/dashboard/**").permitAll()
                .antMatchers("/api/v1/student/alerts").permitAll()
                .antMatchers("/api/v1/driver/**").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/swagger-ui/").permitAll()
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest()
                .authenticated();
        http.addFilterBefore(tokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());

    }




}

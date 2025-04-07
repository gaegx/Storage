package com.example.storage.config;


import com.example.storage.filter.CopyFilter;
import com.example.storage.filter.MoveFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<CopyFilter> filterRegistrationBean() {
        FilterRegistrationBean<CopyFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new CopyFilter());
        filterRegistrationBean.addUrlPatterns("/{filename}/copy/{copyfile}");
        return filterRegistrationBean;

    }

    @Bean
    public FilterRegistrationBean<MoveFilter> filterRegistrationBean2() {
        FilterRegistrationBean<MoveFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new MoveFilter());
        filterRegistrationBean.addUrlPatterns("/{filename}/move/{copyfile}");
        return filterRegistrationBean;
    }
}

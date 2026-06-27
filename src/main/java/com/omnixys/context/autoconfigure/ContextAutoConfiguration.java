package com.omnixys.context.autoconfigure;

import com.omnixys.context.async.ContextAwareTaskDecorator;
import com.omnixys.context.filter.ContextFilter;
import com.omnixys.context.filter.CorrelationIdFilter;
import com.omnixys.context.resolver.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

@AutoConfiguration
@ConditionalOnWebApplication
@EnableAsync
@Configuration
public class ContextAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextFilter contextFilter(Optional<PrincipalResolver> principalResolver) {
        return new ContextFilter(principalResolver.orElse(null));
    }

    @Bean
    @ConditionalOnMissingBean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration() {
        FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.setOrder(0);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextAwareTaskDecorator contextAwareTaskDecorator() {
        return new ContextAwareTaskDecorator();
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantResolver tenantResolver() {
        return new HeaderTenantResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientMetadataResolver clientMetadataResolver() {
        return new DefaultClientMetadataResolver();
    }

    @Bean
    public ContextArgumentResolver contextArgumentResolver() {
        return new ContextArgumentResolver();
    }

    @Bean
    public WebMvcConfigurer contextWebMvcConfigurer(ContextArgumentResolver resolver) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(resolver);
            }
        };
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.retry.annotation.EnableRetry")
    @EnableRetry
    static class ContextRetryConfiguration {
    }

    @Bean(name = {"taskExecutor", "contextAwareTaskExecutor"})
    @ConditionalOnMissingBean(Executor.class)
    public ThreadPoolTaskExecutor contextAwareTaskExecutor(ContextAwareTaskDecorator decorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(decorator);
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("omnixys-async-");
        executor.initialize();
        return executor;
    }
}

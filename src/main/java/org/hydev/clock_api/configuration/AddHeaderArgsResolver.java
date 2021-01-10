package org.hydev.clock_api.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AddHeaderArgsResolver implements WebMvcConfigurer {
    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    public AddHeaderArgsResolver(ConfigurableListableBeanFactory configurableListableBeanFactory) {
        this.configurableListableBeanFactory = configurableListableBeanFactory;
    }

    @Override
    // https://github.com/spring-projects/spring-framework/issues/23838
    // https://stackoverflow.com/questions/49305099/requestheader-not-binding-in-pojo-but-binding-only-in-variable
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new RequestHeaderMethodArgumentResolver(configurableListableBeanFactory));
    }
}

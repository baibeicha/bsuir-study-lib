package com.github.baibeicha.ioc.application.config;

import com.github.baibeicha.ioc.annotation.configuration.Configuration;
import com.github.baibeicha.ioc.annotation.leaf.TeaLeaf;
import com.github.baibeicha.ioc.application.event.context.EventContext;
import com.github.baibeicha.ioc.application.event.publisher.EventPublisher;
import com.github.baibeicha.ioc.application.event.publisher.StandardEventPublisher;
import com.github.baibeicha.ioc.context.TeaApplicationContext;
import com.github.baibeicha.ioc.context.factory.TeaLeafFactory;
import com.github.baibeicha.ioc.context.registry.TeaLeafDefinitionRegistry;
import com.github.baibeicha.ioc.reflection.ClassDependenciesScanner;
import com.github.baibeicha.reflection.PackageScanner;

@Configuration
public class BasicTeaApplicationConfiguration {

    @TeaLeaf
    public PackageScanner packageScanner(TeaApplicationContext applicationContext) {
        return applicationContext.getPackageScanner();
    }

    @TeaLeaf
    public ClassDependenciesScanner classDependenciesScanner(TeaApplicationContext applicationContext) {
        return applicationContext.getClassDependenciesScanner();
    }

    @TeaLeaf
    public TeaLeafFactory leafFactory(TeaApplicationContext applicationContext) {
        return applicationContext.getLeafFactory();
    }

    @TeaLeaf
    public TeaLeafDefinitionRegistry definitionRegistry(TeaApplicationContext applicationContext) {
        return applicationContext.getDefinitionRegistry();
    }

    @TeaLeaf
    public EventContext eventContext(TeaApplicationContext applicationContext) {
        return applicationContext.getEventContext();
    }

    @TeaLeaf
    public EventPublisher eventPublisher(EventContext eventContext) {
        return new StandardEventPublisher(eventContext);
    }
}

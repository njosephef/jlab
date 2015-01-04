package org.jlab.service;

import org.jlab.configuration.SpringMongoConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Created by scorpiovn on 1/3/15.
 */
public class MongoWorker {

    private static MongoOperations instance;

    private MongoWorker() {}

    public static MongoOperations getInstance() {
        if (instance == null) {
            ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringMongoConfig.class);
            instance = (MongoOperations) ctx.getBean("mongoTemplate");
        }
        return instance;
    }
}

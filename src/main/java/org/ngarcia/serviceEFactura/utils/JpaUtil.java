package org.ngarcia.serviceEFactura.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory entityManagerFactory = buildEntityManagerFactory();

    private static EntityManagerFactory buildEntityManagerFactory() {
        //está definido en persistence.xml
        return Persistence.createEntityManagerFactory("defaultPU");
    }

    public static EntityManager getEntityManager() {
        return  entityManagerFactory.createEntityManager();
    }
}

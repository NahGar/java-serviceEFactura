package org.ngarcia.serviceEFactura.interceptors;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import java.util.logging.Logger;

@TransactionalJpa
@Interceptor
public class TransactionalJpaInterceptor {
   
   @Inject
   private EntityManager em;
   
   @Inject
   private Logger log;
   
   @AroundInvoke
   public Object transactional(InvocationContext invocation) throws Exception {

      try {
         log.info(" -*-*- Iniciando trn en " + invocation.getMethod().getName());
         em.getTransaction().begin();
         Object resultado = invocation.proceed();
         em.getTransaction().commit();
         log.info(" -*-*- Finalizando trn");
         return resultado;
      }
      catch(Exception e) {
         em.getTransaction().rollback();
         throw e;
      }
   }
}

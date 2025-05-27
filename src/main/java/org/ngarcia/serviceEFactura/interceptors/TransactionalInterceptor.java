package org.ngarcia.serviceEFactura.interceptors;

import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.ngarcia.serviceEFactura.configs.MysqlConn;

import java.sql.Connection;
import java.util.logging.Logger;

@TransactionalJdbc
@Interceptor
public class TransactionalInterceptor {
   
   @Inject
   @MysqlConn
   private Connection conn;
   
   @Inject
   private Logger log;
   
   @AroundInvoke
   public Object transactional(InvocationContext invocation) throws Exception {
      
      if(conn.getAutoCommit()) {
         conn.setAutoCommit(false);
      }
      
      try {
         log.info(" -*-*- Iniciando trn en " + invocation.getMethod().getName());
         Object resultado = invocation.proceed();
         conn.commit();
         log.info(" -*-*- Finalizando trn");
         return resultado;
      }
      catch(Exception e) {
         conn.rollback();
         throw e;
      }
   }
}

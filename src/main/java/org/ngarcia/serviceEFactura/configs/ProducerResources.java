package org.ngarcia.serviceEFactura.configs;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.persistence.EntityManager;
import org.ngarcia.serviceEFactura.utils.JpaUtil;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@ApplicationScoped
public class ProducerResources {

   @Resource(name="jdbc/mysqlDB")
   private DataSource ds;
   
   @Produces
   @RequestScoped
   @MysqlConn
   private Connection beanConnection() throws NamingException, SQLException {
      //Context initContext = new InitialContext();
      //Context envContext = (Context) initContext.lookup("java:/comp/env");
      //DataSource ds = (DataSource) envContext.lookup("jdbc/mysqlDB");
      return ds.getConnection();
   }

   //en lugar del autoclose en ConexionFilter
   public void close(@Disposes @MysqlConn Connection conn) throws SQLException {
      conn.close();
   }

   @Produces
   private Logger beanLogger(InjectionPoint injectionPoint) {
      return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
   }

   @Produces
   @RequestScoped
   private EntityManager beanEntityManager() {
      return JpaUtil.getEntityManager();
   }

   public void close(@Disposes EntityManager entityManager) {
      if(entityManager.isOpen()) {
         entityManager.close();
         //log.info("cerrando la conexión de EntityManager");
      }
   }
}
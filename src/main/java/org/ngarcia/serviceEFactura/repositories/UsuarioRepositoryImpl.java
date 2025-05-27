package org.ngarcia.serviceEFactura.repositories;

import jakarta.persistence.*;
import org.ngarcia.serviceEFactura.models.Usuario;

import java.util.List;

public class UsuarioRepositoryImpl implements CrudRepository<Usuario> {

   private EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPU");

   @Override
   public List<Usuario> listar() throws Exception {
      EntityManager em = emf.createEntityManager();
      try {
         return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();
      } finally {
         em.close();
      }
   }

   @Override
   public Usuario porId(Long id) throws Exception {
      EntityManager em = emf.createEntityManager();
      try {
         return em.find(Usuario.class, id.intValue());
      } finally {
         em.close();
      }
   }

   @Override
   public List<Usuario> porNombre(String nombre) throws Exception {
      EntityManager em = emf.createEntityManager();
      try {
         return em.createQuery("SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class)
                 .setParameter("nombre", nombre)
                 .getResultList();
      } finally {
         em.close();
      }
   }

   @Override
   public Usuario crear(Usuario usuario) throws Exception {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();
         em.persist(usuario);
         tx.commit();
         return usuario;
      } catch (Exception e) {
         if (tx.isActive()) tx.rollback();
         throw e;
      } finally {
         em.close();
      }
   }

   @Override
   public Usuario editar(Usuario usuario) throws Exception {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();
         Usuario usuarioActualizado = em.merge(usuario);
         tx.commit();
         return usuarioActualizado;
      } catch (Exception e) {
         if (tx.isActive()) tx.rollback();
         throw e;
      } finally {
         em.close();
      }
   }

   @Override
   public void eliminar(Long id) throws Exception {
      EntityManager em = emf.createEntityManager();
      EntityTransaction tx = em.getTransaction();
      try {
         tx.begin();
         Usuario usuario = em.find(Usuario.class, id.intValue());
         if (usuario != null) {
            em.remove(usuario);
         }
         tx.commit();
      } catch (Exception e) {
         if (tx.isActive()) tx.rollback();
         throw e;
      } finally {
         em.close();
      }
   }
}
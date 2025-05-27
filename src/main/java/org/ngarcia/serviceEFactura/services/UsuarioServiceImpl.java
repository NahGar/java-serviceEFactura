package org.ngarcia.serviceEFactura.services;

import org.ngarcia.serviceEFactura.models.Usuario;
import org.ngarcia.serviceEFactura.repositories.CrudRepository;
import org.ngarcia.serviceEFactura.repositories.UsuarioRepositoryImpl;

import java.util.List;
import java.util.Optional;


public class UsuarioServiceImpl implements UsuarioService {

   private final CrudRepository<Usuario> repository = new UsuarioRepositoryImpl();

   public List<Usuario> listar() {
      try {
         return repository.listar();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public Optional<Usuario> porId(Long id) {
      try {
         return Optional.ofNullable(repository.porId(id));
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public List<Usuario> porNombre(String nombre) {
      try {
         return repository.porNombre(nombre);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public Usuario crear(Usuario usuario) {
      try {
         return repository.crear(usuario);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public Usuario editar(Usuario usuario) {
      try {
         return repository.editar(usuario);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public void eliminar(Long id) {
      try {
         repository.eliminar(id);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
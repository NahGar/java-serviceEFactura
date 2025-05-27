package org.ngarcia.serviceEFactura.repositories;

import java.util.List;

public interface CrudRepository<T> {

   List<T> listar() throws Exception;
   T porId(Long id) throws Exception;
   List<T> porNombre(String nombre) throws Exception;
   T crear(T t) throws Exception;
   T editar(T t) throws Exception;
   void eliminar(Long id) throws Exception;

}

package org.ngarcia.serviceEFactura.services;

import org.ngarcia.serviceEFactura.models.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
   List<Usuario> listar();
   Optional<Usuario> porId(Long id);
   Usuario crear(Usuario u);
   Usuario editar(Usuario u);
   void eliminar(Long id);
}

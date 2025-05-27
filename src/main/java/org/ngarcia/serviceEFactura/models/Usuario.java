package org.ngarcia.serviceEFactura.models;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int id;

   @Column(nullable = false, length = 100)
   private String nombre;

   @Column(nullable = false, unique = true, length = 100)
   private String email;

   // Getters y setters
   public int getId() { return id; }
   public void setId(int id) { this.id = id; }
   public String getNombre() { return nombre; }
   public void setNombre(String nombre) { this.nombre = nombre; }
   public String getEmail() { return email; }
   public void setEmail(String email) { this.email = email; }

   @Override
   public String toString() {
      return "Usuario{" +
              "id=" + id +
              ", nombre='" + nombre + '\'' +
              ", email='" + email + '\'' +
              '}';
   }
}
package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Ubicacion

interface UbicacionService {

    fun mover(vectorId: Int, nombreUbicacion: String)
    fun expandir(nombreUbicacion: String)
    fun actualizar(ubicacion : Ubicacion)

    /* Operaciones CRUD*/
    fun crearUbicacion(nombreDeLaUbicacion: String): Ubicacion
    fun recuperar(ubicacion: String) : Ubicacion
}
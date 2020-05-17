package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Ubicacion

interface UbicacionDAO {

    fun crear(ubicacion: Ubicacion): Ubicacion
    fun recuperar(nombreDeLaUbicacion: String): Ubicacion
    fun actualizar(ubicacion: Ubicacion)
    fun nomEspecieMasInfecciosa(nombreDeLaUbicacion: String) : String
    fun cantVectoresInfectados(nombreDeLaUbicacion: String) : Int
}
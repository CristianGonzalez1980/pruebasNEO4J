package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector

interface UbicacionService {

    fun mover(vectorId: Int, nombreUbicacion: String)
    //ver el tema de las nuevas excepciones
    //Arroje una excepción UbicacionMuyLejana si no es posible llegar desde la actual ubicación del vector a la nueva por medio de un camino.
    //Arrojar una excepción UbicacionNoAlcanzable si se intenta mover a un vector a través de un tipo de camino que no puede atravesar.

    fun expandir(nombreUbicacion: String)

    fun actualizar(ubicacion: Ubicacion)

    /* Operaciones CRUD*/
    fun crearUbicacion(nombreDeLaUbicacion: String): Ubicacion
    //en ambas bbdd --HECHO

    fun recuperar(ubicacion: String): Ubicacion

    fun conectar(ubicacion1: String, ubicacion2: String, tipoCamino: String)
    //que conecte dos ubicaciones (se asumen preexistentes) por medio de un tipo de camino.--HECHO

    fun conectados(nombreDeUbicacion: String): List<Ubicacion>
    //que dado el nombre de una ubicacion, retorne todos las ubicaciones conectadas al la ubicacion dada por cualquier tipo de camino.--HECHO

    fun moverMasCorto(vectorId: Long, nombreDeUbicacion: String)
    //que funcione como el mover, pero que el vector intente llegar a la ubicación dada en la menor cantidad de movimientos. Hay que tener en cuenta que: -No todos los tipos de vectores pueden moverse por los mismos tipos de caminos. -De no poder llegar de ninguna forma a la ubicacion destino se debera lanzar la excepcion UbicionNoAlcanzable

    fun capacidadDeExpansion(vectorId: Long, nombreDeUbicacion: String, movimientos: Int): Int
    //que dado un vector, retorna la cantidad de diferentes ubicaciones a las que podria moverse el Vector dada una cierta cantidad de movimientos.
    fun estanConectadasPorCamino(nombreUbicacionBase: String, nombreUbicacionDestino: String, nombreTipoCamino: String): Boolean

    fun tipoCaminoEntre(nombreUbicacionBase: String, nombreUbicacionDestino: String): String

    fun ubicacionDeVector(vector: Vector): Ubicacion

    fun existeUbicacion(ubicacionCreada: Ubicacion): Boolean
}
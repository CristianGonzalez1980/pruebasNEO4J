package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno

interface PatogenoDAO {
    fun crear(patogeno: Patogeno): Int
    fun actualizar(patogeno: Patogeno)
    fun recuperar(idDelPatogeno: Int): Patogeno
    fun recuperarATodos() : List<Patogeno>
    fun agregarEspecie(idPatogeno: Int, nombreEspecie: String, paisDeOrigen: String, adn: Int): Especie
    fun recuperarEspecie(id: Int): Especie
    fun cantidadDeInfectados(especieId: Int): Int

}
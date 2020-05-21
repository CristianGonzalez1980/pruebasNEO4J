package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno

interface PatogenoDAO {
    fun crear(patogeno: Patogeno): Int
    fun actualizar(patogeno: Patogeno)
    fun recuperar(idDelPatogeno: Int): Patogeno
    fun recuperarATodos() : List<Patogeno>
    fun agregarEspecie(patogeno: Patogeno,  especie: Especie): Especie

    /* Operaciones sobre Especie*/
    fun actualizar(especie: Especie)
    fun recuperarEspecie(id: Int): Especie
    fun cantidadDeInfectados(especieId: Int): Int
    fun esPandemia(especieId: Int) : Boolean
    fun lideresSobreHumanos() : List<Especie>
    fun especieLider() : Especie

}
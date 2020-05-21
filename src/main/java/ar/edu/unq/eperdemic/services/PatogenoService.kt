package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

interface PatogenoService {

    fun crearPatogeno(patogeno: Patogeno): Int
    fun recuperarPatogeno(id: Int): Patogeno
    fun recuperarATodosLosPatogenos(): Collection<Patogeno>
    fun agregarEspecie(id: Int, nombreEspecie: String, paisDeOrigen : String, adn: Int) : Especie

    fun cantidadDeInfectados (especieId: Int) : Int
    fun esPandemia (especieId: Int) : Boolean
    fun recuperarEspecie(id: Int): Especie
}
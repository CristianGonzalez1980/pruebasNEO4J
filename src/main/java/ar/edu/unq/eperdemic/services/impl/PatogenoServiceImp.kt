package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType

class PatogenoServiceImp(
        private val patogenoDAO: PatogenoDAO,
        private val dataDAO: DataDAO

) : PatogenoService {

    override fun recuperarEspecie(id: Int): Especie {
        return runTrx({ patogenoDAO.recuperarEspecie(id) }, listOf(TransactionType.HIBERNATE))
    }

    override fun esPandemia(especieId: Int): Boolean {

        return runTrx({ patogenoDAO.esPandemia(especieId) }, listOf(TransactionType.HIBERNATE))
    }

    override fun cantidadDeInfectados(especieId: Int): Int {

        return runTrx({ patogenoDAO.cantidadDeInfectados(especieId) }, listOf(TransactionType.HIBERNATE))
    }

    override fun agregarEspecie(id: Int, nombreEspecie: String, paisDeOrigen: String, adn: Int): Especie {
        var patogeno = runTrx({ patogenoDAO.recuperar(id) }, listOf(TransactionType.HIBERNATE))
        val especie = patogeno.agregarEspecie(nombreEspecie, paisDeOrigen, adn)
        return runTrx({ patogenoDAO.agregarEspecie(patogeno, especie) }, listOf(TransactionType.HIBERNATE))
    }

    override fun crearPatogeno(patogeno: Patogeno): Int {
        return runTrx({ patogenoDAO.crear(patogeno) }, listOf(TransactionType.HIBERNATE))
    }

    override fun recuperarPatogeno(id: Int): Patogeno {
        return runTrx({ patogenoDAO.recuperar(id) }, listOf(TransactionType.HIBERNATE))
    }

    override fun recuperarATodosLosPatogenos(): Collection<Patogeno> {
        return runTrx({ patogenoDAO.recuperarATodos() }, listOf(TransactionType.HIBERNATE))
    }
}

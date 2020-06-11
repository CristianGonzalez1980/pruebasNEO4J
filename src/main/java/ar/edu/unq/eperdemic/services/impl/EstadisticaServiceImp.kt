
package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.ReporteDeContagios
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.EstadisticasService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType

class EstadisticaServiceImp(private val patogenoDAO: PatogenoDAO, private val ubicacionDAO: UbicacionDAO, private val ubicacionServiceImp: UbicacionServiceImp) : EstadisticasService {

    override fun especieLider(): Especie {
        return runTrx ({ patogenoDAO.especieLider() }, listOf(TransactionType.HIBERNATE))
    }

    override fun lideres(): List<Especie> {
        return runTrx ({ patogenoDAO.lideresSobreHumanos() }, listOf(TransactionType.HIBERNATE))
    }

    override fun reporteDeContagios(nombreUbicacion: String): ReporteDeContagios {
        val presentes: Int = runTrx ({ ubicacionDAO.cantVectoresPresentes(nombreUbicacion) }, listOf(TransactionType.HIBERNATE))
        val infectados: Int = runTrx ({ ubicacionDAO.cantVectoresInfectados(nombreUbicacion) }, listOf(TransactionType.HIBERNATE))
        val masInfecciosa: String = runTrx ({ ubicacionDAO.nomEspecieMasInfecciosa(nombreUbicacion) }, listOf(TransactionType.HIBERNATE))

        return ReporteDeContagios(presentes, infectados, masInfecciosa)
    }
}


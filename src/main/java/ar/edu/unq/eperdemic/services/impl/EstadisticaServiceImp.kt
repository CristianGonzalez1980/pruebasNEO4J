package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.ReporteDeContagios
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.EstadisticasService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class EstadisticaServiceImp(private val patogenoDAO: PatogenoDAO, private val ubicacionDAO: UbicacionDAO, private val ubicacionServiceImp: UbicacionServiceImp) : EstadisticasService {

    override fun especieLider(): Especie {
        return runTrx { patogenoDAO.especieLider() }
    }

    override fun lideres(): List<Especie> {
        return runTrx { patogenoDAO.lideresSobreHumanos() }
    }

    override fun reporteDeContagios(nombreUbicacion: String): ReporteDeContagios {
        val presentes: Int = ubicacionServiceImp.recuperar(nombreUbicacion).vectores.size
        val infectados: Int = runTrx { ubicacionDAO.cantVectoresInfectados(nombreUbicacion) }
        val masInfecciosa: String = runTrx { ubicacionDAO.nomEspecieMasInfecciosa(nombreUbicacion) }

        return ReporteDeContagios(presentes, infectados, masInfecciosa)
    }
}

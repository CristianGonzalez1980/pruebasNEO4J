package ar.edu.unq.eperdemic.services.runner

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.ReporteDeContagios
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateEspecieDAO
import ar.edu.unq.eperdemic.services.EstadisticasService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class EstadisticaServiceImp(private val especieDAO: EspecieDAO, private val ubicacionDAO: UbicacionDAO, private val ubicacionServiceImp: UbicacionServiceImp) : EstadisticasService {

    override fun especieLider(): Especie {
        return runTrx { especieDAO.especieLider() }
    }

    override fun lideres(): List<Especie> {
        return runTrx { especieDAO.lideresSobreHumanos() }
    }

    override fun reporteDeContagios(nombreUbicacion: String): ReporteDeContagios {
        var presentes: Int = ubicacionServiceImp.recuperar(nombreUbicacion).vectores.size
        var infectados: Int = runTrx { ubicacionDAO.cantVectoresInfectados(nombreUbicacion) }
        var masInfecciosa: String = runTrx { ubicacionDAO.nomEspecieMasInfecciosa(nombreUbicacion) }

        return ReporteDeContagios(presentes, infectados, masInfecciosa)
    }

}

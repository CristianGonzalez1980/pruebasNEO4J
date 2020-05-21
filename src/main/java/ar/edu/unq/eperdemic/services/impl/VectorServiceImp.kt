package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx


class VectorServiceImp(
        private val vectorDAO: VectorDAO,
        private val dataDAO: DataDAO,
        private val patogenoDAO: PatogenoDAO
) : VectorService {

    override fun actualizar(vector: Vector): Vector {
        return runTrx {
            vectorDAO.actualizar(vector)
        }
    }

    override fun contagiar(vectorInfectado: Vector, vectores: List<Vector>) {
        var vecAInfect: Vector
        for (vectorAInfect: Vector in vectores) {
            vecAInfect = vectorAInfect
            if (vecAInfect != vectorInfectado) {
                vectorInfectado.estrategiaDeContagio!!.darContagio(vectorInfectado, vecAInfect)
                runTrx { vectorDAO.actualizar(vecAInfect) }
            }
        }
    }

    override fun infectar(vector: Vector, especie: Especie) {
        runTrx {
            vector.estrategiaDeContagio!!.infectar(vector, especie)
            vectorDAO.actualizar(vector)
        }
    }

    override fun enfermedades(vectorId: Int): List<Especie> {
        return runTrx {
            vectorDAO.enfermedades(vectorId)
        }
    }

    override fun crearVector(vector: Vector): Vector {
        return runTrx {
            vectorDAO.crearVector(vector)
        }
    }

    override fun recuperarVector(vectorId: Int): Vector {
        return runTrx { vectorDAO.recuperar(vectorId) }
    }

    override fun borrarVector(vectorId: Int) {
        runTrx { vectorDAO.eliminar(vectorId) }
    }
}
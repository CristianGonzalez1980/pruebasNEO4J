package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType


class VectorServiceImp(
        private val vectorDAO: VectorDAO,
        private val dataDAO: DataDAO,
        private val patogenoDAO: PatogenoDAO
) : VectorService {

    override fun actualizar(vector: Vector): Vector {
        return runTrx({
            vectorDAO.actualizar(vector)
        }, listOf(TransactionType.HIBERNATE))
    }

    override fun contagiar(vectorInfectado: Vector, vectores: List<Vector>) {
        var vecAInfect: Vector
        for (vectorAInfect: Vector in vectores) {
            vecAInfect = vectorAInfect
            if (vecAInfect != vectorInfectado) {
                vectorInfectado.estrategiaDeContagio!!.darContagio(vectorInfectado, vecAInfect)
                runTrx({ vectorDAO.actualizar(vecAInfect) }, listOf(TransactionType.HIBERNATE))
            }
        }
    }

    override fun contagiarSimulPositivo(vectorInfectado: Vector, vectores: List<Vector>) {
        var vecAInfect: Vector
        for (vectorAInfect: Vector in vectores) {
            vecAInfect = vectorAInfect
            if (vecAInfect != vectorInfectado) {
                vectorInfectado.estrategiaDeContagio!!.darContagioSimularPositivo(vectorInfectado, vecAInfect)
                runTrx({ vectorDAO.actualizar(vecAInfect) }, listOf(TransactionType.HIBERNATE))
            }
        }
    }

    override fun contagiarSimulNegativo(vectorInfectado: Vector, vectores: List<Vector>) {
        var vecAInfect: Vector
        for (vectorAInfect: Vector in vectores) {
            vecAInfect = vectorAInfect
            if (vecAInfect != vectorInfectado) {
                vectorInfectado.estrategiaDeContagio!!.darContagioSimularNegativo(vectorInfectado, vecAInfect)
                runTrx({ vectorDAO.actualizar(vecAInfect) }, listOf(TransactionType.HIBERNATE))
            }
        }
    }

    override fun infectar(vector: Vector, especie: Especie) {
        runTrx({
            vector.estrategiaDeContagio!!.infectar(vector, especie)
            vectorDAO.actualizar(vector)
        }, listOf(TransactionType.HIBERNATE))
    }

    override fun enfermedades(vectorId: Int): List<Especie> {
        return runTrx({
            vectorDAO.enfermedades(vectorId)
        }, listOf(TransactionType.HIBERNATE))
    }

    override fun crearVector(vector: Vector): Vector {
        return runTrx({
            vectorDAO.crearVector(vector)
        }, listOf(TransactionType.HIBERNATE))
    }

    override fun recuperarVector(vectorId: Int): Vector {
        return runTrx({ vectorDAO.recuperar(vectorId) }, listOf(TransactionType.HIBERNATE))
    }

    override fun borrarVector(vectorId: Int) {
        runTrx({ vectorDAO.eliminar(vectorId) }, listOf(TransactionType.HIBERNATE))
    }
}
package ar.edu.unq.eperdemic.services.runner

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyHumano
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategySuperClase
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateEspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import java.lang.RuntimeException

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
        runTrx {
            //val vectorInf: Vector = vectorDAO.recuperar(vectorInfectado.id!!.toInt())
            for (vectorAInfect: Vector in vectores) {
                //val vectorAContagiar = vectorDAO.recuperar(vectorAInfect.id!!.toInt())
                //vectorInf.estrategiaDeContagio!!.darContagio(vectorInf, vectorAContagiar)
                vectorInfectado.estrategiaDeContagio!!.darContagio(vectorInfectado, vectorAInfect)
                vectorDAO.actualizar(vectorAInfect)
            }
            //vectorDAO.actualizar(vectorInf)
        }
    }

    override fun infectar(vector: Vector, especie: Especie) {
        runTrx {
            var especieRec: Especie = patogenoDAO.recuperarEspecie(especie.owner!!.id!!.toInt())

            //vectorDAO.agregarEnfermedad(vector.id!!.toInt(),especieRec)
            var vectorRec: Vector = vectorDAO.recuperar(vector.id!!.toInt())
            vector.estrategiaDeContagio!!.infectar(vectorRec, especieRec)
            vectorDAO.actualizar(vectorRec)
            //entiendo que la especie se actualizaaaaa*/
            /*vector.agregarEnfermedad(especieRec)
            especie.agregarVector(vectorRec)
            vectorDAO.actualizar(vectorRec)*/
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

    fun clear() {
        runTrx { dataDAO.clear() }
    }
}
package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.Especie
import java.util.*
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO

open class StrategySuperClase() {

    open fun darContagio(vectorDAO: VectorDAO,vectorInfectado: Vector, vectorAContagiar: Vector) {
        val enfermedades: MutableSet<Especie> = vectorInfectado.enfermedades
        for (e: Especie in enfermedades) {
            val factorContagio = e.owner?.capacidadContagio
            val porcentajeDeContagioExitoso = 5 + factorContagio!!
            if ((porcentajeDeContagioExitoso > 70) and (!vectorAContagiar.enfermedades.contains(e))) {
                this.infectar(vectorDAO,vectorAContagiar, e)
            }
        }
    }

    open fun infectar(vectorDAO: VectorDAO,vector: Vector, especie: Especie) {

            vectorDAO.agregarEnfermedad(vector.id!!.toInt(), especie)

           /*vector.agregarEnfermedad(especie)
           especie.agregarVector(vector)
            //vector.enfermedades.add(especie)
            //especie.vectores.add(vector)*/
    }
}
package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.Especie
import java.util.*
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO

open class StrategySuperClase() {

    open fun darContagio(vectorInfectado: Vector, vectorAContagiar: Vector) {
        val enfermedades: MutableSet<Especie> = vectorInfectado.enfermedades
        for (e: Especie in enfermedades) {
            val factorContagio = e.owner?.capacidadContagio
            val porcentajeDeContagioExitoso = 5 + factorContagio!!
            if ((porcentajeDeContagioExitoso > 70) and (!vectorAContagiar.enfermedades.contains(e))) {
                this.infectar(vectorAContagiar, e)
            }
        }
    }

    fun infectar(vector: Vector, especie: Especie) {
        vector.enfermedades.add(especie)
        especie.vectores.add(vector)
        especie.sumarAdn()
    }
}
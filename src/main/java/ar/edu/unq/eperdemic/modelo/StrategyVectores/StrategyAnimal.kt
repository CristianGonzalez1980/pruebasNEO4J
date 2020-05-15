package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO

class StrategyAnimal : StrategySuperClase() {
    fun poneEnRiesgoA(vectorRecibido: Vector): Boolean {
        return ((vectorRecibido.tipo!!.name == "Persona")
                || (vectorRecibido.tipo!!.name == "Insecto"))
    }

    override fun darContagio( vectorDAO: VectorDAO,vectorInfectado: Vector, vectorAContagiar: Vector) {
        if (this.poneEnRiesgoA(vectorAContagiar)) {
            super.darContagio(vectorDAO,vectorInfectado, vectorAContagiar)
        }
    }
}
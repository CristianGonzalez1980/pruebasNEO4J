package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Vector

class StrategyAnimal : StrategySuperClase() {
    fun poneEnRiesgoA(vectorRecibido: Vector): Boolean {
        return ((vectorRecibido.tipo!!.name == "Persona")
                || (vectorRecibido.tipo!!.name == "Insecto"))
    }

    fun puedeAtravesar(camino: TipoDeCamino) : Boolean {
        return ((camino.name == "Terrestre") || (camino.name == "Maritimo") || (camino.name == "Aereo"))
    }

    override fun darContagio(vectorInfectado: Vector, vectorAContagiar: Vector) {
        if (this.poneEnRiesgoA(vectorAContagiar)) {
            super.darContagio(vectorInfectado, vectorAContagiar)
        }
    }
}
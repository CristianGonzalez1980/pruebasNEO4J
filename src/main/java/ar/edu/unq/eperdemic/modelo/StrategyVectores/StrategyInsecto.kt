package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Vector


class StrategyInsecto : StrategySuperClase() {
    fun poneEnRiesgoA(vectorRecibido: Vector): Boolean {
        return ((vectorRecibido.tipo!!.name == "Animal")
                || (vectorRecibido.tipo!!.name == "Persona"))
    }

    fun puedeAtravesar(camino: TipoDeCamino) : Boolean {
        return ((camino.name == "Terrestre") || (camino.name == "Aereo"))
    }

    override fun darContagio(vectorInfectado: Vector, vectorAContagiar: Vector) {
        if (this.poneEnRiesgoA(vectorAContagiar)) {
            super.darContagio(vectorInfectado, vectorAContagiar)
        }
    }
}
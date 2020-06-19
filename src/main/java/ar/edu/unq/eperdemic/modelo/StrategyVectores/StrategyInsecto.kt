package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Vector


class StrategyInsecto : StrategySuperClase() {
    fun poneEnRiesgoA(vectorRecibido: Vector): Boolean {
        return ((vectorRecibido.tipo!!.name == "Animal")
                || (vectorRecibido.tipo!!.name == "Persona"))
    }

    override fun puedeAtravesar(): List<String> {
        return listOf("Terrestre", "Aereo")
    }

    override fun caminosDeVector(): String {
        return ":Terrestre|Aereo*"
    }

    override fun darContagio(vectorInfectado: Vector, vectorAContagiar: Vector) {
        if (this.poneEnRiesgoA(vectorAContagiar)) {
            super.darContagio(vectorInfectado, vectorAContagiar)
        }
    }

    override fun darContagioSimularPositivo(vectorInfectado: Vector, vectorAContagiar: Vector) {
        if (this.poneEnRiesgoA(vectorAContagiar)) {
            super.darContagioSimularPositivo(vectorInfectado, vectorAContagiar)
        }
    }

    override fun darContagioSimularNegativo(vectorInfectado: Vector, vectorAContagiar: Vector) {
        if (this.poneEnRiesgoA(vectorAContagiar)) {
            super.darContagioSimularNegativo(vectorInfectado, vectorAContagiar)
        }
    }
}
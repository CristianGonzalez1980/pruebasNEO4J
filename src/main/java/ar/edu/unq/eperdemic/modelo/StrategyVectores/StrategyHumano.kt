package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Vector

class StrategyHumano : StrategySuperClase() {
    fun poneEnRiesgoA(vectorRecibido: Vector): Boolean {
        return ((vectorRecibido.tipo!!.name == "Persona")
                || (vectorRecibido.tipo!!.name == "Insecto"))
    }

    fun puedeAtravesar(camino: TipoDeCamino) : Boolean {
        return ((camino.name == "Terrestre") || (camino.name == "Maritimo"))
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

    fun puedePasarPor(tipoDeCamino: String): Boolean {
        return ( (tipoDeCamino == "Terrestre") or (tipoDeCamino == "Maritimo"))
    }
}
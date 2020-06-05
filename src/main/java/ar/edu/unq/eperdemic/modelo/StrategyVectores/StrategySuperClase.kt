package ar.edu.unq.eperdemic.modelo.StrategyVectores

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Random
import ar.edu.unq.eperdemic.modelo.Vector

open class StrategySuperClase() {

    open fun darContagio(vectorInfectado: Vector, vectorAContagiar: Vector) {
        val simulRandomEntre0y100 = 0 //Esto simula un random entre 0 y 100 que es el que determina si entra en el porcentaje
        //de probabilidad o no, esta hecho de esta manera para que siempre entre en contagio y poder testear esos casos positivos de contagio.
        val enfermedades: MutableSet<Especie> = vectorInfectado.enfermedades
        for (e: Especie in enfermedades) {
            val factorContagio = e.owner?.capacidadContagio
            val porcentajeDeContagioExitoso = Random().giveRandom() + factorContagio!!
            if ((simulRandomEntre0y100 <= porcentajeDeContagioExitoso) and (!vectorAContagiar.enfermedades.contains(e))) {
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
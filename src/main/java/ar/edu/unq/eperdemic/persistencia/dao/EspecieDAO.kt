package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie

interface EspecieDAO {
    fun recuperarEspecie(id: Int): Especie
    fun actualizar(especie: Especie)
    fun lideresSobreHumanos() : List<Especie>
    fun especieLider() : Especie
}

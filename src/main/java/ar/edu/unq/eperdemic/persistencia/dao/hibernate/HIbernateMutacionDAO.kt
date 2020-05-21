package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

open class HibernateMutacionDAO : HibernateDAO<Mutacion>(Mutacion::class.java), MutacionDAO {

    override fun crear(mutacion: Mutacion): Mutacion {
        this.guardar(mutacion)
        return mutacion
    }

    override fun recuperarMut(mutacionId: Int): Mutacion {
        return this.recuperar(mutacionId.toLong())
    }
}
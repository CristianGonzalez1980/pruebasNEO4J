package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO

open class HibernateMutacionDAO : HibernateDAO<Mutacion>(Mutacion::class.java), MutacionDAO {

    override fun crear(mutacion: Mutacion): Mutacion {
        this.guardar(mutacion)
        return  (this.recuperar(mutacion.id))
    }

    override fun recuperarMut(mutacionId: Int): Mutacion {
        return this.recuperar(mutacionId.toLong())
    }
}
package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.services.runner.HibernateTransaction



open class HibernateDAO<T>(private val entityType: Class<T>) {


    fun guardar(item: T) {
        val session = HibernateTransaction.currentSession
        session.save(item)
    }

    fun recuperar(id: Long?): T {
        val session = HibernateTransaction.currentSession
        return session.get(entityType, id)
    }
}
package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.runner.HibernateTransaction

open class HibernateUbicacionDAO : HibernateDAO<Ubicacion>(Ubicacion::class.java), UbicacionDAO {

    override fun crear(ubicacion: Ubicacion): Ubicacion {
        
        this.guardar(ubicacion)
        return this.recuperar(ubicacion.nombreDeLaUbicacion!!)
    }

    override fun recuperar(nombreDeLaUbicacion: String): Ubicacion {
        val session = HibernateTransaction.currentSession
        val hql = ("from ubicacion " + " where nombreDeLaUbicacion = :id")
        val query = session.createQuery(hql, Ubicacion::class.java)
        query.setParameter("id", nombreDeLaUbicacion)
        return query.singleResult
    }

    override fun actualizar(ubicacion: Ubicacion) {
        val session = HibernateTransaction.currentSession
        session.saveOrUpdate(ubicacion)
    }

    override fun nomEspecieMasInfecciosa(nombreDeLaUbicacion: String): String {
        val session = HibernateTransaction.currentSession
        val hql = """
            select especie 
            from especie especie
                join especie.vectores v where v.location.nombreDeLaUbicacion = :id
                group by especie
                order by count(v) desc
         """
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("id", nombreDeLaUbicacion)
        query.maxResults = 1
        return query.singleResult.toString()
    }

    override fun cantVectoresPresentes(nombreDeLaUbicacion: String): Int {
        val session = HibernateTransaction.currentSession
        val hql = """
            select count(v)
            from ubicacion ubicacion
                join ubicacion.vectores v where ubicacion.nombreDeLaUbicacion = :id
         """
        val query = session.createQuery(hql, Number::class.java)
        query.setParameter("id", nombreDeLaUbicacion)
        return query.singleResult.toInt()
    }

    override fun cantVectoresInfectados(nombreDeLaUbicacion: String): Int {
        val session = HibernateTransaction.currentSession
        val hql = """
            select count(distinct vector.id)
            from vector vector
                join vector.enfermedades e where vector.location.nombreDeLaUbicacion = :id
         """
        val query = session.createQuery(hql, Number::class.java)
        query.setParameter("id", nombreDeLaUbicacion)
        return query.singleResult.toInt()
    }
}
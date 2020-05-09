package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner


open class HibernateVectorDAO : HibernateDAO<Vector>(Vector::class.java), VectorDAO {

    override fun recuperar(idDelVector: Int): Vector {
        val vector = this.recuperar(idDelVector.toLong())
        vector.initEstrategia()
        return vector
    }

    override fun recuperarEnfermedades(idDelVector: Int): MutableSet<Especie> {
        val session = TransactionRunner.currentSession
        val hql = ("select enfermedades_nombre from vector_especie where vector_id = :idVector")
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("idVector", idDelVector)
        return query.resultList.toMutableSet()
    }

    override fun crearVector(vector: Vector): Vector {
        this.guardar(vector)
        return (this.recuperar(vector.id))
    }

    override fun eliminar(idDelVector: Int) {
        val session = TransactionRunner.currentSession
        val hql = ("delete from vector where id = :idDelVector")
        val query = session.createQuery(hql, Vector::class.java)
        query.setParameter("idDelVector", idDelVector)
        query.executeUpdate()
    }

    override fun actualizar(vector: Vector): Vector {
        val session = TransactionRunner.currentSession
        session.saveOrUpdate(vector)
        return this.recuperar(vector.id)
    }

/*    override fun recuperarVectores(ciudad : String) : MutableList<Vector> {
        val session = TransactionRunner.currentSession
        val hql = ("from vector v " + "where location_nombreDeLaUbicacion = :ciudad")
        val query = session.createQuery(hql, Vector::class.java)
        query.setParameter("ciudad", ciudad)
        return query.resultList.toMutableSet().toMutableList()
    }*/
}
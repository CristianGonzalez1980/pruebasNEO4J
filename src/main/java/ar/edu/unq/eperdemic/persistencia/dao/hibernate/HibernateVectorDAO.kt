package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.runner.HibernateTransaction


open class HibernateVectorDAO : HibernateDAO<Vector>(Vector::class.java), VectorDAO {

    override fun recuperar(idDelVector: Int): Vector {
        val vector = this.recuperar(idDelVector.toLong())
        vector.initEstrategia()
        return vector
    }

    override fun enfermedades(idDelVector: Int): List<Especie> {
        val session = HibernateTransaction.currentSession
        val hql = """
        select enfermedad 
        from vector v join v.enfermedades enfermedad where v.id = :idVector"""
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("idVector", idDelVector.toLong())
        //return query.resultList.toMutableSet()
        return query.resultList
    }

    override fun crearVector(vector: Vector): Vector {
        this.guardar(vector)
        return (this.recuperar(vector.id))
    }

    override fun agregarEnfermedad(vectorId: Int, especie: Especie) {
        val vectorRec: Vector = this.recuperar(vectorId)
        vectorRec.estrategiaDeTipo!!.infectar(vectorRec, especie)
        this.actualizar(vectorRec)
    }

    override fun actualizar(vector: Vector): Vector {
        val session = HibernateTransaction.currentSession
        session.saveOrUpdate(vector)
        return this.recuperar(vector.id)
    }

    override fun eliminar(idDelVector: Int) {
        val vector = this.recuperar(idDelVector)
        val session = HibernateTransaction.currentSession
        vector.location!!.desAlojarVector(vector)
        vector.enfermedades.map { it.vectores.remove(vector) }
        session.delete(vector)
    }
/*    override fun eliminar(idDelVector: Int) {
        val session = TransactionRunner.currentSession
        val hql = ("delete from vector where id = :idDelVector")
        val query = session.createQuery(hql, Vector::class.java)
        query.setParameter("idDelVector", idDelVector)
        query.executeUpdate()
    }*/
}
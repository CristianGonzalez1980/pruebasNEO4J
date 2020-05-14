package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner


open class HibernatePatogenoDAO : HibernateDAO<Patogeno>(Patogeno::class.java), PatogenoDAO {

    override fun recuperarEspecie(id: Int): Especie {

        var patogeno = this.recuperar(id)
        val session = TransactionRunner.currentSession
        val hql = """ select especie from patogeno p join p.especies especie  where especie.owner = :unPatogeno"""
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unPatogeno", patogeno)
        query.maxResults = 1
        return query.singleResult

    }

    override fun cantidadDeInfectados(especieId: Int) : Int{

        val especie = this.recuperarEspecie(especieId)
        val session = TransactionRunner.currentSession
        val hql = """ 
                  select vectores_id
                  from especie e join especie_vector  on e.owner = v.enfermedades_id where e.owner = :unPatogeno 

                  """

        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unPatogeno", especie.owner)
        val res = query.resultList.size
        return  res
    }

    override fun agregarEspecie(idPatogeno: Int, nombreEspecie: String, paisDeOrigen: String, adn: Int): Especie {
        var patogeno = this.recuperar(idPatogeno)
        val especie = Especie(patogeno,nombreEspecie,paisDeOrigen, adn)
        patogeno.agregarEspecie(especie)
        val session = TransactionRunner.currentSession
        session.save(especie)
        return especie
    }

    override fun recuperarATodos(): List<Patogeno> {
        val session = TransactionRunner.currentSession
        val hql = ("from patogeno p " + "order by p.tipo asc")
        val query = session.createQuery(hql, Patogeno::class.java)
        return query.resultList
    }

    override fun recuperar(idDelPatogeno: Int): Patogeno {
        return this.recuperar(idDelPatogeno.toLong())
    }

    override fun actualizar(unPatogeno: Patogeno) {
        val session = TransactionRunner.currentSession
        session.saveOrUpdate(unPatogeno)
    }

    override fun crear(patogeno: Patogeno): Int {
        this.guardar(patogeno)
        return (this.recuperar(patogeno.id).id!!.toInt())
    }
}

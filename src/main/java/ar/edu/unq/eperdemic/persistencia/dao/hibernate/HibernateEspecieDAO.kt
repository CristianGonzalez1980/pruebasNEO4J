package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

open class HibernateEspecieDAO : HibernateDAO<Especie>(Especie::class.java), EspecieDAO {

    override fun recuperarEspecie(id: Int): Especie {
        return this.recuperar(id.toLong())
    }

    override fun actualizar(especie: Especie) {
        val session = TransactionRunner.currentSession
        session.saveOrUpdate(especie)
    }

    override fun lideresSobreHumanos(): List<Especie> {//retorna las diez primeras especies que hayan infecatado la mayor cantidad total de vectores humanos y animales combinados en orden descendente.
        val session = TransactionRunner.currentSession
        val hql = """
            select especie
            from especie especie
                join especie.vectores v where v.tipo = 2 and especie in (select especie
            from especie especie
                join especie.vectores v where v.tipo = 0
                group by especie)
                group by especie
                order by count(especie) desc
        """
        val query = session.createQuery(hql, Especie::class.java)
        query.maxResults = 10
        return query.resultList
    }

    override fun especieLider(): Especie {//retorna la especie que haya infectado a más humanos
        val session = TransactionRunner.currentSession
        val hql = """
            select especie 
            from especie especie
                join especie.vectores v where v.tipo = 0
                group by especie
                order by count(especie) desc
         """
        val query = session.createQuery(hql, Especie::class.java)
        query.maxResults = 1
        return query.singleResult
    }
}

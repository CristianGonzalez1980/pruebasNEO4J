package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
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
        val persona = VectorFrontendDTO.TipoDeVector.Persona
        val animal = VectorFrontendDTO.TipoDeVector.Animal
        val session = TransactionRunner.currentSession
        val hql = """
            
            select e
            from especie e
                join e.vectores v where v.tipo = :unTipo 
                and e in (select e
            from especie e
                join e.vectores v where v.tipo = :otroTipo
                group by e)
                group by e
                order by count(v) desc
        """

        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unTipo",persona)
        query.setParameter("otroTipo",animal)
        query.maxResults = 10
        return query.resultList
    }

    override fun especieLider(): Especie {//retorna la especie que haya infectado a más humanos
        val persona = VectorFrontendDTO.TipoDeVector.Persona
        val session = TransactionRunner.currentSession
        val hql = """
            select  e  
            from especie e
                join e.vectores v where v.tipo = :unTipo
                group by e
                order by count(v) desc
                
         """
        //
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unTipo",persona)
        query.maxResults = 1
        return query.singleResult
    }
}

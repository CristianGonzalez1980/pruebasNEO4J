package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.runner.HibernateTransaction



open class HibernatePatogenoDAO : HibernateDAO<Patogeno>(Patogeno::class.java), PatogenoDAO {

    override fun crear(patogeno: Patogeno): Int {
        this.guardar(patogeno)
        return (this.recuperar(patogeno.id).id!!.toInt())
    }

    override fun recuperar(idDelPatogeno: Int): Patogeno {
        return this.recuperar(idDelPatogeno.toLong())
    }

    override fun actualizar(unPatogeno: Patogeno) {
        val session = HibernateTransaction.currentSession
        session.saveOrUpdate(unPatogeno)
    }

    override fun recuperarATodos(): List<Patogeno> {
        val session = HibernateTransaction.currentSession
        val hql = ("from patogeno p " + "order by p.tipo asc")
        val query = session.createQuery(hql, Patogeno::class.java)
        return query.resultList
    }

    override fun agregarEspecie(patogeno: Patogeno, especie: Especie): Especie {
        val session = HibernateTransaction.currentSession
        session.saveOrUpdate(patogeno)
        session.saveOrUpdate(especie)
        val hql = ("from especie e " + "where e.nombre = :unNombre")
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unNombre", especie.nombre)

        return query.singleResult
    }

    override fun recuperarEspecie(id: Int): Especie {

        val session = HibernateTransaction.currentSession
        val hql = ("from especie e " + "where e.id = :unId")
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unId", id.toLong())
        return query.singleResult
    }

    override fun actualizar(especie: Especie) {
        val session = HibernateTransaction.currentSession
        session.saveOrUpdate(especie)
    }

    override fun lideresSobreHumanos(): List<Especie> {//retorna las diez primeras especies que hayan infecatado la mayor cantidad total de vectores humanos y animales combinados en orden descendente.
        val persona = VectorFrontendDTO.TipoDeVector.Persona
        val animal = VectorFrontendDTO.TipoDeVector.Animal
        val session = HibernateTransaction.currentSession
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
        query.setParameter("unTipo", persona)
        query.setParameter("otroTipo", animal)
        query.maxResults = 10
        return query.resultList
    }

    override fun especieLider(): Especie {//retorna la especie que haya infectado a más humanos
        val persona = VectorFrontendDTO.TipoDeVector.Persona
        val session = HibernateTransaction.currentSession
        val hql = """
            select  e  
            from especie e
                join e.vectores v where v.tipo = :unTipo
                group by e
                order by count(v) desc
                
         """
        //
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unTipo", persona)
        query.maxResults = 1
        return query.singleResult
    }

    override fun cantidadDeInfectados(especieId: Int): Int {

        val especie = this.recuperarEspecie(especieId)
        val session = HibernateTransaction.currentSession
        val hql = """ 
                  select count(distinct vector.id)
                  from especie e join e.vectores vector  where e = :unaEspecie 

                  """

        val query = session.createQuery(hql, Number::class.java)
        query.setParameter("unaEspecie", especie)
        return query.singleResult.toInt()
    }

    override fun esPandemia(especieId: Int): Boolean {

        var especie = this.recuperarEspecie(especieId)

        val session = HibernateTransaction.currentSession
        val hql1 = """ select count ( distinct v.location)
                 from especie e join e.vectores v where e.id = :unId"""
        val query1 = session.createQuery(hql1, Number::class.java)
        query1.setParameter("unId", especie.id!!.toLong())
        var ubicaciones = query1.singleResult.toInt()

        val hql2 = """ select count(*) from ubicacion"""
        val query2 = session.createQuery(hql2, Number::class.java)
        var cantUbicacion = query2.singleResult.toInt() / 2

        return ubicaciones > cantUbicacion
    }
}



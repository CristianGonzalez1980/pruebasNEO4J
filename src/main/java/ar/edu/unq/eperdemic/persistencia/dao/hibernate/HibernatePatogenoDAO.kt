package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner


open class HibernatePatogenoDAO : HibernateDAO<Patogeno>(Patogeno::class.java), PatogenoDAO {

    override fun recuperarEspecie(id: Int): Especie {

        val session = TransactionRunner.currentSession
        val hql = """ select especie from patogeno p join p.especies especie  where especie.id =:unId"""
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unId",id.toLong())
        return query.singleResult

    }

    override fun cantidadDeInfectados(especieId: Int) : Int{

        val especie = this.recuperarEspecie(especieId)
        val session = TransactionRunner.currentSession
        val hql = """ 
                  select vector
                  from especie e join e.vectores vector  where e = :unaEspecie 

                  """

        val query = session.createQuery(hql, Vector::class.java)
        query.setParameter("unaEspecie", especie)
        val res = query.resultList.size
        return  res
    }

    override fun esPandemia(especieId: Int): Boolean {

        var especie = this.recuperarEspecie(especieId)

        val session = TransactionRunner.currentSession
        val hql1 = """ select distinct (v.location)
                 from especie e join e.vectores v where e.id = :unId"""
        val query1 = session.createQuery(hql1, Ubicacion::class.java)
        query1.setParameter("unId", especie.id!!.toLong())
        var ubicaciones =  query1.resultList.size

        val hql2 = "from ubicacion u " + " order by u.id"
        val query2 = session.createQuery(hql2, Ubicacion::class.java)
        var cantUbicacion = (query2.resultList.size) / 2


        return ubicaciones > cantUbicacion


    }

    override fun agregarEspecie(idPatogeno: Int, nombreEspecie: String, paisDeOrigen: String, adn: Int): Especie {
        var patogeno = this.recuperar(idPatogeno)
        val especie = patogeno.agregarEspecie(nombreEspecie, paisDeOrigen, adn)
        val session = TransactionRunner.currentSession
        session.save(especie)
        val hql = """ select especie from patogeno p join p.especies especie  where especie.nombre = :unNombre"""
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("unNombre",nombreEspecie)

        return query.singleResult
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

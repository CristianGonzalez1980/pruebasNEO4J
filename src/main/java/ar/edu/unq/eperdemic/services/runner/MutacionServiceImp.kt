package ar.edu.unq.eperdemic.services.runner

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateEspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateMutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.services.MutacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class MutacionServiceImp(
        private val dataDAO: HibernateDataDAO,
        private val mutacionDAO: HibernateMutacionDAO,
        private val especieDAO: HibernateEspecieDAO,
        private val patogenoDAO: HibernatePatogenoDAO
) : MutacionService {


    override fun mutar(especieId: Int, mutacionId: Int) {
        runTrx {
            val mutacion: Mutacion = mutacionDAO.recuperarMut(mutacionId)
            val especie: Especie = especieDAO.recuperarEspecie(especieId)
            val patogeno: Patogeno = patogenoDAO.recuperar(especie.owner!!.id)
            especie.agregarMutacion(mutacion)
            especieDAO.actualizar(especie)
            patogenoDAO.actualizar(patogeno)
            //entiendo que la especie se persiste y la mutacion tambien!!!
        }
    }

    override fun crearMutacion(mutacion: Mutacion): Mutacion {

        return runTrx { mutacionDAO.crear(mutacion) }
    }

    override fun recuperarMutacion(mutacionId: Int): Mutacion {

        return runTrx { mutacionDAO.recuperarMut(mutacionId) }
    }
}
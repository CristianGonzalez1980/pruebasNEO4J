package ar.edu.unq.eperdemic.utils

import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.modelo.Potencialidad
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateMutacionDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MutacionDaoTest {

    private val dao: MutacionDAO = HibernateMutacionDAO()
    private val datadao: DataDAO = HibernateDataDAO()
    lateinit var mutacionCreada: Mutacion

    @Before
    fun crearModelo() {
        val mutacion1 = Mutacion(45, mutableListOf(), mutableListOf(), Potencialidad.Letalidad)
        mutacionCreada = runTrx { dao.crear(mutacion1) }
    }

    @Test
    fun creoUnaMutacionVerificoSuPersistencia() {
        val mutacion2 = Mutacion(15, mutableListOf(), mutableListOf(), Potencialidad.Contagio)
        val mutacionPersistida = runTrx { dao.crear(mutacion2) }
        Assert.assertEquals(mutacion2.puntosAdnNecesarios, mutacionPersistida.puntosAdnNecesarios)
        Assert.assertEquals(mutacion2.potencialidad!!.name, mutacionPersistida.potencialidad!!.name)
        Assert.assertEquals(mutacion2.mutacionesNecesarias.size, mutacionPersistida.mutacionesNecesarias.size)
    }

    @Test
    fun VerificoRecuperoDeUnaMutacion() {
        val mutacionRecuperada = runTrx { dao.recuperarMut(mutacionCreada.id!!.toInt()) }
        Assert.assertEquals(mutacionCreada.puntosAdnNecesarios, mutacionRecuperada.puntosAdnNecesarios)
        Assert.assertEquals(mutacionCreada.potencialidad!!.name, mutacionRecuperada.potencialidad!!.name)
        Assert.assertEquals(mutacionCreada.mutacionesNecesarias.size, mutacionRecuperada.mutacionesNecesarias.size)
        Assert.assertEquals(mutacionCreada.id, mutacionRecuperada.id)
    }

    @After
    fun cleanup() {
        runTrx { datadao.clear() }
        //Destroy cierra la session factory y fuerza a que, la proxima vez, una nueva tenga
        //que ser creada.
    }
}
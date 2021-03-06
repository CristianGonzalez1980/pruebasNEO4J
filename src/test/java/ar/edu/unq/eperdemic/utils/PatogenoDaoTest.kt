package ar.edu.unq.eperdemic.utils

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType
import org.hibernate.exception.ConstraintViolationException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PatogenoDaoTest {

    private val dao: PatogenoDAO = HibernatePatogenoDAO()
    private val datadao: DataDAO = HibernateDataDAO()
    lateinit var service: PatogenoService


    /*private val modelo: DataService = DataServiceJDBC()
    lateinit var patogenoRaro: Patogeno*/

    @Before
    fun crearModelo() {
        this.service = PatogenoServiceImp(
                HibernatePatogenoDAO(),
                HibernateDataDAO()
        )
        service.crearPatogeno(Patogeno("Bacteria", 30, 50, 50))
        service.crearPatogeno(Patogeno("Parsero", 52, 8, 25))
        service.crearPatogeno(Patogeno("Volado", 7, 6, 3))
    }

    @Test
    fun crearPatogenoYCorroborarId() {
        val patogenoRaro = Patogeno("Priones", 30, 20, 15)
        Assert.assertEquals(4, runTrx ({ dao.crear(patogenoRaro) }, listOf(TransactionType.HIBERNATE)))
    }

    @Test(expected = ConstraintViolationException::class)
    fun crearPatogenoConTipoYaExistenteSinExito() {
        val patogenoRaro = Patogeno("Bacteria", 50, 84, 98)
        val idPatogeno: Int = runTrx ({ dao.crear(patogenoRaro) }, listOf(TransactionType.HIBERNATE))
        Assert.assertEquals(2, idPatogeno)
    }

    @Test(expected = NullPointerException::class)
    fun intentoDeRecuperarPatogenoNuncaCreadoRespondeQueEsNulo() {
        val patogenoRaro2 = runTrx ({ dao.recuperar(4) }, listOf(TransactionType.HIBERNATE))
        Assert.assertEquals("Virus", patogenoRaro2.tipo)
    }

    @Test
    fun pruebaRecuperar() {
        val patogeno: Patogeno = runTrx ({ dao.recuperar(1) }, listOf(TransactionType.HIBERNATE))
        Assert.assertEquals("Bacteria", patogeno.tipo)
    }

    @Test
    fun agregaUnaEspecieYCorroboraActualizacionDelPatogeno() {
        val patogeno: Patogeno = runTrx ({ dao.recuperar(1) }, listOf(TransactionType.HIBERNATE))
        val especie = patogeno.agregarEspecie("Vaca Loca","Francia", 5)
        runTrx ({ dao.actualizar(patogeno) }, listOf(TransactionType.HIBERNATE))
        val patogenoRec = runTrx ({ dao.recuperar(1) }, listOf(TransactionType.HIBERNATE))
        Assert.assertEquals(1, patogenoRec.cantidadDeEspecies)
    }

    @Test
    fun recuperaTodosLosPatogenosYCorroboraCantidad() {
        val patogenos: List<Patogeno> = runTrx ({ dao.recuperarATodos() }, listOf(TransactionType.HIBERNATE))
        Assert.assertEquals(3, patogenos.size)
    }

    @After
    fun cleanup() {
        runTrx ({ datadao.clear() }, listOf(TransactionType.HIBERNATE))
    }
}
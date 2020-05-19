package ar.edu.unq.eperdemic.utils

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.runner.PatogenoServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import org.hibernate.exception.ConstraintViolationException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertThrows
import java.sql.SQLIntegrityConstraintViolationException
import javax.validation.constraints.AssertTrue

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
    fun crearUnPatogenoSeCorroboraNumeroDeId() {
        val patogenoRaro = Patogeno("Priones", 30, 20, 15)
        Assert.assertEquals(4, runTrx { dao.crear(patogenoRaro) })
    }

    @Test(expected = ConstraintViolationException::class)
    fun crearUnSegundoPatogenoConUnTipoYaExistenteYNoMeLoPermite() {
        val patogenoRaro = Patogeno("Bacteria", 50, 84, 98)
        val idPatogeno: Int = runTrx { dao.crear(patogenoRaro) }
        Assert.assertEquals(2, idPatogeno)
    }

    @Test(expected = NullPointerException::class)
    fun pruebaRecuperarPatogenoQueNoFueCreadoYMeRespondeQueEsNulo() {
        val patogenoRaro2 = runTrx { dao.recuperar(4) }
        Assert.assertEquals("Virus", patogenoRaro2.tipo)
    }

    @Test
    fun pruebaRecuperar() {
        val patogeno: Patogeno = runTrx { dao.recuperar(1) }
        Assert.assertEquals("Bacteria", patogeno.tipo)
    }

/*    @Test
    fun crearUnPatogenoCuandoLoActualizoMeRespondeQueNoPuedeSerActualizado() {
        val patogenoRaro = Patogeno("Hongo", 80, 80, 48)
        val idPatogeno = dao.crear(patogenoRaro)
        println(dao.actualizar(patogenoRaro))
        Assert.assertEquals(0, dao.recuperar(idPatogeno).cantidadDeEspecies)
    }*/

    @Test
    fun seAgregaUnaEspecieSeCorroboraLaActualizacionDelPatogeno() {
        val patogeno: Patogeno = runTrx { dao.recuperar(1) }
        val especie = Especie(patogeno,"Vaca Loca","Francia", 5)
        val patogenoRec = runTrx { dao.recuperar(1) }
        Assert.assertEquals(1, patogenoRec.cantidadDeEspecies)
    }

    @Test
    fun seRecuperanTodosLosPatogenosSeCorroboraCantidad() {
        val patogenos: List<Patogeno> = runTrx { dao.recuperarATodos() }
        Assert.assertEquals(3, patogenos.size)
    }

    @After
    fun emilinarModelo() {
        runTrx { datadao.clear() }
    }
}
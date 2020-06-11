package ar.edu.unq.eperdemic.utils

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionType
import ar.edu.unq.eperdemic.utils.Impl.DataServiceImp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VectorDaoTest {


    private val dao: VectorDAO = HibernateVectorDAO()
    private val datadao: DataDAO = HibernateDataDAO()
    private val dataService: DataService = DataServiceImp(HibernateDataDAO())
    private val serviceVect: VectorService = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
    private val serviceUbi: UbicacionService = UbicacionServiceImp(HibernateUbicacionDAO(),
            UbicacionNeo4jDao(), HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()))
    lateinit var ubicacionA: Ubicacion
    lateinit var ubicacionB: Ubicacion
    lateinit var vectorA: Vector
    lateinit var vectorB: Vector


    @Before
    fun crearModelo() {
        dataService.eliminarTodo()
        //se instancian 2 ciudad
        ubicacionA = Ubicacion("La Plata")
        ubicacionB = Ubicacion("Ranelagh")
        //se persiste La plata y Ranelagh
        serviceUbi.crearUbicacion(ubicacionA.nombreDeLaUbicacion!!)
        serviceUbi.crearUbicacion(ubicacionB.nombreDeLaUbicacion!!)
        //se recupera La Plata
        ubicacionA = serviceUbi.recuperar("La Plata")
        //se instancian 2 vectores en LaPlata y de tipo persona
        vectorA = Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona)
        vectorB = Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona)
        //se persisten 1 vectores
        vectorB = serviceVect.crearVector(vectorB)
        //se persiste LaPlata
        serviceUbi.actualizar(ubicacionA)
    }

    @Test
    fun creaVectorLuegoRecuperaYVerificaId() {
        val vecGuardado: Vector = serviceVect.crearVector(vectorA)//
        val vecRecuperado: Vector = serviceVect.recuperarVector(vecGuardado.id!!.toInt())
        Assert.assertEquals(vecGuardado.id, vecRecuperado.id)
    }

    @Test
    fun sePruebaActualizarYRecuperarVector() {
        serviceUbi.mover(vectorB.id!!.toInt(), "Ranelagh")
        val vecRecuperado: Vector = serviceVect.recuperarVector(vectorB.id!!.toInt())
        Assert.assertEquals("Ranelagh", vecRecuperado.location!!.nombreDeLaUbicacion)
    }

    @Test
    fun eliminacionDeVector() {
        Assert.assertEquals(2, serviceUbi.recuperar("La Plata").vectores.size)
        serviceVect.borrarVector(vectorB.id!!.toInt())
        Assert.assertEquals(1, serviceUbi.recuperar("La Plata").vectores.size)
    }

    @After
    fun cleanup() {
        runTrx ({ datadao.clear() }, listOf(TransactionType.HIBERNATE))
    }
}
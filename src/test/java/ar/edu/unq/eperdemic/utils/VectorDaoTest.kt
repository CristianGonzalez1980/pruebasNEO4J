package ar.edu.unq.eperdemic.utils

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.runner.VectorServiceImp
import ar.edu.unq.eperdemic.utils.Impl.DataServiceImp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VectorDaoTest {


    private val dao: VectorDAO = HibernateVectorDAO()
    private val especieDAO: EspecieDAO = HibernateEspecieDAO()
    private val dataService: DataService = DataServiceImp(HibernateDataDAO())
    private val serviceVect: VectorService = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()/*, HibernateEspecieDAO()*/)
    private val serviceUbi: UbicacionService = UbicacionServiceImp(HibernateUbicacionDAO(),
            HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()/*HibernateEspecieDAO()*/))
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
    fun sePruebaCrearVector() {
        var vecGuardado: Vector = serviceVect.crearVector(vectorA)//
        var vecRecuperado: Vector = serviceVect.recuperarVector(vecGuardado.id!!.toInt())
        Assert.assertEquals(vecGuardado.id, vecRecuperado.id)
    }

    @Test
    fun sePruebaActualizaryRecuperarVector() {
        serviceUbi.mover(vectorB.id!!.toInt(), "Ranelagh")
        var vecRecuperado: Vector = serviceVect.recuperarVector(vectorB.id!!.toInt())
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
        dataService.eliminarTodo()
        //Destroy cierra la session factory y fuerza a que, la proxima vez, una nueva tenga
        //que ser creada.
    }
}
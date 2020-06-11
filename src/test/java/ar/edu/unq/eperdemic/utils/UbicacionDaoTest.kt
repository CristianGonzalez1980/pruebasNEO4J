package ar.edu.unq.eperdemic.utils

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionType
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionDaoTest {

    private val dao: UbicacionDAO = HibernateUbicacionDAO()
    private val datadao: DataDAO = HibernateDataDAO()
    private val serviceVect: VectorService = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
    private val serviceUbi: UbicacionService = UbicacionServiceImp(HibernateUbicacionDAO(),
            UbicacionNeo4jDao(), HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()))
    lateinit var ubicacionA: Ubicacion
    lateinit var ubicacionB: Ubicacion
    lateinit var ubicacionC: Ubicacion
    lateinit var ubicacionD: Ubicacion
    lateinit var vectorA: Vector
    lateinit var vectorB: Vector
    lateinit var vectorC: Vector
    lateinit var vectorD: Vector
    lateinit var vectorE: Vector

    @Before
    fun crearModelo() {
        //se instancian 4 ciudades
        ubicacionA = Ubicacion("La Plata")
        ubicacionB = Ubicacion("City Bell")
        ubicacionC = Ubicacion("Tolosa")
        ubicacionD = Ubicacion("Ringuelet")
        //se persiste La plata
        serviceUbi.crearUbicacion(ubicacionA.nombreDeLaUbicacion!!)
        //se persiste City Bell
        serviceUbi.crearUbicacion(ubicacionB.nombreDeLaUbicacion!!)
        //se recupera City Bell
        ubicacionB = serviceUbi.recuperar("City Bell")
        //se instancian 5 vectores en City Bell y de tipo persona
        vectorA = Vector(ubicacionB, VectorFrontendDTO.TipoDeVector.Persona)
        vectorB = Vector(ubicacionB, VectorFrontendDTO.TipoDeVector.Persona)
        vectorC = Vector(ubicacionB, VectorFrontendDTO.TipoDeVector.Persona)
        vectorD = Vector(ubicacionB, VectorFrontendDTO.TipoDeVector.Persona)
        vectorE = Vector(ubicacionB, VectorFrontendDTO.TipoDeVector.Persona)
        //se persisten 5 vectores
        serviceVect.crearVector(vectorA)
        serviceVect.crearVector(vectorB)
        serviceVect.crearVector(vectorC)
        serviceVect.crearVector(vectorD)
        serviceVect.crearVector(vectorE)
        //se persiste City Bell
        serviceUbi.actualizar(ubicacionB)
        //se recupera La Plata
        var ubicacionA: Ubicacion = serviceUbi.recuperar("La Plata")
    }

    @Test
    fun chequeoDeGuardarUbicaciones() {
        serviceUbi.crearUbicacion(ubicacionC.nombreDeLaUbicacion!!)//"Tolosa"
        val ubiGuardada: Ubicacion = serviceUbi.recuperar(ubicacionC.nombreDeLaUbicacion!!)
        Assert.assertEquals(ubicacionC.nombreDeLaUbicacion, ubiGuardada.nombreDeLaUbicacion)
    }

    @Test
    fun seAsientanVectoresEnLaUbicacion() {
        val ubicacionAnterior = vectorB.location
        serviceUbi.mover(vectorB.id!!.toInt(), ubicacionA.nombreDeLaUbicacion!!.toString())
        serviceUbi.mover(vectorC.id!!.toInt(), ubicacionA.nombreDeLaUbicacion!!.toString())
        val ubiPersistida: Ubicacion = serviceUbi.recuperar(ubicacionA.nombreDeLaUbicacion!!)
        val ubiAnterior: Ubicacion = serviceUbi.recuperar(ubicacionAnterior?.nombreDeLaUbicacion!!)
        Assert.assertEquals(2, ubiPersistida.vectores.size)//En City Bell
        Assert.assertEquals(3, ubiAnterior.vectores.size)//En La plata
    }

    @Test
    fun moverDeUbicacionVectoresLuegoRecuperarUbiacionYVerificarCambios() {
        serviceUbi.crearUbicacion(ubicacionD.nombreDeLaUbicacion!!)//se persiste Ringuelet
        var ringuelet: Ubicacion = serviceUbi.recuperar(ubicacionD.nombreDeLaUbicacion!!)//se recupera Ringuelet
        var ubiAnterior: Ubicacion? = vectorC.location
        serviceUbi.mover(vectorC.id!!.toInt(), ringuelet.nombreDeLaUbicacion!!.toString())//De City Bell a Ringuelet
        serviceUbi.mover(vectorD.id!!.toInt(), ringuelet.nombreDeLaUbicacion!!.toString())//De City Bell a Ringuelet
        ringuelet = serviceUbi.recuperar(ringuelet.nombreDeLaUbicacion!!)
        ubiAnterior = serviceUbi.recuperar(ubiAnterior!!.nombreDeLaUbicacion!!)
        Assert.assertEquals(2, ringuelet.vectores.size)//En Ringuelet
        Assert.assertEquals(3, ubiAnterior.vectores.size)//En City Bel
    }

    @After
    fun cleanup() {
        runTrx ({ datadao.clear() }, listOf(TransactionType.HIBERNATE))
    }
}

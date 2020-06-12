package ar.edu.unq.eperdemic.testNeo4jUbicacion


import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionNeo4jTest {

    lateinit var dao: UbicacionNeo4jDao
    lateinit var vectorHibernateService: VectorServiceImp
    lateinit var ubicacionA: Ubicacion
    lateinit var ubicacionB: Ubicacion
    lateinit var ubicacionC: Ubicacion
    lateinit var vectorA: Vector
    lateinit var vectorB: Vector

    @Before
    fun setUp() {
        dao = UbicacionNeo4jDao()
        vectorHibernateService = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
        ubicacionA = Ubicacion("Quilmes")
        ubicacionB = Ubicacion("Colonia")
        ubicacionC = Ubicacion("Maldonado")
        runTrx({ dao.crearUbicacion(ubicacionA) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.crearUbicacion(ubicacionB) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.crearUbicacion(ubicacionC) }, listOf(TransactionType.NEO4J))
    }

    @Test
    fun creoUnaUbicacionYverificoQueSeCreoElGrafo() {
        val ubicacion = Ubicacion("La Plata")
        runTrx({
            dao.crearUbicacion(ubicacion)
        }, listOf(TransactionType.NEO4J))
        Assert.assertTrue(runTrx({ dao.existeUbicacion(ubicacion) }, listOf(TransactionType.NEO4J)))
    }

    @Test
    fun conectoYVerificoUbicacionesConectadas() {
        runTrx({ dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name) }, listOf(TransactionType.NEO4J))
        val conectados = runTrx({ dao.conectados(ubicacionA.nombreDeLaUbicacion!!) }, listOf(TransactionType.NEO4J))
        val nombresConectados = conectados.map { it.nombreDeLaUbicacion }
        Assert.assertEquals(2, conectados.size)
        Assert.assertTrue(nombresConectados.contains("Maldonado"))
        Assert.assertTrue(nombresConectados.contains("Colonia"))
    }

    @Test
    fun conectoUbicacionesYVerificoPorTipoCamino() {
        runTrx({ dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name) }, listOf(TransactionType.NEO4J))
        Assert.assertTrue(runTrx({ dao.estanConectadasPorCamino(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name) }, listOf(TransactionType.NEO4J)))
        Assert.assertFalse(runTrx({ dao.estanConectadasPorCamino(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Aereo.name) }, listOf(TransactionType.NEO4J)))
        Assert.assertTrue(runTrx({ dao.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name) }, listOf(TransactionType.NEO4J)))
        Assert.assertFalse(runTrx({ dao.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name) }, listOf(TransactionType.NEO4J)))
    }

    @Test
    fun conectoUbicacionesYVerificoElTipoCamino() {
        runTrx({
            dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        }, listOf(TransactionType.NEO4J))
        runTrx({
            dao.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        }, listOf(TransactionType.NEO4J))
        Assert.assertEquals("Maritimo", runTrx({
            dao.tipoCaminoEntre(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!)
        }, listOf(TransactionType.NEO4J)))
        Assert.assertEquals("Terrestre", runTrx({
            dao.tipoCaminoEntre(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!)
        }, listOf(TransactionType.NEO4J)))
    }

    @Test
    fun muevoUnVectorHaciaUnaUbicacion() {
        vectorA = vectorHibernateService.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona))
        vectorB = vectorHibernateService.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Insecto))
        val ubicacion = Ubicacion("La Plata")
        runTrx({
            dao.crearUbicacion(ubicacion)
        }, listOf(TransactionType.NEO4J))
        runTrx({
            dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        }, listOf(TransactionType.NEO4J))
        runTrx({ dao.conectar(ubicacionB.nombreDeLaUbicacion!!, "La Plata", TipoDeCamino.Maritimo.name) }, listOf(TransactionType.NEO4J))
        runTrx({
            dao.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        }, listOf(TransactionType.NEO4J))
        runTrx({
            dao.relacionarUbicacion(vectorB, ubicacionA)
        }, listOf(TransactionType.NEO4J))
        runTrx({
            dao.moverMasCorto(vectorB.id!!, "La Plata")//Falta hacerlo funcionar y utilizar las excepciones en cada caso
        }, listOf(TransactionType.NEO4J))
        /*ubicActualRecuperadaDeVectorB = dao.ubicacionDeVector(vectorB.id!!.toInt()) //Despues de moverse
        Assert.assertNotEquals(ubicacionA.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertNotEquals(ubicacionB.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertEquals(ubicacionC.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)*/
    }

    @After
    fun limpiar() {
        runTrx({dao.clear()}, listOf(TransactionType.NEO4J))
    }
}


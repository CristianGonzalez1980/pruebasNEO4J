package ar.edu.unq.eperdemic.testNeo4jUbicacion


import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.services.runner.TransactionRunner
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionNeo4jTest {

    lateinit var dao: UbicacionNeo4jDao
    lateinit var ubicacionA: Ubicacion
    lateinit var ubicacionB: Ubicacion
    lateinit var ubicacionC: Ubicacion
    lateinit var vectorA: Vector
    lateinit var vectorB: Vector

    @Before
    fun setUp() {
        dao = UbicacionNeo4jDao()
        ubicacionA = Ubicacion("Quilmes")
        ubicacionB = Ubicacion("Colonia")
        ubicacionC = Ubicacion("Maldonado")
        vectorA = runTrx({ dao.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona)) }, listOf(TransactionType.NEO4J))
        vectorB = runTrx({ dao.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Insecto)) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.crearUbicacion(ubicacionA) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.crearUbicacion(ubicacionB) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.crearUbicacion(ubicacionC) }, listOf(TransactionType.NEO4J))
    }

    @Test
    fun creoUnaUbicacionYverificoQueSeCreoElGrafo() {
        val ubicacion = Ubicacion("La Plata")
        dao.crearUbicacion(ubicacion)
        Assert.assertTrue(runTrx({ dao.existeUbicacion(ubicacion) }, listOf(TransactionType.NEO4J)))
    }

    @Test
    fun conectoYVerificoUbicacionesConectadas() {
        runTrx({ dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name) }, listOf(TransactionType.NEO4J))
        runTrx({ dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name) }, listOf(TransactionType.NEO4J))
        val conectados = runTrx({ dao.conectados(ubicacionA.nombreDeLaUbicacion!!) }, listOf(TransactionType.NEO4J))
        val nombresConectados2 = conectados.map { println(it.nombreDeLaUbicacion) }
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
   //     Assert.assertFalse(runTrx({ dao.estanConectadasPorCamino(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Aereo.name) }, listOf(TransactionType.NEO4J)))
    //    Assert.assertTrue(runTrx({ dao.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name) }, listOf(TransactionType.NEO4J)))
    //    Assert.assertFalse(runTrx({ dao.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name) }, listOf(TransactionType.NEO4J)))
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
    fun creoVectorYVerificoLaCreacionDeGrafo() {
        val idInventadoInexistente = 2
        Assert.assertTrue(runTrx({
            dao.existeVector(vectorA.id!!.toInt())
        }, listOf(TransactionType.NEO4J)))
        Assert.assertFalse(runTrx({
            dao.existeVector(idInventadoInexistente)
        }, listOf(TransactionType.NEO4J)))
    }

    @Test
    fun creoVectorRecuperoYVerificoPorId() {
        runTrx({
            dao.relacionarUbicacion(vectorA, ubicacionA)
        }, listOf(TransactionType.NEO4J))
        val vectorARecuperado = runTrx({
            dao.recuperarVector(vectorA.id!!.toInt())
        }, listOf(TransactionType.NEO4J))
        Assert.assertEquals(vectorA.id!!.toInt(), vectorARecuperado.id!!.toInt())
    }

    @Test
    fun creoYVerificoRelacionEntreVectorYUbicacion() {
        runTrx({
            dao.relacionarUbicacion(vectorB, ubicacionA)
        }, listOf(TransactionType.NEO4J))
        Assert.assertEquals(ubicacionA.nombreDeLaUbicacion, runTrx({
            dao.ubicacionDeVector(vectorB.id!!.toInt()).nombreDeLaUbicacion
        }, listOf(TransactionType.NEO4J)))
        Assert.assertNotEquals(ubicacionB.nombreDeLaUbicacion, runTrx({
            dao.ubicacionDeVector(vectorB.id!!.toInt()).nombreDeLaUbicacion
        }, listOf(TransactionType.NEO4J)))
        Assert.assertNotEquals(ubicacionC.nombreDeLaUbicacion, runTrx({
            dao.ubicacionDeVector(vectorB.id!!.toInt()).nombreDeLaUbicacion
        }, listOf(TransactionType.NEO4J)))
    }

    @Test
    fun muevoUnVectorHaciaUnaUbicacion() {
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
            dao.moverMasCorto(vectorB.id!!, "La Plata")
        }, listOf(TransactionType.NEO4J)) //Falta hacerlo funcionar y utilizar las excepciones en cada caso
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


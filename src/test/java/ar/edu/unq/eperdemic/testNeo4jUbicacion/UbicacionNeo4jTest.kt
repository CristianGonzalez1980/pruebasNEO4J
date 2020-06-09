package ar.edu.unq.eperdemic.testNeo4jUbicacion


import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionNeo4jTest {

    lateinit var dao: UbicacionNeo4jDao
    lateinit var ubicacionA: Ubicacion
    lateinit var  ubicacionB: Ubicacion
    lateinit var  ubicacionC: Ubicacion
    lateinit var vectorA: Vector
    lateinit var vectorB: Vector

    @Before
    fun setUp() {
        dao = UbicacionNeo4jDao()
        ubicacionA = Ubicacion("Quilmes")
        ubicacionB = Ubicacion("Colonia")
        ubicacionC = Ubicacion("Maldonado")
        vectorA = dao.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona))
        vectorB = dao.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Insecto))
        dao.crearUbicacion(ubicacionA)
        dao.crearUbicacion(ubicacionB)
        dao.crearUbicacion(ubicacionC)
    }

    @Test
    fun creoUnaUbicacionYverificoQueSeCreoElGrafo() {
        val ubicacion = Ubicacion("La Plata")
        dao.crearUbicacion(ubicacion)
        Assert.assertTrue(dao.existeUbicacion(ubicacion))
    }

    @Test
    fun conectoYVerificoUbicacionesConectadas() {
        dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        val conectados = dao.conectados(ubicacionA.nombreDeLaUbicacion!!)
        val nombresConectados = conectados.map { it.nombreDeLaUbicacion }
        Assert.assertEquals(2, conectados.size)
        Assert.assertTrue(nombresConectados.contains("Maldonado"))
        Assert.assertTrue(nombresConectados.contains("Colonia"))
    }

    @Test
    fun conecto2UbicacionesPorCaminoAereoVerificoLaRelacion() {
        dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        dao.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        Assert.assertTrue(dao.estanConectadasPorCamino(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name))
        Assert.assertFalse(dao.estanConectadasPorCamino(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Aereo.name))
        Assert.assertTrue(dao.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name))
        Assert.assertFalse(dao.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name))
    }

    @Test
    fun creoVectorYVerificoLaCreacionDeGrafo() {
        val idInventadoInexistente = 2
        Assert.assertTrue(dao.existeVector(vectorA.id!!.toInt()))
        Assert.assertFalse(dao.existeVector(idInventadoInexistente))
    }

    @Test
    fun creoYVerificoRelacionEntreVectorYUbicacion() {
        dao.relacionarUbicacion(vectorB, ubicacionA)
        Assert.assertTrue(dao.ubicacionesDeVector(vectorB.id!!.toInt()).contains(ubicacionA))
        Assert.assertFalse(dao.ubicacionesDeVector(vectorB.id!!.toInt()).contains(ubicacionB))
        Assert.assertFalse(dao.ubicacionesDeVector(vectorB.id!!.toInt()).contains(ubicacionC))
    }

    @Test
    fun muevoUnVectorHaciaUnaUbicacion() {
        dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        dao.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!,TipoDeCamino.Maritimo.name)
        dao.relacionarUbicacion(vectorB, ubicacionA)
        var ubicActualRecuperadaDeVectorB = dao.ubicacionDeVector(vectorB.id!!.toInt()) //Antes de moverse
        Assert.assertEquals(ubicacionA.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertNotEquals(ubicacionB.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertNotEquals(ubicacionC.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        /*dao.moverMasCorto(vectorB.id!!.toInt(), ubicacionC.nombreDeLaUbicacion!!) //Falta hacerlo funcionar y utilizar las excepciones en cada caso
        ubicActualRecuperadaDeVectorB = dao.ubicacionDeVector(vectorB.id!!.toInt()) //Despues de moverse
        Assert.assertNotEquals(ubicacionA.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertNotEquals(ubicacionB.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertEquals(ubicacionC.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)*/
    }

    @After
    fun limpiar(){
        dao.clear()
    }
}


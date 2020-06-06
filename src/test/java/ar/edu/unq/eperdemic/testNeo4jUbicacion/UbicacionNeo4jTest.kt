package ar.edu.unq.eperdemic.testNeo4jUbicacion


import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion;
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test

class UbicacionNeo4jTest {

    lateinit var dao: UbicacionNeo4jDao
    val ubicacionA = Ubicacion("Quilmes")
    val ubicacionB = Ubicacion("Colonia")
    val ubicacionC = Ubicacion("Maldonado")

    @Before
    fun setUp() {
        dao = UbicacionNeo4jDao()
        dao.clear()
        dao.crearUbicacion(ubicacionA)
        dao.crearUbicacion(ubicacionB)
        dao.crearUbicacion(ubicacionC)
    }

    @Test
    fun creoUnaUbicacionYverificoQueSeCreoElGrafo() {

        var ubicacion = Ubicacion("La Plata")
        dao.crearUbicacion(ubicacion)

        Assert.assertEquals(true, dao.existeUbicacion(ubicacion))
    }

/*    @Test
    fun conecto2UbicacionesPorCaminoAereoVerificoLaRelacion() {

        dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)

           Assert.assertEquals(true,dao.estaConectadaPorCamino(ubicacionA, ubicacionC, TipoDeCamino.Terrestre)) DA SIEMPRE VERDE CORREGIR
    }*/

    @Test
    fun verificoUbicacionesConectadas() {

        dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        dao.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)

        val conectados = dao.conectados(ubicacionA.nombreDeLaUbicacion!!)
        val nombresConectados = conectados.map { it.nombreDeLaUbicacion }

        Assert.assertEquals(2, conectados.size)
        Assert.assertTrue(nombresConectados.contains("Maldonado"))
        Assert.assertTrue(nombresConectados.contains("Colonia"))
    }

}

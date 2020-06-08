package ar.edu.unq.eperdemic.testNeo4jUbicacion


import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion;
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test

class UbicacionNeo4jTest {

    lateinit var dao: UbicacionNeo4jDao
    val ubicacionA = Ubicacion("Quilmes")
    val ubicacionB = Ubicacion("Colonia")
    val ubicacionC = Ubicacion("Maldonado")
    var vectorA = Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona)
    var vectorB = Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Insecto)


    @Before
    fun setUp() {
        dao = UbicacionNeo4jDao()
       // dao.clear()
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

    @Test
    fun creoUnVectorYVerificoQueSeCreoElGrafoVector() {

        dao.crearVector(vectorA)
        Assert.assertEquals(true, dao.existeVector(vectorA))

    }

    @Test
    fun creoUnaRelacionEntreUnVectorYUnaUbicacion() {

        dao.crearVector(vectorB)
        dao.relacionarUbicacion(vectorB, ubicacionA)
        Assert.assertEquals(true, dao.ubicacionesDeVector(vectorB.tipo!!.name).contains(ubicacionA))

    }

    @Test
    fun muevoUnVectorHaciaUnaUbicacion() {

        dao.conectar(ubicacionC.nombreDeLaUbicacion!!, "La Plata", TipoDeCamino.Maritimo.name)

        dao.relacionarUbicacion(vectorB, ubicacionB)

        //dao.moverMasCorto(vectorB.tipo!!.name, ubicacionB.nombreDeLaUbicacion!!)


    }
}


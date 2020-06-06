package ar.edu.unq.eperdemic.testNeo4jUbicacion


import ar.edu.unq.eperdemic.modelo.Ubicacion;
import ar.edu.unq.eperdemic.neo4jDao.UbicacionNeo4jDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test

class UbicacionNeo4jTest {

    lateinit var dao: UbicacionNeo4jDao

    @Before
    fun setUp() {
        dao = UbicacionNeo4jDao()
    }

    @Test
    fun creoUnaUbicacionYverificoQueSeCreoElGrafo(){

        var ubicacion = Ubicacion("La Plata")
        dao.crearUbicacion(ubicacion)

        Assert.assertEquals(true,dao.existeUbicacion(ubicacion))

    }
}

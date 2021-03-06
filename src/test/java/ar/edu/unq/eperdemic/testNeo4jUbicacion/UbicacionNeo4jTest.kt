package ar.edu.unq.eperdemic.testNeo4jUbicacion


import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Excepciones.UbicacionMuyLejana
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import ar.edu.unq.eperdemic.services.runner.TransactionType
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.Impl.DataServiceImp
import org.hibernate.exception.ConstraintViolationException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UbicacionNeo4jTest {

    lateinit var serviceUbi: UbicacionServiceImp
    lateinit var dao: UbicacionNeo4jDao
    lateinit var serviceData: DataServiceImp
    lateinit var vectorHibernateService: VectorServiceImp
    lateinit var ubicacionA: Ubicacion
    lateinit var ubicacionB: Ubicacion
    lateinit var ubicacionC: Ubicacion
    lateinit var ubicacionD: Ubicacion
    lateinit var ubicacionE: Ubicacion
    lateinit var ubicacionF: Ubicacion
    lateinit var ubicacionG: Ubicacion
    lateinit var ubicacionH: Ubicacion
    lateinit var ubicacionI: Ubicacion

    @Before
    fun setUp() {
        serviceUbi = UbicacionServiceImp(HibernateUbicacionDAO(),
                UbicacionNeo4jDao(), HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()))
        dao = UbicacionNeo4jDao()
        serviceData = DataServiceImp(HibernateDataDAO(), UbicacionNeo4jDao())
        vectorHibernateService = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
        // serviceData.eliminarTodo()
        ubicacionA = serviceUbi.crearUbicacion("Quilmes")
        ubicacionB = serviceUbi.crearUbicacion("Colonia")
        ubicacionC = serviceUbi.crearUbicacion("Maldonado")
        ubicacionD = serviceUbi.crearUbicacion("Posadas")
        ubicacionE = serviceUbi.crearUbicacion("Encarnacion")
        ubicacionF = serviceUbi.crearUbicacion("Ituzaingo")
        ubicacionG = serviceUbi.crearUbicacion("Paso de la Patria")
        ubicacionH = serviceUbi.crearUbicacion("Formosa")
        ubicacionI = serviceUbi.crearUbicacion("Asuncion")

    }

    @Test
    fun creoUnaUbicacionYverificoQueSeCreoElGrafo() {
        val ubicacionCreada = serviceUbi.crearUbicacion("La Plata")
        Assert.assertTrue(serviceUbi.existeUbicacion(ubicacionCreada))
    }

    @Test
    fun conectoYVerificoUbicacionesConectadas() {
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        val ubicConectadas = serviceUbi.conectados(ubicacionA.nombreDeLaUbicacion!!)
        val nombresConectados = ubicConectadas.map { it.nombreDeLaUbicacion }
        Assert.assertEquals(2, ubicConectadas.size)
        Assert.assertTrue(nombresConectados.contains("Maldonado"))
        Assert.assertTrue(nombresConectados.contains("Colonia"))
    }

    @Test
    fun conectoUbicacionesYVerificoPorTipoCamino() {
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        Assert.assertTrue(serviceUbi.estanConectadasPorCamino(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name))
        Assert.assertFalse(serviceUbi.estanConectadasPorCamino(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Aereo.name))
        Assert.assertTrue(serviceUbi.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name))
        Assert.assertFalse(serviceUbi.estanConectadasPorCamino(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name))
    }

    @Test
    fun conectoUbicacionesYVerificoElTipoCamino() {
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        Assert.assertEquals("Maritimo", serviceUbi.tipoCaminoEntre(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!))
        Assert.assertEquals("Terrestre", serviceUbi.tipoCaminoEntre(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!))
    }

    @Test
    fun VerificoPathConVectorInsectoQueUtilizaMoverMasCorto() {
        serviceUbi.conectar("Posadas", "Encarnacion", TipoDeCamino.Maritimo.name)
        serviceUbi.conectar("Encarnacion", "Asuncion", TipoDeCamino.Terrestre.name)
        serviceUbi.conectar("Asuncion", "Encarnacion", TipoDeCamino.Terrestre.name)
        serviceUbi.conectar("Formosa", "Asuncion", TipoDeCamino.Aereo.name)
        serviceUbi.conectar("Posadas", "Asuncion", TipoDeCamino.Aereo.name)
        serviceUbi.conectar("Posadas", "Formosa", TipoDeCamino.Aereo.name)
        serviceUbi.conectar("Ituzaingo", "Posadas", TipoDeCamino.Terrestre.name)
        serviceUbi.conectar("Paso de la Patria", "Ituzaingo", TipoDeCamino.Maritimo.name)
        val vectorA = vectorHibernateService.crearVector(Vector(ubicacionG, VectorFrontendDTO.TipoDeVector.Persona))
        val vectorB = vectorHibernateService.crearVector(Vector(ubicacionD, VectorFrontendDTO.TipoDeVector.Insecto))
        //     serviceUbi.moverMasCorto(vectorA.id!!, "Asuncion")
        serviceUbi.moverMasCorto(vectorB.id!!, "Asuncion")
        //    Assert.assertEquals("Asuncion", vectorA.location!!.nombreDeLaUbicacion)
        Assert.assertEquals("Asuncion", vectorB.location!!.nombreDeLaUbicacion)
    }

    @Test(expected = UbicacionMuyLejana::class)
    fun prueboMoverVectorYLanzaExcepcionUbicacionMuyLejana() {
        val vectorA = vectorHibernateService.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona))
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        serviceUbi.mover(vectorA.id!!.toInt(), ubicacionC.nombreDeLaUbicacion!!)
    }

    @Test
    fun muevoUnVectorHaciaUnaUbicacion() {
        val vectorA = vectorHibernateService.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona))
        serviceUbi.crearUbicacion("La Plata")
        serviceUbi.crearUbicacion("Bernal")
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        serviceUbi.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionC.nombreDeLaUbicacion!!, "La Plata", TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, "Bernal", TipoDeCamino.Terrestre.name)
        serviceUbi.conectar("Bernal", "La Plata", TipoDeCamino.Terrestre.name)
        serviceUbi.moverMasCorto(vectorA.id!!, "Bernal")
        val ubicActualRecuperadaDeVectorA = serviceUbi.ubicacionDeVector(vectorA.id!!) //Despues de moverse
        Assert.assertNotEquals(ubicacionA.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorA.nombreDeLaUbicacion)
        Assert.assertNotEquals(ubicacionB.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorA.nombreDeLaUbicacion)
        Assert.assertEquals(ubicacionC.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorA.nombreDeLaUbicacion)
    }

    /*@Test
    fun muevoUnVectorHaciaUnaUbicacionIncalcanzable() { CAMBIAR PARA QUE DE INALCANZABLE
        val vectorA = vectorHibernateService.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona))
        serviceUbi.crearUbicacion("La Plata")
        serviceUbi.crearUbicacion("Bernal")
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        serviceUbi.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionC.nombreDeLaUbicacion!!, "La Plata", TipoDeCamino.Maritimo.name)
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, "Bernal", TipoDeCamino.Terrestre.name)
        serviceUbi.conectar("Bernal", "La Plata", TipoDeCamino.Terrestre.name)
        serviceUbi.moverMasCorto(vectorA.id!!, "La Plata")
        val ubicActualRecuperadaDeVectorB = serviceUbi.ubicacionDeVector(vectorA) //Despues de moverse
        Assert.assertNotEquals(ubicacionA.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertNotEquals(ubicacionB.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
        Assert.assertEquals(ubicacionC.nombreDeLaUbicacion, ubicActualRecuperadaDeVectorB.nombreDeLaUbicacion)
    }*/

    @Test
    fun dadoUnVectorSeExpandeYSiLePreguntoSuCapacidadDeExpansionConDosMovimientosMeDevuelveTresUbicaciones() {
        val vectorA = vectorHibernateService.crearVector(Vector(ubicacionA, VectorFrontendDTO.TipoDeVector.Persona))
        serviceUbi.crearUbicacion("La Plata")
        serviceUbi.conectar(ubicacionA.nombreDeLaUbicacion!!, ubicacionB.nombreDeLaUbicacion!!, TipoDeCamino.Terrestre.name)
        serviceUbi.conectar(ubicacionB.nombreDeLaUbicacion!!, ubicacionC.nombreDeLaUbicacion!!, TipoDeCamino.Maritimo.name)

        Assert.assertEquals(3, dao.capacidadDeExpansion(vectorA, 2))
    }

    @After
    fun limpiar() {
        serviceData.eliminarTodo()
    }
}


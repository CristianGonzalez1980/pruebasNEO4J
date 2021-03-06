package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyHumano
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyAnimal
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImp
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.Impl.DataServiceImp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VectorServiceTest {

    lateinit var servicePatog: PatogenoServiceImp
    lateinit var serviceVect: VectorServiceImp
    lateinit var serviceUbic: UbicacionServiceImp
    lateinit var serviceData: DataService
    lateinit var vectorA: Vector
    lateinit var vectorB: Vector
    lateinit var vectorC: Vector
    lateinit var vectorD: Vector
    lateinit var vectorE: Vector
    lateinit var vectores: MutableList<Vector>
    lateinit var especie1: Especie
    lateinit var mosquito: Especie
    lateinit var covid19: Especie
    lateinit var gripeAviar: Especie
    lateinit var estrategia: StrategyHumano
    lateinit var estrategia1: StrategyAnimal
    lateinit var patogeno: Patogeno

    @Before
    fun crearModelo() {
        this.servicePatog = PatogenoServiceImp(HibernatePatogenoDAO(), HibernateDataDAO())
        this.serviceVect = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
        this.serviceUbic = UbicacionServiceImp(HibernateUbicacionDAO(), UbicacionNeo4jDao(), HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()))
        this.serviceData = DataServiceImp(HibernateDataDAO())
        estrategia = StrategyHumano()
        estrategia1 = StrategyAnimal()
        patogeno = Patogeno("Virus", 80, 80, 80)
        val id = servicePatog.crearPatogeno(patogeno)
        patogeno = servicePatog.recuperarPatogeno(id)
        especie1 = servicePatog.agregarEspecie(patogeno.id!!.toInt(),"Covid", "Argentina", 15)
        covid19 = servicePatog.agregarEspecie(patogeno.id!!.toInt(), "Coronavirus", "China", 55)
        gripeAviar = servicePatog.agregarEspecie(patogeno.id!!.toInt(), "H5N1", "EEUU", 40)
        mosquito = servicePatog.agregarEspecie(patogeno.id!!.toInt(), "Dengue", "Argentina", 15)
        val ubicacion1 = serviceUbic.crearUbicacion("Argentina")
        vectorA = serviceVect.crearVector(Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Persona))
        vectorB = serviceVect.crearVector(Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Persona))
        vectorC = serviceVect.crearVector(Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Animal))
        vectorD = serviceVect.crearVector(Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Animal))
        vectorE = serviceVect.crearVector(Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Animal))
        serviceVect.infectar(vectorA, mosquito)
        serviceVect.infectar(vectorC, especie1)
        serviceVect.infectar(vectorE, mosquito)
        serviceVect.infectar(vectorE, covid19)
        serviceVect.infectar(vectorE, gripeAviar)
        vectores = ArrayList()
    }

    @Test
    fun recuperarYVerificarTipo() {
        val tipoOriginal = vectorA.tipo
        val vectorRecuperado = serviceVect.recuperarVector(vectorA.id!!.toInt())
        val tipoRecuperado = vectorRecuperado.tipo
        Assert.assertEquals(tipoOriginal, tipoRecuperado)
    }

    @Test
    fun verificarContagioExitoso() {
        vectores.add(vectorB)
        Assert.assertTrue(vectorB.enfermedades.isEmpty())
        serviceVect.contagiarSimulPositivo(vectorA, vectores)
        val vectorBRecuperadoPost = serviceVect.recuperarVector(vectorB.id!!.toInt())
        Assert.assertEquals(1, vectorBRecuperadoPost.cantidadEnfermedades())
    }

    @Test
    fun verificarContagioNoExitosoPorTipo() {
        vectores.add(vectorD)
        Assert.assertTrue(vectorD.enfermedades.isEmpty())
        serviceVect.contagiarSimulPositivo(vectorC, vectores)
        val vectorDRecuperadoPost = serviceVect.recuperarVector(vectorD.id!!.toInt())
        Assert.assertEquals(0, (serviceVect.enfermedades(vectorDRecuperadoPost.id!!.toInt()).size))
    }

    @Test
    fun verificarContagioNoExitosoPorProbabilidad() {
        vectores.add(vectorB)
        Assert.assertTrue(vectorB.enfermedades.isEmpty())
        serviceVect.contagiarSimulNegativo(vectorA, vectores)
        val vectorBRecuperadoPost = serviceVect.recuperarVector(vectorB.id!!.toInt())
        Assert.assertEquals(0, vectorBRecuperadoPost.cantidadEnfermedades())
    }

    @Test
    fun infectarVectorYVerificarCantidadDeEnfermedades() {
        serviceVect.infectar(vectorE, especie1)
        val vectorARecuperadoPost = serviceVect.recuperarVector(vectorE.id!!.toInt())
        Assert.assertEquals(4,vectorARecuperadoPost.enfermedades.size)
    }

    @Test
    fun recuperaYVerificaCantEnfermedades(){
        val vectorERecuperado = serviceVect.recuperarVector(vectorE.id!!.toInt())
        Assert.assertEquals(3,vectorERecuperado.enfermedades.size)
    }

    @Test
    fun infectarYVerificarPorFuncionEnfermedades(){
        serviceVect.infectar(vectorA, mosquito)
        val cantidadEnfermedadesRecuperadas = serviceVect.enfermedades(vectorA.id!!.toInt()).size
        Assert.assertEquals(1,cantidadEnfermedadesRecuperadas)
    }

    @After
    fun cleanup() {
        serviceData.eliminarTodo()
    }
}
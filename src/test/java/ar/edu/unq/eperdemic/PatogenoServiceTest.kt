package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDao
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImp
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.Impl.DataServiceImp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.hibernate.exception.ConstraintViolationException


class PatogenoServiceTest {

    lateinit var service: PatogenoService
    lateinit var patogeno: Patogeno
    lateinit var patogeno2: Patogeno
    lateinit var patogeno3: Patogeno
    lateinit var serviceVec: VectorService
    lateinit var serviceUbic: UbicacionService
    lateinit var serviceData: DataService

    @Before
    fun crearModelo() {
        this.service = PatogenoServiceImp(
                HibernatePatogenoDAO(),
                HibernateDataDAO()
        )

        this.serviceVec = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
        this.serviceUbic = UbicacionServiceImp(HibernateUbicacionDAO(), UbicacionNeo4jDao(), HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()))
        this.serviceData = DataServiceImp(HibernateDataDAO())
    }

    @Test
    fun crearUnPatogenoYCorroborarId() {
        patogeno = Patogeno("Priones", 30, 20, 22)
        val id = service.crearPatogeno(patogeno)
        Assert.assertEquals(1, id)
    }

    @Test(expected = ConstraintViolationException::class)
    fun creaPatogenoEIntentaCrearOtroSinExitoPorTenerElMismoTipo() {
        patogeno = Patogeno("Virus", 20, 10, 12)
        service.crearPatogeno(patogeno)
        patogeno2 = Patogeno("Virus", 20, 10, 12)

        service.crearPatogeno(patogeno2)
    }

    @Test
    fun creaUnPatogenoYAlRecuperarloVerificaQueSeaElMismo() {
        patogeno = Patogeno("Virus", 20, 10, 12)
        val id = service.crearPatogeno(patogeno)
        Assert.assertEquals(patogeno.tipo, service.recuperarPatogeno(id).tipo)
    }

    @Test
    fun intentaRecuperarPatogenoNuncaCreadoYDevuelveNull() {
        service.recuperarPatogeno(35)
        Assert.assertEquals(null, service.recuperarPatogeno(35))
    }

    @Test
    fun agregarEspecieAPatogenoYCorroboraQueEsteAgregada() {
        patogeno = Patogeno("Covid", 60, 70, 90)
        val id = service.crearPatogeno(patogeno)
        val especie = service.agregarEspecie(id, "Rojo", "Mexico", 23)
        val patogenoRecuperado = service.recuperarPatogeno(id)
        Assert.assertEquals(patogenoRecuperado, especie.owner)
    }

    @Test(expected = ConstraintViolationException::class)
    fun agregarEspecieAPatogenoEIntentaAgregarOtraRepetidaSinExito() {
        patogeno = Patogeno("Covid", 60, 70, 90)
        val id = service.crearPatogeno(patogeno)
        val especie1 = service.agregarEspecie(id, "Rojo", "Mexico", 23)
        service.agregarEspecie(id, "Rojo", "Mexico", 23)

    }

    @Test
    fun recuperarTodosLosPatogenosYCorroborarCantidad() {
        patogeno = Patogeno("Hongo", 90, 30, 62)
        service.crearPatogeno(patogeno)
        patogeno2 = Patogeno("Covid", 50, 20, 22)
        service.crearPatogeno(patogeno2)
        patogeno3 = Patogeno("Priones", 10, 10, 12)
        service.crearPatogeno(patogeno3)
        Assert.assertEquals(3, service.recuperarATodosLosPatogenos().size)
    }

    @Test
    fun agregarEspecieAPatogenoYRecuperarEspecie() {
        patogeno = Patogeno("1-12", 20, 50, 12)
         val id = service.crearPatogeno(patogeno)
         val especie = service.agregarEspecie(id, "cruza", "Ecuador", 44)

        Assert.assertEquals(especie, (service.recuperarEspecie(especie.id!!.toInt())))
    }

    @Test
    fun verificoLaCantidadDeInfectadosDeUnPatogeno(){
        patogeno = Patogeno("1", 100, 50, 12)
        val id = service.crearPatogeno(patogeno)
        val especie = service.agregarEspecie(id, "rb", "Ecuador", 50)
        val ubicacion1 = serviceUbic.crearUbicacion("Argentina")
        var vectorA = Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Persona)
        var vectorB = Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Persona)
        vectorA = serviceVec.crearVector(vectorA)
        vectorB = serviceVec.crearVector(vectorB)
        serviceVec.infectar(vectorA,especie)
        serviceVec.infectar(vectorB,especie)
        Assert.assertEquals(2, (service.cantidadDeInfectados(especie.id!!.toInt())))
    }

    @Test
    fun verificoSiEsPandemiaUnaEspecieEnUnPatogeno(){
        patogeno = Patogeno("1", 100, 50, 12)
        val id = service.crearPatogeno(patogeno)
        val especie = service.agregarEspecie(id, "rb", "Ecuador", 50)
        val ubicacion1 = serviceUbic.crearUbicacion("Quilmes")
        val ubicacion2 = serviceUbic.crearUbicacion("La Plata")
        val ubicacion3 = serviceUbic.crearUbicacion("Hudson")
        serviceUbic.crearUbicacion("Retiro")

        var vectorA = Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Persona)
        var vectorB = Vector(ubicacion2, VectorFrontendDTO.TipoDeVector.Persona)
        var vectorC = Vector(ubicacion3, VectorFrontendDTO.TipoDeVector.Persona)

        vectorA = serviceVec.crearVector(vectorA)
        vectorB = serviceVec.crearVector(vectorB)
        vectorC = serviceVec.crearVector(vectorC)

        serviceVec.infectar(vectorA,especie)
        serviceVec.infectar(vectorB,especie)
        serviceVec.infectar(vectorC,especie)

        Assert.assertEquals(true, (service.esPandemia(especie.id!!.toInt())))
    }

    @After
    fun cleanup() {
        serviceData.eliminarTodo()
    }
}
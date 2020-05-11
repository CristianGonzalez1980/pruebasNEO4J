package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.PatogenoServiceImp
import ar.edu.unq.eperdemic.services.runner.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.runner.VectorServiceImp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class PatogenoServiceTest {

    lateinit var service: PatogenoService
    lateinit var patogeno: Patogeno
    lateinit var patogeno2: Patogeno
    lateinit var patogeno3: Patogeno
    lateinit var serviceVec: VectorService
    lateinit var serviceUbic: UbicacionService

    @Before
    fun crearModelo() {
        this.service = PatogenoServiceImp(
                HibernatePatogenoDAO(),
                HibernateDataDAO()
        )

        this.serviceVec = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernateEspecieDAO())
        this.serviceUbic = UbicacionServiceImp(HibernateUbicacionDAO(), HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernateEspecieDAO()))

    }

    @Test
    fun crearUnPatogenoYCorroborarId() {
        patogeno = Patogeno("Priones", 30, 20, 22)
        val id = service.crearPatogeno(patogeno)
        Assert.assertEquals(1, id)
    }

    @Test
    fun seCreaUnPatogenoYLuegoAlRecuperarUnPatogenoVerificoQueSeaElMismo() {
        patogeno = Patogeno("Virus", 20, 10, 12)
        val id = service.crearPatogeno(patogeno)
        Assert.assertEquals(patogeno.tipo, service.recuperarPatogeno(id).tipo)
    }

    @Test
    fun seAgregaUnaEspecieAUnPatogenoYSeCorroboraQueSeHallaAgregado() {
        patogeno = Patogeno("Covid", 60, 70, 90)
        val id = service.crearPatogeno(patogeno)
        val especie = service.agregarEspecie(id, "Rojo", "Mexico", 23)
        val patogenoRecuperado = service.recuperarPatogeno(id)
        Assert.assertEquals(patogenoRecuperado, especie.owner)
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
        val patogenoRecuperado = service.recuperarPatogeno(id)
        //revisar la implementacion de recuperarEspecie segun lo que pide el enunciado
        //Assert.assertEquals(patogenoRecuperado, (service.recuperarEspecie(id)).owner)

    }
    @Test
    fun verificoLaCantidadDeInfectadosDeUnPatogeno(){
        patogeno = Patogeno("1", 20, 50, 12)
        val id = service.crearPatogeno(patogeno)
        var especie = service.agregarEspecie(id, "rb", "Ecuador", 50)
        val ubicacion1 = serviceUbic.crearUbicacion("Argentina")
        var vectorA = Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Persona)
        var vectorB = Vector(ubicacion1, VectorFrontendDTO.TipoDeVector.Persona)
        vectorA = serviceVec.crearVector(vectorA)
        vectorB = serviceVec.crearVector(vectorB)
        vectorA.enfermedades.add(especie)
        vectorB.enfermedades.add(especie)
        especie.vectores.add(vectorA)
        especie.vectores.add(vectorB)

        Assert.assertEquals(2, (service.cantidadDeInfectados(especie.id!!.toInt())))
    }


    @After
    fun cleanup() {
        service.clear()
    }
}
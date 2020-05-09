package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyAnimal
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyHumano
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
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

class UbicacionServiceTest {

    lateinit var service: UbicacionService
    lateinit var serviceVec: VectorService
    lateinit var servicePatog: PatogenoService
    lateinit var ubi1: Ubicacion
    lateinit var ubi2: Ubicacion
    lateinit var ubi3: Ubicacion
    lateinit var vectorA: Vector
    lateinit var vectorB: Vector
    lateinit var vectorC: Vector
    lateinit var vectorD: Vector
    lateinit var vectorE: Vector
    lateinit var vectores: MutableList<Vector>
    lateinit var dao: HibernateUbicacionDAO
    lateinit var estrategia: StrategyHumano
    lateinit var estrategia1: StrategyAnimal
    lateinit var patogeno: Patogeno
    lateinit var especie1: Especie
    lateinit var especie2: Especie

    @Before
    fun crearModelo() {
        this.service = UbicacionServiceImp(HibernateUbicacionDAO(),
                HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO()))
        this.serviceVec = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO())
        this.servicePatog = PatogenoServiceImp(HibernatePatogenoDAO(), HibernateDataDAO())

        //ubi1 = service.crearUbicacion("Bernal" )
        estrategia = StrategyHumano()
        estrategia1 = StrategyAnimal()
        patogeno = Patogeno("Virus", 80, 80, 80)
        val id = servicePatog.crearPatogeno(patogeno)
        patogeno = servicePatog.recuperarPatogeno(id)
        especie1 = patogeno.agregarEspecie("Dengue", "Argentina", 15)
        especie2 = patogeno.agregarEspecie("Covid19", "China", 20)
        ubi3 = service.crearUbicacion("La Plata")
        ubi2 = service.crearUbicacion("Quilmes")
        vectorA = Vector(ubi3, VectorFrontendDTO.TipoDeVector.Persona)
        vectorB = Vector(ubi2, VectorFrontendDTO.TipoDeVector.Animal)
        vectorC = Vector(ubi2, VectorFrontendDTO.TipoDeVector.Animal)
        vectorD = Vector(ubi3, VectorFrontendDTO.TipoDeVector.Persona)
        vectorE = Vector(ubi3, VectorFrontendDTO.TipoDeVector.Persona)
        vectorD = serviceVec.crearVector(vectorD)
        vectorE = serviceVec.crearVector(vectorE)
        serviceVec.infectar(vectorD, especie2)
        vectorA = serviceVec.crearVector(vectorA)
        service.actualizar(ubi3)
        vectorB = serviceVec.crearVector(vectorB)
        vectorC = serviceVec.crearVector(vectorC)
        vectorA = serviceVec.recuperarVector(vectorA.id!!.toInt())
    }

    @Test
    fun recuperarCantidadDeUbicacion() {
        Assert.assertEquals(3, service.recuperar("La Plata").vectores.size)
    }

    @Test
    fun cambioDeUbicacion() {
        Assert.assertEquals("La Plata", vectorA.location!!.nombreDeLaUbicacion)
        service.mover(vectorA.id!!.toInt(), "Quilmes")

        val vectorARecuperado = serviceVec.recuperarVector(vectorA.id!!.toInt())
        Assert.assertEquals("Quilmes", vectorARecuperado.location!!.nombreDeLaUbicacion)
    }

    @Test
    fun cambiaUbicacionEInfectaVectores() {
        service.mover(vectorD.id!!.toInt(), "Quilmes")
        val vectores: MutableList<Vector> = service.recuperar("Quilmes").vectores.toMutableList()
        val totalDeInfectados = vectores.count { it.estaInfectado() }
        Assert.assertEquals(3, totalDeInfectados)
    }

    @Test
    fun cambiaUbicacionVectorSanoNoInfectaVectores() {
        service.mover(vectorE.id!!.toInt(), "Quilmes")
        val vectores: MutableList<Vector> = service.recuperar("Quilmes").vectores.toMutableList()
        val totalDeInfectados = vectores.count { it.estaInfectado() }
        Assert.assertEquals(0, totalDeInfectados)
    }

    @Test
    fun expandeLasEnfermedadesEnLaUbicacion() {
        service.expandir("La Plata")
        val vectores: MutableList<Vector> = service.recuperar("La Plata").vectores.toMutableList()
        val totalDeInfectados = vectores.count { it.estaInfectado() }
        Assert.assertEquals(3, totalDeInfectados)//ya reconoce strategia de todos pero no contagia
    }

    @Test
    fun intentaExpandirEnfermedadPeroNoLoLogra() {
        service.expandir("Quilmes")
        val vectores: MutableList<Vector> = service.recuperar("Quilmes").vectores.toMutableList()
        val totalDeInfectados = vectores.count { it.estaInfectado() }
        Assert.assertEquals(0, totalDeInfectados)
    }

    @After
    fun cleanup() {
        //Destroy cierra la session factory y fuerza a que, la proxima vez, una nueva tenga
        //que ser creada.
        service.clear()
    }
}
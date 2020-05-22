package ar.edu.unq.eperdemic

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.services.MutacionService
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.MutacionServiceImp
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImp
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImp
import ar.edu.unq.eperdemic.services.impl.VectorServiceImp
import ar.edu.unq.eperdemic.utils.DataService
import ar.edu.unq.eperdemic.utils.Impl.DataServiceImp
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import javax.persistence.PersistenceException
import kotlin.properties.Delegates

class MutacionServiceTest {

    lateinit var servicePatog: PatogenoService
    lateinit var serviceData: DataService
    lateinit var serviceMut: MutacionService
    lateinit var serviceVec: VectorService
    lateinit var serviceUbi: UbicacionService
    lateinit var patogeno1: Patogeno
    lateinit var patogeno2: Patogeno
    lateinit var patogeno3: Patogeno
    lateinit var especie1: Especie
    lateinit var especie2: Especie
    lateinit var especie3: Especie
    lateinit var especie4: Especie
    lateinit var especie5: Especie
    lateinit var mutacion1: Mutacion
    lateinit var mutacion2: Mutacion
    lateinit var mutacion3: Mutacion
    lateinit var mutacion4: Mutacion
    lateinit var mutacion5: Mutacion
    lateinit var mutacion6: Mutacion
    lateinit var mutacion7: Mutacion
    var idPat1 by Delegates.notNull<Int>()
    var idPat2 by Delegates.notNull<Int>()
    var idPat3 by Delegates.notNull<Int>()

    @Before
    fun crearModelo() {
        this.servicePatog = PatogenoServiceImp(HibernatePatogenoDAO(), HibernateDataDAO())
        this.serviceData = DataServiceImp(HibernateDataDAO())
        this.serviceMut = MutacionServiceImp(HibernateDataDAO(), HibernateMutacionDAO(), HibernatePatogenoDAO())
        this.serviceVec = VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO())
        this.serviceUbi = UbicacionServiceImp(HibernateUbicacionDAO(), HibernateDataDAO(), HibernateVectorDAO(), VectorServiceImp(HibernateVectorDAO(), HibernateDataDAO(), HibernatePatogenoDAO()))

        /*//se persisten mutaciones
        mutacion1 = serviceMut.crearMutacion(Mutacion(1, mutableListOf(), mutableListOf(), Potencialidad.Letalidad))
        mutacion2 = serviceMut.crearMutacion(Mutacion(2, mutableListOf(), mutableListOf(), Potencialidad.Contagio))
        mutacion3 = serviceMut.crearMutacion(Mutacion(3, mutableListOf(), mutableListOf(), Potencialidad.Defensa))
        mutacion4 = serviceMut.crearMutacion(Mutacion(4, mutableListOf(), mutableListOf(), Potencialidad.Letalidad))
        mutacion5 = serviceMut.crearMutacion(Mutacion(5, mutableListOf(mutacion1, mutacion2), mutableListOf(), Potencialidad.Contagio))
        mutacion6 = serviceMut.crearMutacion(Mutacion(6, mutableListOf(), mutableListOf(), Potencialidad.Defensa))
        mutacion7 = serviceMut.crearMutacion(Mutacion(7, mutableListOf(), mutableListOf(), Potencialidad.Letalidad))*/
        mutacion1 = serviceMut.crearMutacion(Mutacion(1, mutableListOf(), Potencialidad.Letalidad))
        mutacion2 = serviceMut.crearMutacion(Mutacion(2, mutableListOf(), Potencialidad.Contagio))
        mutacion3 = serviceMut.crearMutacion(Mutacion(3, mutableListOf(), Potencialidad.Defensa))
        mutacion4 = serviceMut.crearMutacion(Mutacion(4, mutableListOf(), Potencialidad.Letalidad))
        mutacion5 = serviceMut.crearMutacion(Mutacion(5, mutableListOf(mutacion1, mutacion2), Potencialidad.Contagio))
        mutacion6 = serviceMut.crearMutacion(Mutacion(6, mutableListOf(), Potencialidad.Defensa))
        mutacion7 = serviceMut.crearMutacion(Mutacion(7, mutableListOf(), Potencialidad.Letalidad))

        //se instancian patogenos
        patogeno1 = Patogeno("Bacteria", 10, 10, 10)
        patogeno2 = Patogeno("Virus", 66, 100, 20)//contagia
        patogeno3 = Patogeno("Hongo", 30, 30, 30)

        //se persisten patogenos
        idPat1 = servicePatog.crearPatogeno(patogeno1)
        idPat2 = servicePatog.crearPatogeno(patogeno2)
        idPat3 = servicePatog.crearPatogeno(patogeno3)

        //se persisten especies
        especie1 = servicePatog.agregarEspecie(1, "Covid19", "China", 0)
        especie2 = servicePatog.agregarEspecie(2, "H5N1", "Espania", 0)
        especie3 = servicePatog.agregarEspecie(3, "Dengue", "Brasil", 2)
        especie4 = servicePatog.agregarEspecie(1, "H1N1", "Francia", 10)
        especie5 = servicePatog.agregarEspecie(2, "Ebola", "Congo", 7)
    }


    @Test
    fun intentaMutarUnaEspecieNotieneAdnSuficiente() {
        serviceMut.mutar(especie1.id!!.toInt(), mutacion7.id!!.toInt())
        especie1 = servicePatog.recuperarEspecie(especie1.id!!.toInt())
        patogeno1 = servicePatog.recuperarPatogeno(idPat1)
        Assert.assertEquals(0, especie1.mutaciones.size)
        Assert.assertEquals(10, patogeno1.letalidad)
    }

    @Test
    fun Infecta5VectoresyObtiene1puntoDeAdn() {
        val vector1: Vector = serviceVec.crearVector(Vector(serviceUbi.crearUbicacion("Mar del Plata"), VectorFrontendDTO.TipoDeVector.Persona))
        val vector2: Vector = serviceVec.crearVector(Vector(vector1.location!!, VectorFrontendDTO.TipoDeVector.Persona))
        val vector3: Vector = serviceVec.crearVector(Vector(vector1.location!!, VectorFrontendDTO.TipoDeVector.Persona))
        val vector4: Vector = serviceVec.crearVector(Vector(vector1.location!!, VectorFrontendDTO.TipoDeVector.Persona))
        val vector5: Vector = serviceVec.crearVector(Vector(vector1.location!!, VectorFrontendDTO.TipoDeVector.Persona))
        serviceVec.infectar(vector1, especie2)
        serviceVec.contagiar(vector1, mutableListOf(vector2, vector3, vector4, vector5))
        especie2 = servicePatog.recuperarEspecie(especie2.id!!.toInt())
        Assert.assertEquals(1, especie2.adn)
    }

    @Test
    fun MutaYSeCorroboraLaCantidadDeAdnRestante() {
        Assert.assertEquals(2, especie3.adn)
        serviceMut.mutar(especie3.id!!.toInt(), mutacion1.id!!.toInt())
        especie3 = servicePatog.recuperarEspecie(especie3.id!!.toInt())
        Assert.assertEquals(1, especie3.adn)
    }

    /*
        @Test
        fun MutaySeHabilitanNuevasMutaciones() {}

    @Test
    fun intentaMutarUnaEspecieNotieneMutacionesPrevias() {
        //tiene el adn muta y no requiere mutaciones previas
        serviceMut.mutar(especie4.id!!.toInt(), mutacion1.id!!.toInt())
        especie4 = servicePatog.recuperarEspecie(especie4.id!!.toInt())
        Assert.assertEquals(1, especie4.mutaciones.size)
        //quiere mutar nuevamente tiene adn pero no las mutaciones requeridas
        serviceMut.mutar(especie4.id!!.toInt(), mutacion5.id!!.toInt())
        especie4 = servicePatog.recuperarEspecie(especie4.id!!.toInt())
        Assert.assertEquals(1, especie4.mutaciones.size)

    }
    */
    @Test
    fun MutaEIncrementaElAtributoCorrepondiente() {
        patogeno3 = servicePatog.recuperarPatogeno(3)
        Assert.assertEquals(30, patogeno3.letalidad)
        serviceMut.mutar(especie3.id!!.toInt(), mutacion1.id!!.toInt())
        patogeno3 = servicePatog.recuperarPatogeno(3)
        Assert.assertEquals(31, patogeno3.letalidad)
    }

    @Test
    fun AlMutarVerficaQueElValorDelAtributoYaEs100YNoHaceNada() {
        patogeno2 = servicePatog.recuperarPatogeno(2)
        Assert.assertEquals(100, patogeno2.defensa)
        serviceMut.mutar(especie5.id!!.toInt(), mutacion6.id!!.toInt())
        patogeno2 = servicePatog.recuperarPatogeno(2)
        Assert.assertEquals(100, patogeno2.defensa)
    }

    @Test
    fun intentaAdquirirUnaMutacionYaAdquiridaPreviamente() { //Da amarillo porque no se estan persistiendo las mutaciones adquiridas, al recuperar especie siempre tiene la lista de mutaciones vacías.
        val especie3RecuperadaPrev = servicePatog.recuperarEspecie(especie3.id!!.toInt())
        Assert.assertTrue(especie3RecuperadaPrev.mutaciones.isEmpty()) //Antes de mutar
        serviceMut.mutar(especie3.id!!.toInt(), mutacion1.id!!.toInt())
        val especie3RecuperadaPost = servicePatog.recuperarEspecie(especie3.id!!.toInt())
        Assert.assertEquals(1, especie3RecuperadaPost.mutaciones.size) //Despues de mutar
        serviceMut.mutar(especie3.id!!.toInt(), mutacion1.id!!.toInt())
        val especie3RecuperadaPostB = servicePatog.recuperarEspecie(especie3.id!!.toInt())
        Assert.assertEquals(1, especie3RecuperadaPostB.mutaciones.size) //Despues de intentar mutar otra vez verifica que quede igual por ya tenerla previamente
    }

    @After
    fun cleanup() {
        serviceData.eliminarTodo()
    }
}
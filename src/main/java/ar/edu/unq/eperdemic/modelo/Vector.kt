package ar.edu.unq.eperdemic.modelo

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyAnimal
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyHumano
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyInsecto
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategySuperClase
import java.lang.RuntimeException
import javax.persistence.*
import javax.transaction.Transactional
import kotlin.jvm.Transient

@Entity(name = "vector")
@Table(name = "vector")
class Vector() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, columnDefinition = "VARCHAR(64)")
    var tipo: VectorFrontendDTO.TipoDeVector? = null

    @ManyToOne
    var location: Ubicacion? = null

    @ManyToOne
    var especie: Especie? = null

    @OneToMany(mappedBy = "vector", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var enfermedades: MutableSet<Especie> = HashSet()

    @Transient
    var estrategiaDeContagio: StrategySuperClase? = null

    constructor(location: Ubicacion, tipoDeVector: VectorFrontendDTO.TipoDeVector) : this() {
        this.location = location
        location.vectores.add(this)
        this.tipo = tipoDeVector
        this.initEstrategia()
    }

    /*fun contagiar(vectorInfectado: Vector, vectores: List<Vector>) {
        for (v: Vector in vectores) {
            estrategiaDeContagio?.darContagio(vectorInfectado, v)!!
        }
    }*/

    fun cambiarDeUbicacion(ubicacion: Ubicacion) {
        this.location!!.desAlojarVector(this)
        ubicacion.alojarVector(this)
    }

    fun estaInfectado(): Boolean {
        return (this.enfermedades.isNotEmpty())
    }

    fun initEstrategia() {
        if (this.tipo!!.name == "Persona") {
            this.estrategiaDeContagio = StrategyHumano()
        }
        if (this.tipo!!.name == "Animal") {
            this.estrategiaDeContagio = StrategyAnimal()
        }
        if (this.tipo!!.name == "Insecto") {
            this.estrategiaDeContagio = StrategyInsecto()
        }
    }

    fun agregarEnfermedad(unaEspecie: Especie) {
        this.especie = unaEspecie
        this.enfermedades.add(especie!!)

    }
}
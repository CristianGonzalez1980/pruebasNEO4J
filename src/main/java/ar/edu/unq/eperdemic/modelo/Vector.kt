package ar.edu.unq.eperdemic.modelo

import ar.edu.unq.eperdemic.dto.VectorFrontendDTO
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyAnimal
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyHumano
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategyInsecto
import ar.edu.unq.eperdemic.modelo.StrategyVectores.StrategySuperClase
import javax.persistence.*
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

    @ManyToMany(mappedBy = "vectores", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var enfermedades: MutableSet<Especie> = HashSet()

    @Transient
    var estrategiaDeContagio: StrategySuperClase? = null

    constructor(location: Ubicacion, tipoDeVector: VectorFrontendDTO.TipoDeVector) : this() {
        this.location = location
        location.vectores.add(this)
        this.tipo = tipoDeVector
        this.initEstrategia()
    }

    fun setId(idNuevo: Long) {
        this.id = idNuevo
    }

    fun cantidadEnfermedades(): Int {
        return (this.enfermedades.size)
    }

    fun cambiarDeUbicacion(ubicacion: Ubicacion) {
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
}
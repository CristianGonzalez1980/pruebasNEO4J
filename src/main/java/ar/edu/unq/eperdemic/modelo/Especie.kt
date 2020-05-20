package ar.edu.unq.eperdemic.modelo

import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity(name = "especie")
@Table(name = "especie")
class Especie() : Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(64)")
    var nombre: String? = null
    var paisDeOrigen: String? = null

    @ManyToOne
    var owner: Patogeno? = null

    var adn: Int = 0

    @Column(nullable = false, length = 500)
    var countIncAdn: Int = 0

/*    @ManyToOne
    var vector : Vector? = null*/

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val mutaciones: MutableList<Mutacion> = ArrayList()

    @ManyToMany
    var vectores: MutableSet<Vector> = HashSet()

    fun tieneMutaciones(mutaciones: List<Mutacion>): Boolean {         //Corrobora que la especie tenga las mutaciones
        var resultado = true                                            // que requiere la nueva mutacion a adquirir
        for (mutacion: Mutacion in mutaciones) {
            resultado = (resultado and this.mutaciones.contains(mutacion))
        }
        return resultado
    }

    fun sumarAdn() {
        ++countIncAdn
        if (this.countIncAdn == 5) {
            ++adn
            countIncAdn = 0
        }
    }

    fun agregarMutacion(unaMutacion: Mutacion) {
        if (this.noLaTiene(unaMutacion) && (this.adn >= unaMutacion.getAdnNecesario()!!) && this.tieneMutaciones(unaMutacion.mutacionesNecesarias())) {
            this.adn = (this.adn - unaMutacion.getAdnNecesario()!!)
            this.mutaciones.add(unaMutacion)
            unaMutacion.potenciarEspecie(this)
        }
    }

    fun noLaTiene(unaMutacion: Mutacion) : Boolean {
        return (!this.mutaciones.contains(unaMutacion))
    }

    constructor(owner: Patogeno, nombre: String, paisDeOrigen: String, adn : Int) : this() {
        this.owner = owner
        this.nombre = nombre
        this.paisDeOrigen = paisDeOrigen
        this.adn = adn
    }

    override fun toString(): String {
        return nombre!!
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val especie = o as Especie?
        return owner!!.id == (especie!!.owner!!).id
    }
/*
    fun agregarVector(unVector: Vector) {
        this.vector = unVector
        this.vectores.add(vector!!)
*/

}

package ar.edu.unq.eperdemic.modelo

import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity(name = "patogeno")
@Table(name = "patogeno")
class Patogeno() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true, length = 500)
    var tipo: String? = null

    @OneToMany(mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var especies: MutableSet<Especie> = HashSet()

    val cantidadDeEspecies: Int
        get() = especies.size

    @Column(nullable = false, length = 500)
    var capacidadContagio: Int? = null

    @Column(nullable = false, length = 500)
    var defensa: Int? = null

    @Column(nullable = false, length = 500)
    var letalidad: Int? = null

    constructor(unTipo: String, unaCapacidad: Int, unaDefensa: Int, unaLetalidad: Int) : this() {
        this.tipo = unTipo
        this.capacidadContagio = unaCapacidad
        this.defensa = unaDefensa
        this.letalidad = unaLetalidad
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val patogeno = o as Patogeno?
        return id == patogeno!!.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    override fun toString(): String {
        return this.tipo!!
    }
    fun agregarEspecie(nombreEspecie: String, paisDeOrigen: String, adn: Int): Especie {
        val nuevaEspecie: Especie = Especie(this, nombreEspecie, paisDeOrigen)
        this.especies.add(nuevaEspecie)
        return nuevaEspecie
    }
/*    fun agregarEspecie(especie: Especie) {
        this.especies.add(especie)
    }*/

    fun incrementarCapacidadDeContagio() {
        this.capacidadContagio = this.incrementarValor(this.capacidadContagio!!)
    }

    fun incrementarLetalidal() {
        this.letalidad = this.incrementarValor(this.letalidad!!)
    }

    fun incrementarDefensa() {
        this.defensa = this.incrementarValor(this.defensa!!)
    }

    fun incrementarValor(valor: Int): Int {
        if (valor < 100) {
            return valor + 1
        } else {
            return valor
        }
    }
}
package ar.edu.unq.eperdemic.modelo

import javax.persistence.*


@Entity(name = "mutacion")
@Table(name = "mutacion")
class Mutacion() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null;
    var puntosAdnNecesarios: Int? = null

    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var mutacionesNecesarias: MutableList<Mutacion> = mutableListOf()

    var potencialidad: Potencialidad? = null

    constructor(puntos: Int, mutacionesNecesarias: MutableList<Mutacion>, potencialidad: Potencialidad) : this() {
        this.puntosAdnNecesarios = puntos
        this.mutacionesNecesarias = mutacionesNecesarias
        this.potencialidad = potencialidad
    }

    fun getAdnNecesario(): Int? {
        return (this.puntosAdnNecesarios)
    }

    fun mutacionesNecesarias(): List<Mutacion> {
        return (this.mutacionesNecesarias)
    }

    fun potenciarEspecie(especie: Especie) {

        if (this.potencialidad!!.name == "Contagio") {
            especie.owner!!.incrementarCapacidadDeContagio()
        }

        if (this.potencialidad!!.name == "Defensa") {
            especie.owner!!.incrementarDefensa()
        }

        if (this.potencialidad!!.name == "Letalidad") {
            especie.owner!!.incrementarLetalidal()
        }
    }
}
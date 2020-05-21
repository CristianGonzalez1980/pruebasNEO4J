package ar.edu.unq.eperdemic.modelo

import java.util.HashSet
import javax.persistence.*

@Entity(name = "ubicacion")
@Table(name = "ubicacion")
class Ubicacion() {
    @Id
    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(64)")
    var nombreDeLaUbicacion: String? = null

    @OneToMany(mappedBy = "location", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var vectores: MutableSet<Vector> = HashSet()

    constructor(nombreUbicacion: String) : this() {
        this.nombreDeLaUbicacion = nombreUbicacion
    }

    fun alojarVector(vector: Vector) {
        vector.location!!.desAlojarVector(vector)
        vector.location = this
        this.vectores.add(vector)
    }

    fun desAlojarVector(vector: Vector) {
        this.vectores.remove(vector)
    }

    override fun toString(): String {
        return nombreDeLaUbicacion!!
    }

    fun actualizarInfectadoseEnUbicacion(vectorInfectado : Vector , vectores : MutableList<Vector>){
        vectorInfectado.initEstrategia()
        for(v  : Vector in vectores){
            vectorInfectado.estrategiaDeContagio!!.darContagio(vectorInfectado ,v )
        }
    }
}
package ar.edu.unq.eperdemic.services.runner

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class UbicacionServiceImp(
        private val ubicacionDAO: UbicacionDAO,
        private val dataDAO: DataDAO,
        private val vectorDAO: VectorDAO,
        private val vectorServiceImp: VectorServiceImp

) : UbicacionService {

    override fun mover(vectorId: Int, nombreUbicacion: String) {

        val vectorRecuperado = vectorServiceImp.recuperarVector(vectorId)
        val ubicacionVieja = vectorRecuperado.location
        val ubicacionNueva = this.recuperar(nombreUbicacion)

        vectorRecuperado.cambiarDeUbicacion(ubicacionNueva)
        //vectorDAO.actualizar(vector) La comente porque estamos actualizando con el mismo vector que nos pasan, no tiene mucho sentido
/*        if (ubicacionVieja != null) {
            ubicacionDAO.actualizar(ubicacionVieja)
        }*/
        //this.actualizar(ubicacionVieja!!)
        this.actualizar(ubicacionNueva)
        if (vectorRecuperado.estaInfectado()) {
            vectorServiceImp.contagiar(vectorRecuperado, ubicacionNueva.vectores.toList())
        }
    }

    override fun actualizar(ubicacion: Ubicacion) {
        runTrx { ubicacionDAO.actualizar(ubicacion) }
    }

    override fun expandir(nombreUbicacion: String) {
        val ubicacion: Ubicacion = this.recuperar(nombreUbicacion)
        val vectores: MutableList<Vector> = ubicacion.vectores.toMutableList()

        if(vectores.filter {it.estaInfectado()}.isNotEmpty()){
        val vectorInfectado: Vector = vectores.filter {it.estaInfectado() }[0]
        // vectorServiceImp.contagiar(vectorInfectado, vectores)
        ubicacion.actualizarInfectadoseEnUbicacion(vectorInfectado , vectores)
        runTrx { ubicacionDAO.actualizar(ubicacion) }
        }

    }

    override fun crearUbicacion(nombre: String): Ubicacion {
        return runTrx {
            val ubicacion = Ubicacion(nombre)
            ubicacionDAO.crear(ubicacion)
        }
    }

    override public fun clear() {
        runTrx { dataDAO.clear() }
    }

    override fun recuperar(ubicacion: String): Ubicacion {
        return runTrx { ubicacionDAO.recuperar(ubicacion) }
    }
}
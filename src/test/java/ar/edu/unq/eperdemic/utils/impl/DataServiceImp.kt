package ar.edu.unq.eperdemic.utils.impl

import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner
import ar.edu.unq.eperdemic.utils.DataService

class DataServiceImp(private val dataDAO: DataDAO) : DataService {
    override fun crearSetDeDatosIniciales() {
        TODO("Not yet implemented")
    }

    override fun eliminarTodo() {
        TransactionRunner.runTrx { dataDAO.clear() }
    }
}
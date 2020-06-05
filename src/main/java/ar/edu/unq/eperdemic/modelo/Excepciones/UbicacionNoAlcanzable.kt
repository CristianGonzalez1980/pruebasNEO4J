package ar.edu.unq.eperdemic.modelo.Excepciones


class UbicacionNoAlcanzable(vector: String, ubicacionRequerida: String, camino: String) : Exception("el vector: $vector no puede llegar a la ubicacion: $ubicacionRequerida por medio del camino: $camino.")
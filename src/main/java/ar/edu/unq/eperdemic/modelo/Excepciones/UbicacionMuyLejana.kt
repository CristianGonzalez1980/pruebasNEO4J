package ar.edu.unq.eperdemic.modelo.Excepciones

class UbicacionMuyLejana(ubicacionActual: String, ubicacionRequerida: String) : Exception("Imposible llegar desde la ubicacion: $ubicacionActual a la ubicacion: $ubicacionRequerida por medio de un camino.")
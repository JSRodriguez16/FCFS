package com;

import java.util.Iterator;
import java.util.NoSuchElementException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ColaProcesos implements Iterable<Proceso> {


    public static class Nodo {
        private Proceso proceso;
        private Nodo siguiente;

        public Nodo(Proceso proceso) {
            this.proceso = proceso;
            this.siguiente = null;
        }

        public Proceso getProceso() {
            return proceso;
        }

        public Nodo getSiguiente() {
            return siguiente;
        }
    }

    private Nodo cabeza;
    private Nodo cola;
    private int tamaño;

    // Lista observable para sincronizar con la UI de JavaFX
    private final ObservableList<Proceso> listaObservable = FXCollections.observableArrayList();

    public ColaProcesos() {
        this.cabeza = null;
        this.cola = null;
        this.tamaño = 0;
    }

    public void encolar(Proceso proceso) {
        Nodo nuevo = new Nodo(proceso);
        if (cola == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamaño++;
        sincronizar();
    }

    public Proceso desencolar() {
        if (cabeza == null) {
            throw new NoSuchElementException("La cola está vacía");
        }
        Proceso proceso = cabeza.proceso;
        cabeza = cabeza.siguiente;
        if (cabeza == null) {
            cola = null;
        }
        tamaño--;
        sincronizar();
        return proceso;
    }

    public boolean eliminar(Proceso proceso) {
        if (cabeza == null) {
            return false;
        }
        if (cabeza.proceso == proceso) {
            cabeza = cabeza.siguiente;
            if (cabeza == null) {
                cola = null;
            }
            tamaño--;
            sincronizar();
            return true;
        }
        Nodo anterior = cabeza;
        Nodo actual = cabeza.siguiente;
        while (actual != null) {
            if (actual.proceso == proceso) {
                anterior.siguiente = actual.siguiente;
                if (actual == cola) {
                    cola = anterior;
                }
                tamaño--;
                sincronizar();
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    public Proceso obtener(int indice) {
        if (indice < 0 || indice >= tamaño) {
            throw new IndexOutOfBoundsException("Índice: " + indice + ", Tamaño: " + tamaño);
        }
        Nodo actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.proceso;
    }

    public Proceso frente() {
        if (cabeza == null) {
            throw new NoSuchElementException("La cola está vacía");
        }
        return cabeza.proceso;
    }

    public int tamaño() {
        return tamaño;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    public Nodo getCabeza() {
        return cabeza;
    }

    public ObservableList<Proceso> getListaObservable() {
        return listaObservable;
    }

    private void sincronizar() {
        listaObservable.clear();
        Nodo actual = cabeza;
        while (actual != null) {
            listaObservable.add(actual.proceso);
            actual = actual.siguiente;
        }
    }

    public void refrescar() {
        sincronizar();
    }

    @Override
    public Iterator<Proceso> iterator() {
        return new Iterator<>() {
            private Nodo actual = cabeza;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public Proceso next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                Proceso proceso = actual.proceso;
                actual = actual.siguiente;
                return proceso;
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cola: [");
        Nodo actual = cabeza;
        while (actual != null) {
            sb.append(actual.proceso.getNombreProceso());
            if (actual.siguiente != null) {
                sb.append(" -> ");
            }
            actual = actual.siguiente;
        }
        sb.append("]");
        return sb.toString();
    }
}

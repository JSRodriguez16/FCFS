# Simulador de Planificación de Procesos — FCFS (First Come, First Served)

## 1. Descripción General

Aplicación de escritorio desarrollada en **Java 21** con **JavaFX** que simula el algoritmo de planificación de CPU **FCFS (First Come, First Served)**. Permite al usuario crear procesos con tiempos de ráfaga personalizados y visualizar en tiempo real cómo el planificador los atiende en el orden en que fueron creados, sin interrupciones (no preemptive).

---

## 2. Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|---|---|---|
| Java | 21 | Lenguaje de programación |
| JavaFX | 21.0.2 | Interfaz gráfica de usuario |
| Maven | - | Gestión de dependencias y compilación |
| FXML | - | Definición declarativa de la interfaz |

---

## 3. Estructura del Proyecto

```
process/
├── pom.xml                          # Configuración Maven
├── src/main/java/
│   ├── module-info.java             # Módulo Java (requiere javafx.controls y javafx.fxml)
│   └── com/
│       ├── App.java                 # Punto de entrada de la aplicación
│       ├── Proceso.java             # Modelo de datos de un proceso
│       ├── ColaProcesos.java        # Estructura de datos de cola con lista enlazada
│       └── FCFSController.java      # Controlador con la lógica de simulación
└── src/main/resources/com/
    └── FCFS.fxml                    # Definición de la interfaz gráfica
```

---

## 4. Descripción de Clases

### 4.1 App.java — Punto de Entrada

Clase principal que extiende `Application` de JavaFX.

**Funcionalidad:**
- Carga el archivo `FCFS.fxml` como interfaz gráfica.
- Crea una ventana de 700×500 píxeles no redimensionable.
- Contiene el método `main()` que inicia la aplicación.

---

### 4.2 Proceso.java — Modelo de Datos

Representa un proceso del sistema operativo con los siguientes atributos:

| Atributo | Tipo | Descripción |
|---|---|---|
| `pid` | `int` | Identificador único del proceso |
| `tiempoRafaga` | `int` | Tiempo total de CPU requerido (burst time) |
| `tiempoRestante` | `int` | Tiempo de CPU que falta por ejecutar |
| `estado` | `String` | Estado actual: `"LISTO"`, `"Listo"`, `"Ejecutando"`, `"Terminado"` |
| `tiempoEspera` | `int` | Tiempo que el proceso esperó antes de ejecutarse |

**Al crearse**, el proceso inicia en estado `"LISTO"` con `tiempoRestante = tiempoRafaga`.

---

### 4.3 ColaProcesos.java — Estructura de Datos de Cola

Implementación propia de una **cola (FIFO)** basada en una **lista enlazada simple** con manejo de apuntadores.

#### Estructura interna:

- **Nodo**: Clase interna con dos campos:
  - `proceso`: referencia al objeto `Proceso`.
  - `siguiente`: apuntador al siguiente nodo en la cola.
- **cabeza**: Apuntador al primer nodo (frente de la cola).
- **cola**: Apuntador al último nodo (final de la cola).
- **tamaño**: Contador de elementos.

#### Operaciones:

| Método | Complejidad | Descripción |
|---|---|---|
| `encolar(Proceso)` | O(1) | Agrega un proceso al final de la cola. Actualiza el apuntador `cola.siguiente` al nuevo nodo y mueve `cola` al nuevo nodo. |
| `desencolar()` | O(1) | Remueve y retorna el proceso del frente. Mueve el apuntador `cabeza` al siguiente nodo. |
| `eliminar(Proceso)` | O(n) | Recorre la lista con apuntadores `anterior` y `actual` para localizar y eliminar un proceso específico, relinking los apuntadores. |
| `obtener(int)` | O(n) | Accede a un proceso por índice recorriendo desde `cabeza` siguiendo los apuntadores `siguiente`. |
| `frente()` | O(1) | Retorna el proceso al frente sin removerlo. |
| `estaVacia()` | O(1) | Verifica si `cabeza == null`. |
| `tamaño()` | O(1) | Retorna el contador de elementos. |
| `iterator()` | - | Implementa `Iterable<Proceso>` para permitir recorrido con `for-each`. |

#### Sincronización con JavaFX:

La clase mantiene una `ObservableList<Proceso>` interna que se sincroniza automáticamente con la estructura enlazada cada vez que se modifica la cola. Esto permite que los componentes `ListView` y `TableView` de JavaFX reflejen los cambios.

---

### 4.4 FCFSController.java — Controlador Principal

Maneja toda la lógica de la aplicación. Se divide en las siguientes funcionalidades:

#### 4.4.1 Gestión de Procesos (CRUD)

| Acción | Método | Descripción |
|---|---|---|
| **Crear** | `create()` | Crea un proceso con PID auto-incrementado y la ráfaga seleccionada en el spinner. Lo encola en `ColaProcesos`. |
| **Modificar** | `modify()` | Cambia la ráfaga del proceso seleccionado en el ListView. Resetea su estado a `"LISTO"`. |
| **Eliminar** | `delete()` | Elimina el proceso seleccionado de la cola y renumera los PIDs restantes. |

#### 4.4.2 Simulación FCFS

La simulación se ejecuta mediante un `Timeline` de JavaFX que genera un "tick" cada segundo:

1. **Iniciar** (`start()`): Resetea todos los procesos a estado `"Listo"`, bloquea la edición, e inicia el Timeline.
2. **Tick de simulación** (`processSimulationTick()`):
   - Toma el proceso en la posición `currentProcessIndex` de la cola.
   - Si es la primera vez que se atiende, calcula su tiempo de espera.
   - Lo marca como `"Ejecutando"` y le resta 1 unidad de tiempo restante.
   - Cuando `tiempoRestante` llega a 0, lo marca como `"Terminado"` y avanza `currentProcessIndex` al siguiente proceso.
   - Cuando no hay más procesos, finaliza la simulación.
3. **Pausar/Reanudar** (`stopResume()`): Pausa o reanuda el Timeline.
4. **Reiniciar** (`restart()`): Detiene la simulación y resetea todos los procesos a su estado original.

#### 4.4.3 Resultados

Al finalizar la simulación, `buildFinalResultText()` calcula y muestra:
- El **tiempo de espera** individual de cada proceso.
- El **tiempo promedio de espera** de todos los procesos.

---

## 5. Interfaz Gráfica (FCFS.fxml)

La ventana (700×500) se divide en las siguientes secciones:

```
┌──────────────────────────────────────────────────────────┐
│           Planificación de Procesos de la CPU             │
├────────────────────────┬─────────────────────────────────┤
│ Asignar Ráfaga:        │ Simulador del planificador      │
│ [Spinner] [Crear]      │                                 │
│ [Modificar]            │ ┌─────────────────────────────┐ │
│                        │ │ Proceso│Ráfaga│Rest.│Estado │ │
│ ┌────────────────────┐ │ │        │      │     │Espera │ │
│ │   Lista de         │ │ │ (TableView con los procesos)│ │
│ │   Procesos         │ │ │                             │ │
│ │   (ListView)       │ │ └─────────────────────────────┘ │
│ └────────────────────┘ │                                 │
│ [Eliminar]             │ [Iniciar] [Pausar] [Reiniciar]  │
├────────────────────────┴─────────────────────────────────┤
│ Área de resultados (tiempos de espera y promedio)         │
└──────────────────────────────────────────────────────────┘
```

### Componentes:

| Componente | Tipo | Función |
|---|---|---|
| `burstSpinner` | Spinner (1-100) | Seleccionar tiempo de ráfaga |
| `processListView` | ListView | Listar procesos creados |
| `processPlanifierTableView` | TableView | Tabla con columnas: Proceso, Ráfaga, Restante, Estado, Espera |
| `createButton` | Button | Crear nuevo proceso |
| `modifyButton` | Button | Modificar proceso seleccionado |
| `deleteButton` | Button | Eliminar proceso seleccionado |
| `startButton` | Button | Iniciar simulación |
| `stopResumeButton` | Button | Pausar / Reanudar simulación |
| `restartButton` | Button | Reiniciar simulación |
| `resultText` | Text | Mostrar resultados finales |

### Indicadores visuales:
- El proceso actualmente en ejecución se resalta con fondo **verde** (`#9DF59D`) en la tabla.
- Los botones se habilitan/deshabilitan según el contexto (ej: no se puede editar durante la simulación).

---

## 6. Flujo de Uso

1. El usuario establece un tiempo de ráfaga en el spinner y presiona **Crear** para agregar procesos a la cola.
2. Opcionalmente, selecciona un proceso del ListView para **Modificar** su ráfaga o **Eliminarlo**.
3. Presiona **Iniciar** para comenzar la simulación FCFS.
4. Cada segundo, el simulador ejecuta 1 unidad de tiempo del proceso actual (resaltado en verde).
5. Cuando un proceso termina, pasa automáticamente al siguiente en la cola (FCFS).
6. El usuario puede **Pausar** y **Reanudar** la simulación en cualquier momento.
7. Al terminar todos los procesos, se muestran los tiempos de espera individuales y el promedio.
8. El usuario puede presionar **Reiniciar** para volver al estado inicial y correr otra simulación.

---

## 7. Algoritmo FCFS

**First Come, First Served** es el algoritmo de planificación de CPU más simple:

- Los procesos se atienden en el **orden exacto** en que llegaron a la cola (primero en llegar, primero en servirse).
- Es **no preemptive**: una vez que un proceso empieza a ejecutarse, no se interrumpe hasta que termina.
- El tiempo de espera de un proceso es igual a la suma de los tiempos de ráfaga de todos los procesos que lo preceden en la cola.

### Ejemplo:

| Proceso | Ráfaga | Tiempo de Espera |
|---|---|---|
| P1 | 5 | 0 |
| P2 | 3 | 5 |
| P3 | 2 | 8 |

**Tiempo promedio de espera** = (0 + 5 + 8) / 3 = **4.33**

---

## 8. Ejecución

```bash
cd process
mvn javafx:run
```

Requisitos: Java 21 y Maven instalados y configurados en el PATH del sistema.

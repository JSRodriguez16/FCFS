package com;

public class Proceso {
    private int pid;
    private int tiempoRafaga;
    private int tiempoRestante;
    private String estado;
    private int tiempoEspera;

    public Proceso(int pid, int tiempoRafaga) {
        this.pid = pid;
        this.tiempoRafaga = tiempoRafaga;
        this.tiempoRestante = tiempoRafaga;
        this.estado = "LISTO";
        this.tiempoEspera = 0;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getTiempoRafaga() {
        return tiempoRafaga;
    }

    public void setTiempoRafaga(int tiempoRafaga) {
        this.tiempoRafaga = tiempoRafaga;
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }

    public void setTiempoRestante(int tiempoRestante) {
        this.tiempoRestante = tiempoRestante;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getTiempoEspera() {
        return tiempoEspera;
    }

    public void setTiempoEspera(int tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public String getNombreProceso() {
        return "Proceso " + pid;
    }

    @Override
    public String toString() {
        return String.format("[PID: %d | Ráfaga: %d | Restante: %d | Estado: %s]",
                pid, tiempoRafaga, tiempoRestante, estado);
    }
}
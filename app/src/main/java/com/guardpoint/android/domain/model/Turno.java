package com.guardpoint.android.domain.model;

public class Turno {

    private final String turnoId;
    private final String postoId;
    private final String postoNome;
    private final int intervaloMinutos;
    private final String tokenSessao;
    private final String status;
    private final long ultimoCheckinMillis;
    private final long inicioPrevistoMillis;

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis) {
        this.turnoId = turnoId;
        this.postoId = postoId;
        this.postoNome = postoNome;
        this.intervaloMinutos = intervaloMinutos;
        this.tokenSessao = tokenSessao;
        this.status = status;
        this.ultimoCheckinMillis = ultimoCheckinMillis;
        this.inicioPrevistoMillis = inicioPrevistoMillis;
    }

    public String getTurnoId() { return turnoId; }
    public String getPostoId() { return postoId; }
    public String getPostoNome() { return postoNome; }
    public int getIntervaloMinutos() { return intervaloMinutos; }
    public String getTokenSessao() { return tokenSessao; }
    public String getStatus() { return status; }
    public long getUltimoCheckinMillis() { return ultimoCheckinMillis; }
    public long getInicioPrevistoMillis() { return inicioPrevistoMillis; }

    public long getDeadlineMillis() {
        return ultimoCheckinMillis + (intervaloMinutos * 60L * 1000L);
    }

    public long getTempoRestanteMillis() {
        return Math.max(0, getDeadlineMillis() - System.currentTimeMillis());
    }
}

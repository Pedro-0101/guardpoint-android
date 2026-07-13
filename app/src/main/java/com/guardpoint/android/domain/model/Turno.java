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
    private final long proximoDeadlineMillis;
    private final String tipoProximoDeadline;

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis) {
        this(turnoId, postoId, postoNome, intervaloMinutos, tokenSessao, status,
                ultimoCheckinMillis, inicioPrevistoMillis, 0L, null);
    }

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis, long proximoDeadlineMillis) {
        this(turnoId, postoId, postoNome, intervaloMinutos, tokenSessao, status,
                ultimoCheckinMillis, inicioPrevistoMillis, proximoDeadlineMillis, null);
    }

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis, long proximoDeadlineMillis,
                 String tipoProximoDeadline) {
        this.turnoId = turnoId;
        this.postoId = postoId;
        this.postoNome = postoNome;
        this.intervaloMinutos = intervaloMinutos;
        this.tokenSessao = tokenSessao;
        this.status = status;
        this.ultimoCheckinMillis = ultimoCheckinMillis;
        this.inicioPrevistoMillis = inicioPrevistoMillis;
        this.proximoDeadlineMillis = proximoDeadlineMillis;
        this.tipoProximoDeadline = tipoProximoDeadline;
    }

    public String getTurnoId() { return turnoId; }
    public String getPostoId() { return postoId; }
    public String getPostoNome() { return postoNome; }
    public int getIntervaloMinutos() { return intervaloMinutos; }
    public String getTokenSessao() { return tokenSessao; }
    public String getStatus() { return status; }
    public long getUltimoCheckinMillis() { return ultimoCheckinMillis; }
    public long getInicioPrevistoMillis() { return inicioPrevistoMillis; }
    public long getProximoDeadlineMillis() { return proximoDeadlineMillis; }
    public String getTipoProximoDeadline() { return tipoProximoDeadline; }

    public boolean isProximoFinalizar() {
        return "finalizar".equals(tipoProximoDeadline);
    }

    public boolean isProximoCheckin() {
        return tipoProximoDeadline == null || "checkin".equals(tipoProximoDeadline);
    }

    public long getDeadlineMillis() {
        if (proximoDeadlineMillis > 0) return proximoDeadlineMillis;
        return ultimoCheckinMillis + (intervaloMinutos * 60L * 1000L);
    }

    public long getTempoRestanteMillis() {
        return Math.max(0, getDeadlineMillis() - System.currentTimeMillis());
    }
}

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
    private final long fimPrevistoMillis;

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis) {
        this(turnoId, postoId, postoNome, intervaloMinutos, tokenSessao, status,
                ultimoCheckinMillis, inicioPrevistoMillis, 0L, null, 0L);
    }

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis, long proximoDeadlineMillis) {
        this(turnoId, postoId, postoNome, intervaloMinutos, tokenSessao, status,
                ultimoCheckinMillis, inicioPrevistoMillis, proximoDeadlineMillis, null, 0L);
    }

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis, long proximoDeadlineMillis,
                 String tipoProximoDeadline) {
        this(turnoId, postoId, postoNome, intervaloMinutos, tokenSessao, status,
                ultimoCheckinMillis, inicioPrevistoMillis, proximoDeadlineMillis,
                tipoProximoDeadline, 0L);
    }

    public Turno(String turnoId, String postoId, String postoNome, int intervaloMinutos,
                 String tokenSessao, String status, long ultimoCheckinMillis,
                 long inicioPrevistoMillis, long proximoDeadlineMillis,
                 String tipoProximoDeadline, long fimPrevistoMillis) {
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
        this.fimPrevistoMillis = fimPrevistoMillis;
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
    public long getFimPrevistoMillis() { return fimPrevistoMillis; }

    public boolean isProximoFinalizar() {
        if ("finalizar".equals(tipoProximoDeadline)) return true;
        if (fimPrevistoMillis > 0 && intervaloMinutos > 0 && proximoDeadlineMillis > 0) {
            long agora = System.currentTimeMillis();
            if (agora + (intervaloMinutos * 60L * 1000L) >= fimPrevistoMillis) return true;
        }
        return false;
    }

    public boolean isProximoCheckin() {
        return tipoProximoDeadline == null || "checkin".equals(tipoProximoDeadline);
    }

    public long getDeadlineMillis() {
        if (proximoDeadlineMillis > 0) return proximoDeadlineMillis;
        return ultimoCheckinMillis + (intervaloMinutos * 60L * 1000L);
    }

    public long getTempoRestanteMillis() {
        return getDeadlineMillis() - System.currentTimeMillis();
    }
}

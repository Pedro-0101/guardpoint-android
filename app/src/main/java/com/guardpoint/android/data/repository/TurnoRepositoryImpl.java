package com.guardpoint.android.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.guardpoint.android.data.remote.api.GuardPointApi;
import com.guardpoint.android.data.remote.dto.CheckinRequest;
import com.guardpoint.android.data.remote.dto.CheckinResponse;
import com.guardpoint.android.data.remote.dto.FinalizarTurnoRequest;
import com.guardpoint.android.data.remote.dto.IniciarResponse;
import com.guardpoint.android.data.remote.dto.IniciarTurnoRequest;
import com.guardpoint.android.data.remote.dto.VigiaTurnoInfo;
import com.guardpoint.android.data.remote.dto.VigiaTurnoResponse;
import com.guardpoint.android.data.remote.dto.VigiaProximoTurno;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.domain.model.Resource;
import com.guardpoint.android.domain.model.Turno;
import com.guardpoint.android.domain.repository.TurnoRepository;

import timber.log.Timber;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class TurnoRepositoryImpl implements TurnoRepository {

    private final GuardPointApi api;

    @Inject
    public TurnoRepositoryImpl(GuardPointApi api) {
        this.api = api;
    }

    @Override
    public LiveData<Resource<Turno>> getTurnoAtivo() {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        api.getVigiaTurno().enqueue(new Callback<VigiaTurnoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VigiaTurnoResponse> call, @NonNull Response<VigiaTurnoResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Timber.w("getVigiaTurno erro HTTP: code=%d", response.code());
                    result.setValue(Resource.error("Erro ao buscar dados do turno"));
                    return;
                }

                VigiaTurnoResponse body = response.body();

                if (body.isTemTurnoAtivo() && body.getTurno() != null) {
                    VigiaTurnoInfo info = body.getTurno();
                    Timber.i("Turno ativo: status=%s, deadline=%s, tipo=%s, checkins=%d, atrasado=%s",
                            info.getStatus(), info.getProximoDeadline(),
                            info.getTipoProximoDeadline(), info.getCheckinsHoje(), info.isAtrasado());

                    long ultimoCheckinMillis = System.currentTimeMillis();
                    if (info.getUltimoCheckin() != null) {
                        ultimoCheckinMillis = parseIso8601(info.getUltimoCheckin().getTimestampCriacao());
                    }

                    String nomePosto = info.getPostoNome();
                    if (nomePosto == null && info.getPosto() != null) {
                        nomePosto = info.getPosto().getNome();
                    }

                    result.setValue(Resource.success(new Turno(
                            info.getId(),
                            info.getPosto() != null ? info.getPosto().getId() : "",
                            nomePosto,
                            info.getIntervaloMin(),
                            info.getTokenSessao(),
                            info.getStatus(),
                            ultimoCheckinMillis,
                            parseIso8601(info.getInicioPrevisto()),
                            parseIso8601(info.getProximoDeadline()),
                            info.getTipoProximoDeadline()
                    )));
                } else if (!body.isTemTurnoAtivo() && body.getProximoTurno() != null) {
                    VigiaProximoTurno prox = body.getProximoTurno();
                    String nomePosto = prox.getPosto() != null ? prox.getPosto().getNome() : "";
                    Timber.i("Proximo turno: data=%s, inicio=%s, posto=%s", prox.getData(), prox.getHoraInicio(), nomePosto);

                    result.setValue(Resource.success(new Turno(
                            "",
                            prox.getPosto() != null ? prox.getPosto().getId() : "",
                            nomePosto,
                            0,
                            null,
                            "agendado",
                            System.currentTimeMillis(),
                            parseIso8601(prox.getInicioPrevisto())
                    )));
                } else {
                    Timber.i("Nenhum turno ativo ou agendado");
                    result.setValue(Resource.error("Nenhum turno encontrado"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<VigiaTurnoResponse> call, @NonNull Throwable t) {
                Timber.e(t, "getVigiaTurno onFailure");
                result.setValue(Resource.error("Erro de conexao: " + t.getMessage()));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<Turno>> iniciarTurno(String deviceId, String postoId, String senha,
                                                   double latitude, double longitude, int intervaloMinutos) {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        IniciarTurnoRequest request = new IniciarTurnoRequest(deviceId, latitude, longitude, postoId, senha, intervaloMinutos);
        api.iniciarTurno(request).enqueue(new Callback<IniciarResponse>() {
            @Override
            public void onResponse(@NonNull Call<IniciarResponse> call, @NonNull Response<IniciarResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTurno() != null) {
                    IniciarResponse body = response.body();
                    Timber.i("iniciarTurno OK: atrasado=%s, proximoDeadline=%s", body.isAtrasado(), body.getProximoDeadline());
                    long proximoDeadlineMillis = parseIso8601(body.getProximoDeadline());
                    String tipoProximoDeadline = body.getTipoProximoDeadline();
                    result.setValue(Resource.success(
                            mapToDomain(body.getTurno(), System.currentTimeMillis(), proximoDeadlineMillis, tipoProximoDeadline)));
                } else {
                    String errBody = "null";
                    try {
                        if (response.errorBody() != null) errBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Timber.e("iniciarTurno erro HTTP: code=%d, body=%s", response.code(), errBody);
                    result.setValue(Resource.error("Erro ao iniciar turno (HTTP " + response.code() + ")"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<IniciarResponse> call, @NonNull Throwable t) {
                Timber.e(t, "iniciarTurno onFailure");
                result.setValue(Resource.error("Erro de conexao ao iniciar turno: " + t.getMessage()));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<CheckinResponse>> realizarCheckin(String deviceId, String turnoId, String senha,
                                                                double latitude, double longitude) {
        MutableLiveData<Resource<CheckinResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
        CheckinRequest request = new CheckinRequest(deviceId, latitude, longitude, senha, timestamp, turnoId, null);
        api.realizarCheckin(request).enqueue(new Callback<CheckinResponse>() {
            @Override
            public void onResponse(@NonNull Call<CheckinResponse> call, @NonNull Response<CheckinResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckinResponse body = response.body();
                    Timber.i("checkin OK: status=%s, proximoDeadline=%s, tipo=%s, atrasado=%s",
                            body.getStatus(), body.getProximoDeadline(),
                            body.getTipoProximoDeadline(), body.isAtrasado());
                    result.setValue(Resource.success(response.body()));
                } else {
                    String errBody = "null";
                    try {
                        if (response.errorBody() != null) errBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Timber.e("checkin erro HTTP: code=%d, body=%s", response.code(), errBody);
                    result.setValue(Resource.error("Erro ao enviar check-in (HTTP " + response.code() + ")"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckinResponse> call, @NonNull Throwable t) {
                Timber.e(t, "checkin onFailure");
                result.setValue(Resource.error("Erro de conexao ao enviar check-in: " + t.getMessage()));
            }
        });

        return result;
    }

    @Override
    public LiveData<Resource<Turno>> finalizarTurno(String deviceId, String turnoId, String senha,
                                                     double latitude, double longitude) {
        MutableLiveData<Resource<Turno>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(new Date());
        FinalizarTurnoRequest request = new FinalizarTurnoRequest(deviceId, latitude, longitude, senha, timestamp, turnoId);
        api.finalizarTurno(request).enqueue(new Callback<TurnoResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoResponse> call, @NonNull Response<TurnoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Timber.i("finalizarTurno OK: status=%s", response.body().getStatus());
                    result.setValue(Resource.success(mapToDomain(response.body())));
                } else {
                    String errBody = "null";
                    try {
                        if (response.errorBody() != null) errBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Timber.e("finalizarTurno erro HTTP: code=%d, body=%s", response.code(), errBody);
                    result.setValue(Resource.error("Erro ao finalizar turno (HTTP " + response.code() + ")"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoResponse> call, @NonNull Throwable t) {
                Timber.e(t, "finalizarTurno onFailure");
                result.setValue(Resource.error("Erro de conexao ao finalizar turno: " + t.getMessage()));
            }
        });

        return result;
    }

    private Turno mapToDomain(TurnoResponse response) {
        return new Turno(
                response.getTurnoId(),
                response.getPostoId(),
                response.getPostoNome(),
                response.getIntervaloMinutos(),
                response.getTokenSessao(),
                response.getStatus(),
                System.currentTimeMillis(),
                parseIso8601(response.getInicioPrevisto())
        );
    }

    private Turno mapToDomain(TurnoResponse response, long ultimoCheckinMillis,
                              long proximoDeadlineMillis, String tipoProximoDeadline) {
        return new Turno(
                response.getTurnoId(),
                response.getPostoId(),
                response.getPostoNome(),
                response.getIntervaloMinutos(),
                response.getTokenSessao(),
                response.getStatus(),
                ultimoCheckinMillis,
                parseIso8601(response.getInicioPrevisto()),
                proximoDeadlineMillis,
                tipoProximoDeadline
        );
    }

    private long parseIso8601(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return 0L;
        try {
            if (dateStr.endsWith("Z") || dateStr.endsWith("z")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(dateStr.substring(0, dateStr.length() - 1));
                return date != null ? date.getTime() : 0L;
            }
            if (dateStr.length() > 19 && (dateStr.contains("+") || dateStr.contains("-"))) {
                int tzIndex = Math.max(dateStr.lastIndexOf('+'), dateStr.lastIndexOf('-'));
                if (tzIndex >= 19) {
                    String datePart = dateStr.substring(0, tzIndex);
                    String tzPart = dateStr.substring(tzIndex);
                    int dotIndex = datePart.indexOf('.');
                    if (dotIndex > 0) {
                        datePart = datePart.substring(0, dotIndex);
                    }
                    long offsetMillis = parseTimezoneOffset(tzPart);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = sdf.parse(datePart);
                    return date != null ? date.getTime() - offsetMillis : 0L;
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : 0L;
        } catch (ParseException e) {
            return 0L;
        }
    }

    private long parseTimezoneOffset(String tzPart) {
        try {
            String normalized = tzPart.replace(":", "");
            boolean negative = normalized.startsWith("-");
            String digits = normalized.substring(1);
            int hours = Integer.parseInt(digits.substring(0, Math.min(2, digits.length())));
            int minutes = 0;
            if (digits.length() > 2) {
                minutes = Integer.parseInt(digits.substring(2, Math.min(4, digits.length())));
            }
            long totalMinutes = hours * 60L + minutes;
            return negative ? -totalMinutes * 60L * 1000L : totalMinutes * 60L * 1000L;
        } catch (Exception e) {
            return 0L;
        }
    }
}

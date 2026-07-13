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
import com.guardpoint.android.data.remote.dto.TurnoListResponse;
import com.guardpoint.android.data.remote.dto.TurnoResponse;
import com.guardpoint.android.data.remote.dto.TurnoStatusResponse;
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

        api.getStatusTurno().enqueue(new Callback<TurnoStatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoStatusResponse> call, @NonNull Response<TurnoStatusResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getTurno() != null) {
                    TurnoStatusResponse body = response.body();
                    TurnoResponse turnoResponse = body.getTurno();
                    Timber.i("getStatusTurno OK: status=%s, proximoDeadline=%s, tipo=%s, checkinsHoje=%d",
                            turnoResponse.getStatus(), body.getProximoDeadline(),
                            body.getTipoProximoDeadline(), body.getCheckinsHoje());
                    long ultimoCheckinMillis = System.currentTimeMillis();
                    if (body.getUltimoCheckin() != null) {
                        ultimoCheckinMillis = parseIso8601(body.getUltimoCheckin().getTimestampCriacao());
                    }
                    long proximoDeadlineMillis = parseIso8601(body.getProximoDeadline());
                    String tipoProximoDeadline = body.getTipoProximoDeadline();

                    if (turnoResponse.getPostoNome() == null) {
                        buscarTurnoCompleto(result, turnoResponse, ultimoCheckinMillis,
                                proximoDeadlineMillis, tipoProximoDeadline);
                    } else {
                        result.setValue(Resource.success(
                                mapToDomain(turnoResponse, ultimoCheckinMillis, proximoDeadlineMillis, tipoProximoDeadline)));
                    }
                } else {
                    Timber.w("getStatusTurno sem turno ativo: code=%d", response.code());
                    buscarProximoTurno(result);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoStatusResponse> call, @NonNull Throwable t) {
                Timber.e(t, "getStatusTurno onFailure");
                buscarProximoTurno(result);
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
                    Timber.i("iniciarTurno API OK: atrasado=%s, proximoDeadline=%s, tipo=%s",
                            body.isAtrasado(), body.getProximoDeadline(), body.getTipoProximoDeadline());
                    long proximoDeadlineMillis = parseIso8601(body.getProximoDeadline());
                    String tipoProximoDeadline = body.getTipoProximoDeadline();
                    result.setValue(Resource.success(
                            mapToDomain(body.getTurno(), System.currentTimeMillis(), proximoDeadlineMillis, tipoProximoDeadline)));
                } else {
                    String errBody = "null";
                    try {
                        if (response.errorBody() != null) errBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Timber.e("iniciarTurno API erro HTTP: code=%d, errorBody=%s", response.code(), errBody);
                    result.setValue(Resource.error("Erro ao iniciar turno (HTTP " + response.code() + ")"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<IniciarResponse> call, @NonNull Throwable t) {
                Timber.e(t, "iniciarTurno onFailure");
                result.setValue(Resource.error("Erro de conexão ao iniciar turno: " + t.getMessage()));
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
                    Timber.i("realizarCheckin API OK: status=%s, proximoDeadline=%s, tipo=%s, atrasado=%s",
                            body.getStatus(), body.getProximoDeadline(),
                            body.getTipoProximoDeadline(), body.isAtrasado());
                    result.setValue(Resource.success(response.body()));
                } else {
                    String errBody = "null";
                    try {
                        if (response.errorBody() != null) errBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Timber.e("realizarCheckin API erro HTTP: code=%d, errorBody=%s", response.code(), errBody);
                    result.setValue(Resource.error("Erro ao enviar check-in (HTTP " + response.code() + ")"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CheckinResponse> call, @NonNull Throwable t) {
                Timber.e(t, "realizarCheckin onFailure");
                result.setValue(Resource.error("Erro de conexão ao enviar check-in: " + t.getMessage()));
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
                    Timber.i("finalizarTurno API OK: status=%s", response.body().getStatus());
                    result.setValue(Resource.success(mapToDomain(response.body())));
                } else {
                    String errBody = "null";
                    try {
                        if (response.errorBody() != null) errBody = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Timber.e("finalizarTurno API erro HTTP: code=%d, errorBody=%s", response.code(), errBody);
                    result.setValue(Resource.error("Erro ao finalizar turno (HTTP " + response.code() + ")"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoResponse> call, @NonNull Throwable t) {
                Timber.e(t, "finalizarTurno onFailure");
                result.setValue(Resource.error("Erro de conexão ao finalizar turno: " + t.getMessage()));
            }
        });

        return result;
    }

    private void buscarTurnoCompleto(MutableLiveData<Resource<Turno>> result,
                                      TurnoResponse turnoResponse, long ultimoCheckinMillis,
                                      long proximoDeadlineMillis, String tipoProximoDeadline) {
        api.getTurnos("em_andamento").enqueue(new Callback<TurnoListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoListResponse> call, @NonNull Response<TurnoListResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    TurnoResponse completo = response.body().getData().get(0);
                    TurnoResponse merged = new TurnoResponse();
                    merged.setTurnoId(turnoResponse.getTurnoId());
                    merged.setPostoId(turnoResponse.getPostoId());
                    merged.setPostoNome(completo.getPostoNome());
                    merged.setIntervaloMinutos(turnoResponse.getIntervaloMinutos());
                    merged.setTokenSessao(turnoResponse.getTokenSessao());
                    merged.setStatus(turnoResponse.getStatus());
                    merged.setInicioPrevisto(turnoResponse.getInicioPrevisto());
                    result.setValue(Resource.success(
                            mapToDomain(merged, ultimoCheckinMillis, proximoDeadlineMillis, tipoProximoDeadline)));
                } else {
                    result.setValue(Resource.success(
                            mapToDomain(turnoResponse, ultimoCheckinMillis, proximoDeadlineMillis, tipoProximoDeadline)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoListResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.success(
                        mapToDomain(turnoResponse, ultimoCheckinMillis, proximoDeadlineMillis, tipoProximoDeadline)));
            }
        });
    }

    private void buscarProximoTurno(MutableLiveData<Resource<Turno>> result) {
        api.getTurnos("agendado").enqueue(new Callback<TurnoListResponse>() {
            @Override
            public void onResponse(@NonNull Call<TurnoListResponse> call, @NonNull Response<TurnoListResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    result.setValue(Resource.success(mapToDomain(response.body().getData().get(0))));
                } else {
                    result.setValue(Resource.error("Nenhum turno agendado"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TurnoListResponse> call, @NonNull Throwable t) {
                result.setValue(Resource.error("Erro ao buscar turnos"));
            }
        });
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

    private Turno mapToDomain(TurnoResponse response, long ultimoCheckinMillis, long proximoDeadlineMillis) {
        return mapToDomain(response, ultimoCheckinMillis, proximoDeadlineMillis, null);
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
                if (tzIndex > 19) {
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

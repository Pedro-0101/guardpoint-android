# Prompt de Correção — guardpoint-android — Fase 5 (Foreground Service e GPS)

> **Para o executor:** você trabalha SOMENTE no repositório `guardpoint-android`. Não altere `guardpoint-server` nem `guardpoint-manager`. Você não tem o contexto da conversa que gerou este prompt — tudo que precisa está aqui.

## Contexto

O `guardpoint-android` é o app nativo Java dos vigias. A **Fase 5** (ver `PLANNING.md` §9) entrega o `GuardPointForegroundService`: coleta contínua de GPS via `FusedLocationProviderClient`, notificação persistente e o timer da janela deslizante alimentando a UI via `LiveData` (`ServiceStateManager`).

O código da fase existe, mas **o Foreground Service crasha assim que é iniciado**, tornando toda a Fase 5 inutilizável.

## Causa raiz (P0 — bloqueante)

O banco Room é construído **sem** `allowMainThreadQueries()`:

`app/src/main/java/com/guardpoint/android/di/DatabaseModule.java`
```java
return Room.databaseBuilder(
        context.getApplicationContext(),
        AppDatabase.class,
        "guardpoint.db"
).fallbackToDestructiveMigration()
.build();
```

O DAO expõe uma leitura **síncrona** (não `LiveData`):

`app/src/main/java/com/guardpoint/android/data/local/db/dao/TurnoDao.java`
```java
@Query("SELECT * FROM turno_ativo LIMIT 1")
TurnoAtivo getTurnoAtivo();
```

E o service a invoca na **main thread**, dentro de `onStartCommand`, **antes** de `startForeground`:

`app/src/main/java/com/guardpoint/android/service/GuardPointForegroundService.java`
```java
@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    carregarTurnoAtivo();               // <-- turnoDao.getTurnoAtivo() na main thread
    String tempoInicial = turnoAtivo != null ? ... : "--:--";
    Notification notification = notificationHelper.buildForegroundNotification(tempoInicial);
    startForeground(Constants.NOTIFICATION_ID_SERVICE, notification);
    ...
}

private void carregarTurnoAtivo() {
    turnoAtivo = turnoDao.getTurnoAtivo();
}
```

Callbacks de `Service` rodam na main thread. Como o banco NÃO permite queries na main thread, `getTurnoAtivo()` lança `IllegalStateException: Cannot access database on the main thread...`. Pior: a exceção ocorre **antes** de `startForeground`, então além do crash o sistema também reporta "did not call startForeground()" (ANR/kill). O mesmo problema existe no `BootReceiver`:

`app/src/main/java/com/guardpoint/android/receiver/BootReceiver.java`
```java
if (turnoDao.getTurnoAtivo() != null) {   // main thread do BroadcastReceiver
    Intent serviceIntent = new Intent(context, GuardPointForegroundService.class);
    ContextCompat.startForegroundService(context, serviceIntent);
}
```

## Correção exigida

**Objetivo:** tirar toda leitura síncrona do Room da main thread, garantindo que `startForeground()` seja chamado **em ≤ 5 segundos e sem depender do banco**.

Abordagem recomendada (mínima e segura):

1. **`GuardPointForegroundService.onStartCommand`:** chame `startForeground()` IMEDIATAMENTE com um texto placeholder (`"--:--"`), **antes** de qualquer acesso ao banco. Só então carregue o `turnoAtivo` em background (ex.: um `Executor`/`HandlerThread` de disco) e, ao concluir, atualize a notificação e inicie o timer. Nunca chame `turnoDao.getTurnoAtivo()` na main thread.

   Esboço:
   ```java
   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
       // 1. Sobe o foreground SEM tocar no banco (evita ANR e crash)
       Notification placeholder = notificationHelper.buildForegroundNotification("--:--");
       startForeground(Constants.NOTIFICATION_ID_SERVICE, placeholder);
       serviceStateManager.setServiceRunning(true);
       startLocationUpdates();

       // 2. Carrega o turno em background e só então inicia o timer
       ioExecutor.execute(() -> {
           TurnoAtivo t = turnoDao.getTurnoAtivo();
           mainHandler.post(() -> {
               this.turnoAtivo = t;
               iniciarTimer();
           });
       });
       return START_STICKY;
   }
   ```
   Use um `java.util.concurrent.ExecutorService` (single thread) criado no `onCreate` e encerrado no `onDestroy`, ou reaproveite um executor já injetado por Hilt se existir no projeto (procure por `@Inject ... Executor` / `AppExecutors` antes de criar um novo — **siga o padrão existente**).

2. **`BootReceiver`:** mova o `getTurnoAtivo()` para fora da main thread usando `goAsync()` + um executor, ou delegue a decisão de "existe turno ativo?" para dentro do próprio service (que já carrega em background). Não bloqueie `onReceive`.

3. **Alternativa aceitável, se o time preferir:** adicionar `.allowMainThreadQueries()` ao builder é a solução de MENOR esforço e destrava o crash, **mas** viola a convenção do próprio `PLANNING.md` §11 ("todas as chamadas de banco em background threads") e pode causar jank. Só use se o revisor aprovar explicitamente. A abordagem 1+2 é a correta.

## Escopo

- **Em escopo:** `GuardPointForegroundService.java`, `BootReceiver.java`, e (apenas se optar pela alternativa) `DatabaseModule.java`. Introduzir um executor de I/O se necessário.
- **Fora de escopo:** não altere DTOs de rede, não mexa no fluxo de check-in/finalização (`CheckinActivity`, `HomeActivity`), não altere migrations, não toque na lógica de sabotagem/offline (outras fases).

## Verificação

1. `cd guardpoint-android && ./gradlew assembleDebug` — deve compilar sem erros.
2. `./gradlew lint` — sem novos erros introduzidos.
3. **Teste manual obrigatório** (o bug é de runtime, não de compilação):
   - Inicie um turno no app. **Resultado esperado:** a notificação persistente "GuardPoint ativo" aparece e o timer regressivo conta; o app NÃO crasha. Confirme no Logcat a ausência de `IllegalStateException: Cannot access database on the main thread` e de `Context.startForegroundService() did not then call Service.startForeground()`.
   - Faça um check-in e volte à Home. **Esperado:** o timer reinicia (janela deslizante). Se após o check-in o timer NÃO reiniciar, veja "Observação" abaixo e **pare para reportar** — pode exigir ajuste adicional.
   - Reinicie o dispositivo com turno ativo. **Esperado:** o `BootReceiver` sobe o service sem crash.

## Observação para o revisor (não corrigir agora, só registrar se aparecer)

O reset da janela após check-in depende de uma cadeia frágil: `checkinLauncher` → `viewModel.carregarTurnoAtivo()` → observer de `getTurnoAtivo()` → `ensureServiceRunning()` → novo `startForegroundService()` → `onStartCommand` recarrega `turnoAtivo`. Se o `LiveData<Boolean>` de turno ativo não re-emitir (valor `true`→`true`), `ensureServiceRunning()` pode não disparar e o timer do service não reinicia. Se o teste manual mostrar esse sintoma, **reporte** — a correção seria fazer o service recarregar o `turnoAtivo` explicitamente ao receber um novo `startForegroundService` (o que a correção acima já garante, desde que o service seja re-startado) OU expor um comando explícito de "reset" via Intent action. Não implemente isso especulativamente.

## Escape hatch

Se ao abrir os arquivos você descobrir que o `DatabaseModule` **já** contém `allowMainThreadQueries()` (ou seja, o crash descrito não se reproduz), **pare e reporte** — o diagnóstico deste prompt estaria desatualizado e o problema real é outro.

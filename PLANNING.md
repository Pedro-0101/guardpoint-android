# guardpoint-android — Plano de Desenvolvimento (App Android Nativo)

## 1. Visão Geral do Módulo

O `guardpoint-android` é o aplicativo nativo Android utilizado pelos vigias em campo. Sua função principal é realizar check-ins de reafirmação de vida (Dead Man's Switch) com senha e coordenadas GPS, operando de forma resiliente mesmo em áreas sem cobertura de rede. O app executa coleta contínua de localização via Foreground Service e gerencia localmente o cronômetro da janela de tempo para notificações e alertas offline.

## 2. Stack Tecnológica

| Componente              | Tecnologia                                |
| ----------------------- | ----------------------------------------- |
| Linguagem               | Java 17                                   |
| Min SDK                 | API 26 (Android 8.0 Oreo)                 |
| Target SDK              | API 34 (Android 14)                       |
| Arquitetura             | MVVM (Model-View-ViewModel)               |
| Injeção de Dependência  | Hilt / Dagger                             |
| Rede                    | Retrofit 2 + OkHttp + Gson                |
| Banco Local             | Room (SQLite)                             |
| Segundo Plano           | Foreground Service + WorkManager + AlarmManager |
| Biometria               | BiometricPrompt API (AndroidX)            |
| Localização             | FusedLocationProviderClient (Google Play) |
| Permissões              | Battery Optimization, GPS, Notificações   |
| Build                   | Gradle (Kotlin DSL opcional)              |
| CI/CD                   | GitHub Actions → APK/AAB build            |

## 3. Estrutura de Diretórios

```
guardpoint-android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/guardpoint/android/
│   │   │   │   ├── GuardPointApp.java              # Application class (Hilt)
│   │   │   │   ├── di/                              # Módulos Hilt
│   │   │   │   │   ├── AppModule.java
│   │   │   │   │   ├── NetworkModule.java
│   │   │   │   │   └── DatabaseModule.java
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── db/
│   │   │   │   │   │   │   ├── AppDatabase.java
│   │   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   │   ├── CheckinDao.java
│   │   │   │   │   │   │   │   ├── TurnoDao.java
│   │   │   │   │   │   │   │   └── ConfigDao.java
│   │   │   │   │   │   │   └── entity/
│   │   │   │   │   │   │       ├── CheckinPendente.java
│   │   │   │   │   │   │       ├── TurnoAtivo.java
│   │   │   │   │   │   │       └── ConfigLocal.java
│   │   │   │   │   │   └── prefs/
│   │   │   │   │   │       └── SecurePrefs.java     # EncryptedSharedPreferences
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   │   ├── GuardPointApi.java   # Retrofit interface
│   │   │   │   │   │   │   └── AuthInterceptor.java # JWT interceptor
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   │   ├── LoginResponse.java
│   │   │   │   │   │   │   ├── CheckinRequest.java
│   │   │   │   │   │   │   ├── CheckinResponse.java
│   │   │   │   │   │   │   ├── TurnoStatusResponse.java
│   │   │   │   │   │   │   └── LoteCheckinRequest.java
│   │   │   │   │   │   └── RetrofitClient.java
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── AuthRepository.java
│   │   │   │   │       ├── CheckinRepository.java
│   │   │   │   │       ├── TurnoRepository.java
│   │   │   │   │       └── SyncRepository.java
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Checkin.java
│   │   │   │   │   │   ├── Turno.java
│   │   │   │   │   │   ├── Posto.java
│   │   │   │   │   │   └── AlertaLocal.java
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── IniciarTurnoUseCase.java
│   │   │   │   │       ├── RealizarCheckinUseCase.java
│   │   │   │   │       ├── FinalizarTurnoUseCase.java
│   │   │   │   │       ├── SincronizarPendentesUseCase.java
│   │   │   │   │       └── VerificarJanelaUseCase.java
│   │   │   │   ├── ui/
│   │   │   │   │   ├── login/
│   │   │   │   │   │   ├── LoginActivity.java
│   │   │   │   │   │   └── LoginViewModel.java
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeActivity.java
│   │   │   │   │   │   ├── HomeFragment.java
│   │   │   │   │   │   └── HomeViewModel.java
│   │   │   │   │   ├── checkin/
│   │   │   │   │   │   ├── CheckinActivity.java
│   │   │   │   │   │   ├── CheckinFragment.java
│   │   │   │   │   │   └── CheckinViewModel.java
│   │   │   │   │   ├── alerta/
│   │   │   │   │   │   ├── AlertaDialogFragment.java
│   │   │   │   │   │   └── BloqueioActivity.java    # Tela de bloqueio por sabotagem
│   │   │   │   │   └── comum/
│   │   │   │   │       ├── BaseActivity.java
│   │   │   │   │       └── BaseViewModel.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── GuardPointForegroundService.java  # GPS + Timer
│   │   │   │   │   ├── LocationProvider.java             # FusedLocation wrapper
│   │   │   │   │   ├── SyncWorker.java                   # WorkManager
│   │   │   │   │   └── CheckinAlarmReceiver.java         # BroadcastReceiver
│   │   │   │   └── util/
│   │   │   │       ├── ConnectivityUtil.java
│   │   │   │       ├── NetworkMonitor.java
│   │   │   │       ├── NotificationHelper.java           # Canais e notificações
│   │   │   │       └── Constants.java
│   │   │   ├── res/
│   │   │   │   ├── drawable/          # Ícones do app
│   │   │   │   ├── layout/            # Layouts XML
│   │   │   │   ├── values/            # strings.xml, colors.xml, themes.xml
│   │   │   │   └── values-pt/         # Português
│   │   │   └── AndroidManifest.xml
│   │   └── test/                       # Testes unitários (JUnit + Mockito)
│   └── build.gradle
├── gradle/
├── build.gradle                       # Project-level
├── gradle.properties
└── settings.gradle
```

## 4. Fluxos de Tela

### 4.1. Fluxo de Login
```
[Login] → BiometricPrompt → API /auth/login → API /auth/biometric → [Home]
     ↓ (falha biométrica)
  [Bloqueio: "Autenticação biométrica obrigatória"]
```

### 4.2. Fluxo de Turno
```
[Home: "Iniciar Turno"] → API /turnos/iniciar → Foreground Service inicia
     ↓
[Home: Timer regressivo + "Inserir Senha" + Mapa com localização]
     ↓
[Checkin: Digitar senha] → API /turnos/checkin (ou fila offline) → Timer reinicia
     ↓ (fim do turno)
[Checkin: "Finalizar Turno"] → API /turnos/finalizar → stopSelf() → [Login]
```

### 4.3. Fluxo de Alerta Local (Offline / Atraso)
```
[5 min restantes + sem rede] → Notificação: "Desloque-se para área com sinal"
[Timer zerado + sem rede] → Salva check-in no Room → Repetição intermitente
[Rede retorna] → WorkManager sincroniza lote → Atualiza UI
```

### 4.4. Fluxo de Sabotagem
```
[GPS desligado] → BroadcastReceiver detecta → BloqueioActivity + API /turnos/sabotagem
[Permissão revogada] → BloqueioActivity + Tentativa de envio de status "Sabotagem"
```

## 5. Entidades do Room (Banco Local)

### 5.1. CheckinPendente
```java
@Entity(tableName = "checkins_pendentes")
public class CheckinPendente {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String turnoId;

    public double latitude;
    public double longitude;

    @NonNull
    public String timestampCriacao;    // ISO 8601 — hora do celular

    @NonNull
    public String tipoSenha;           // 'padrao', 'coacao', 'finalizacao'

    public int tentativasEnvio;        // contador de retry
}
```

### 5.2. TurnoAtivo
```java
@Entity(tableName = "turno_ativo")
public class TurnoAtivo {
    @PrimaryKey
    @NonNull
    public String turnoId;

    public String postoId;
    public String postoNome;
    public int intervaloMinutos;
    public long ultimoCheckinMillis;     // System.currentTimeMillis()
    public String tokenSessao;
    public String status;                // 'em_andamento', 'critico'
}
```

### 5.3. ConfigLocal
```java
@Entity(tableName = "config_local")
public class ConfigLocal {
    @PrimaryKey
    public int id;                       // sempre 1 (single row)

    public String jwtToken;
    public String refreshToken;
    public String usuarioId;
    public String empresaId;
}
```

## 6. Foreground Service: Comunicação e Ciclo de Vida

### 6.1. Responsabilidades
- **Coleta de GPS**: `FusedLocationProviderClient` com `requestLocationUpdates` a cada 30 segundos (configurável).
- **Timer Local**: conta regressiva baseada em `ultimoCheckinMillis + (intervaloMinutos * 60 * 1000)`.
- **Notificação Persistente**: exibe "GuardPoint ativo — Seu turno está em andamento" com botão "Abrir App".
- **Verificação de Rede**: `ConnectivityManager.registerDefaultNetworkCallback` para detectar mudanças de conectividade.

### 6.2. Estados do Service
```
CREATED → STARTED → [executando GPS + Timer]
                         ↓ (check-in finalização)
                      STOPPED (stopSelf)
```

### 6.3. Permissões Obrigatórias (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## 7. WorkManager: Sincronização Offline e Alarmes

### 7.1. SyncWorker
- **Trigger**: `NetworkType.CONNECTED` constraint.
- **Tarefa**: consulta `checkins_pendentes` ordenados por `timestampCriacao ASC`, envia via `POST /api/checkins/lote`.
- **Repetição**: `BackoffPolicy.EXPONENTIAL` com retry se falhar.
- **Limpeza**: remove registros confirmados do Room após sincronização bem-sucedida.

### 7.2. CheckinAlarmReceiver (AlarmManager)
- Agenda alarme para 5 minutos antes do deadline do check-in.
- Se o check-in ocorrer antes, cancela o alarme e reagenda para o próximo deadline.
- Ao disparar, verifica conectividade e emite notificação local de alta prioridade.

## 8. Regras de Negócio (Lado Cliente)

### 8.1. Janela Deslizante Local
- O `Foreground Service` mantém `ultimoCheckinMillis` no `TurnoAtivo` (Room).
- O timer regressivo exibido na UI é calculado como: `(ultimoCheckinMillis + intervaloMinutos * 60 * 1000) - System.currentTimeMillis()`.
- A cada novo check-in (online ou salvo offline), `ultimoCheckinMillis` é atualizado e o timer reinicia.

### 8.2. Senha de Coação (Emergência Silenciosa)
- A UI de check-in tem dois campos: **Senha Normal** e **Senha de Coação**.
- Se o vigia digita no campo de coação e confirma:
  1. A tela mostra "Check-in realizado com sucesso" (resposta normal, sem pânico).
  2. A requisição é enviada com `tipo_senha = 'coacao'` para `/api/turnos/checkin`.
  3. O timer local **continua normalmente** (sem indicativo visual de emergência).
- Este comportamento é crítico para a segurança do vigia em situação de rendição.

### 8.3. Biometria Obrigatória Antes do Check-in
- Antes de exibir a tela de digitação de senha, o app chama `BiometricPrompt`.
- Se o dispositivo não tem biometria, usa PIN/padrão do sistema (fallback `DEVICE_CREDENTIAL`).
- Se falhar, o app não permite prosseguir para a tela de senha (impede que terceiros usem o aparelho do vigia).

### 8.4. Monitoramento de Sabotagem
- `BroadcastReceiver` registrado para `android.location.PROVIDERS_CHANGED` e `android.location.MODE_CHANGED`.
- Se GPS é desligado ou permissões de localização são revogadas durante turno ativo:
  1. `BloqueioActivity` é iniciada (tela cheia, não fechável, overlay).
  2. Tenta enviar `POST /api/turnos/sabotagem`.
  3. Exibe mensagem: "GPS desativado. O turno será reportado como violação."

### 8.5. Finalização Absoluta do Turno
- Ao enviar check-in `tipo_senha = 'finalizacao'` e receber resposta de sucesso:
  1. `GuardPointForegroundService.stopSelf()` é chamado.
  2. `FusedLocationProviderClient.removeLocationUpdates()` é chamado.
  3. `token_sessao` é limpo do `ConfigLocal`.
  4. `WorkManager.cancelAllWorkByTag("sync")` é chamado.
  5. O app retorna à `LoginActivity`, removendo toda a back stack.

## 9. Fases de Desenvolvimento

### Fase 1 — Fundação
- [x] Scaffold do projeto Android (Gradle, estrutura de diretórios)
- [x] Configurar Hilt (DI)
- [x] Configurar Retrofit + OkHttp + AuthInterceptor
- [x] Configurar Room (Database, DAOs, Entidades)
- [x] Tema e style base (Material Design 3)

### Fase 2 — Autenticação
- [x] LoginActivity + LoginViewModel
- [x] Integração com BiometricPrompt
- [x] Chamada à API `/auth/login` e `/auth/biometric`
- [x] Armazenamento seguro do JWT (EncryptedSharedPreferences)
- [x] Auto-refresh de token (AuthInterceptor)

### Fase 3 — Home e Status do Turno
- [ ] HomeActivity / HomeFragment
- [ ] Botão "Iniciar Turno" + chamada `/turnos/iniciar`
- [ ] Exibição de dados do posto e timer regressivo
- [ ] Exibição de mapa simplificado (Google Maps / OSMDroid)

### Fase 4 — Check-in
- [x] CheckinActivity / CheckinFragment
- [x] BiometricPrompt antes da tela de senha
- [x] Campo de senha normal + campo de senha de coação
- [x] Chamada à API `/turnos/checkin`
- [x] Feedback visual de sucesso
- [x] Atualização do timer local e Room

### Fase 5 — Foreground Service e GPS
- [ ] GuardPointForegroundService (Lifecycle)
- [ ] Notificação persistente com canal dedicado
- [ ] FusedLocationProviderClient (coleta contínua)
- [ ] Timer local com atualização do ViewModel via LiveData/Flow
- [ ] Início automático do service ao iniciar turno
- [ ] Parada absoluta ao finalizar turno

### Fase 6 — Offline e Resiliência
- [x] CheckinPendente: salvar no Room quando sem rede
- [x] SyncWorker (WorkManager) para envio de lote
- [x] Lógica de retry com backoff exponencial
- [x] NetworkMonitor (ConnectivityManager callback)
- [x] Fila offline-first: não apaga em 401/403, preserva itens críticos (coação/finalização)
- [x] Idempotência: UUID clienteCheckinId gerado na criação e enviado ao server
- [x] Migration Room v2→v3: adiciona clienteCheckinId + status

### Fase 7 — Alarmes Locais e Notificações
- [x] CheckinAlarmReceiver (AlarmManager)
- [x] Alarme 5 minutos antes do deadline
- [x] Verificação de conectividade no alarme
- [x] Notificação de alta prioridade para deslocamento

### Fase 8 — Sabotagem e Segurança
- [ ] BroadcastReceiver para mudanças de GPS/Permissões
- [ ] BloqueioActivity (tela impeditiva)
- [ ] Chamada `/turnos/sabotagem`
- [ ] Solicitação REQUEST_IGNORE_BATTERY_OPTIMIZATIONS

### Fase 9 — Testes e Robustez
- [ ] Testes unitários (JUnit + Mockito) nos ViewModels e UseCases
- [ ] Testes de integração no Room
- [ ] Testes manuais em diferentes versões Android (8, 10, 12, 14)
- [ ] Teste de comportamento com bateria otimizada (Doze mode)
- [ ] Teste de kill do app pelo SO (reinício do service?)

### Fase 10 — Build e Distribuição
- [ ] Assinatura de release (keystore)
- [ ] Build de APK/AAB via GitHub Actions
- [ ] Firebase App Distribution ou Google Play Internal Testing

## 10. Requisitos Não-Funcionais

| Requisito                   | Meta                                       |
| --------------------------- | ------------------------------------------ |
| Consumo de bateria          | < 5% por hora com GPS ativo                |
| Uso de memória              | < 150MB em foreground                      |
| Tamanho do APK              | < 20MB                                     |
| Tempo de inicialização      | < 2 segundos (cold start)                  |
| Resiliência offline         | Fila local ilimitada (gerenciada por retenção de 7 dias) |
| Compatibilidade             | API 26 a 34                                |
| Crash-free rate             | > 99.5%                                    |

## 11. Convenções de Código

- **Idioma**: nomes de classes, métodos, variáveis em inglês; strings de UI em português (`values-pt/strings.xml`).
- **Arquitetura**: MVVM com Repository pattern; ViewModels não conhecem Android framework classes.
- **Threading**: todas as chamadas de rede e banco em background threads (Coroutines ou RxJava via Retrofit/Room).
- **Lifecycle**: ViewModels usam `LiveData` ou `StateFlow`; Activities/Fragments apenas observam.
- **Permissões**: seguir o fluxo AndroidX `ActivityResultContracts.RequestPermission` para runtime permissions.
- **Logging**: `Timber` para logs de debug; desabilitado em release builds.

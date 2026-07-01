# AGENTS.md — guardpoint-android (Java / Android Nativo)

Instruções e boas práticas para agentes de IA e desenvolvedores trabalhando no app Android do GuardPoint.

---

## 1. Extensões e Ferramentas Recomendadas (Android Studio)

| Extensão / Plugin | Descrição |
|---|---|
| `Android Gradle Plugin (AGP)` | Build system padrão |
| `Hilt` | Injeção de dependência (via KSP/KAPT) |
| `Room` | Annotations + KSP para banco local |
| `LeakCanary` | Detecção de memory leaks (debug only) |
| `Timber` | Logging estruturado |
| `Chucker` | Debug de requisições HTTP (debug only) |
| `Stetho` | Debug de banco local (debug only) |
| IDE: `Android Drawable Preview` | Preview de drawables |
| IDE: `JSON To Kotlin Class` | Geração de DTOs |

---

## 2. Configuração do Ambiente

- **JDK**: 17 (LTS)
- **Android Studio**: Hedgehog (2023.1.1) ou superior
- **Gradle**: 8.x com Kotlin DSL (se optar)
- **AGP**: 8.x (compatível com Gradle 8.x)
- **compileSdk**: 34
- **targetSdk**: 34
- **minSdk**: 26 (Android 8.0)

---

## 3. Estrutura do Projeto (MVVM + Clean Architecture)

```
app/src/main/java/com/guardpoint/android/
├── di/                        # Módulos Hilt
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── DatabaseModule.kt
├── data/
│   ├── local/
│   │   ├── db/AppDatabase.kt
│   │   ├── dao/               # Interfaces Room DAO
│   │   ├── entity/            # Entidades Room
│   │   └── prefs/             # SharedPreferences seguras
│   ├── remote/
│   │   ├── api/GuardPointApi.kt  # Interface Retrofit
│   │   ├── dto/               # Data Transfer Objects
│   │   └── interceptor/       # Auth, Logging interceptors
│   └── repository/            # Implementações de repositórios
├── domain/
│   ├── model/                 # Modelos de domínio puros
│   ├── repository/            # Interfaces de repositório
│   └── usecase/               # Casos de uso (classe por ação)
├── ui/
│   ├── login/
│   ├── home/
│   ├── checkin/
│   ├── alerta/
│   └── comum/                 # Base classes, componentes reutilizáveis
├── service/                   # Foreground Service, WorkManager
├── receiver/                  # BroadcastReceivers
└── util/                      # Extensions, helpers, constants
```

---

## 4. Convenções de Código Java (Android)

### 4.1. Nomenclatura
- **Classes**: PascalCase (`LoginViewModel.java`, `CheckinRepository.java`)
- **Métodos e variáveis**: camelCase (`realizarCheckin()`, `ultimoCheckinMillis`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`, `DEFAULT_INTERVAL_MINUTES`)
- **Resources IDs**: snake_case (`btn_iniciar_turno`, `tv_timer_contagem`)
- **Layout files**: snake_case prefixado pelo tipo (`activity_login.xml`, `fragment_home.xml`)
- **Drawables**: snake_case (`ic_notification.xml`, `bg_button_primary.xml`)

### 4.2. Arquitetura MVVM
- **View (Activity/Fragment)**: Apenas observa o ViewModel e renderiza UI. Zero lógica de negócio.
- **ViewModel**: Expõe `LiveData` ou `StateFlow` para a View. Contém lógica de apresentação e orquestra UseCases.
- **Repository**: Abstrai fonte de dados (local + remota). Retorna modelos de domínio, não DTOs/Entities.
- **UseCase**: Cada ação significativa é um UseCase separado com um único método público (Single Responsibility).

```java
// Exemplo: UseCase
public class RealizarCheckinUseCase {
    private final CheckinRepository repository;
    private final NetworkMonitor networkMonitor;

    @Inject
    public RealizarCheckinUseCase(CheckinRepository repository, NetworkMonitor networkMonitor) {
        this.repository = repository;
        this.networkMonitor = networkMonitor;
    }

    public LiveData<Resource<Checkin>> executar(String turnoId, String senha, double lat, double lon) {
        // Lógica: online → API direto, offline → salva fila local
    }
}
```

### 4.3. Injeção de Dependência (Hilt)
- `@HiltAndroidApp` na Application class.
- `@AndroidEntryPoint` em Activities e Fragments.
- `@Module` + `@InstallIn` para prover dependências (Retrofit, Room, etc.).
- `@Singleton` para objetos com ciclo de vida da aplicação.
- Usar `@Binds` para vincular interface à implementação.

```java
@Module
@InstallIn(SingletonComponent.class)
public abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract CheckinRepository bindCheckinRepository(CheckinRepositoryImpl impl);
}
```

### 4.4. Room (Banco Local)
- DAOs como interfaces com `@Dao`.
- Operações de escrita com `suspend` (Coroutines) ou `Completable`/`Single` (RxJava).
- Operações de leitura retornam `LiveData` ou `Flow` para reatividade automática.
- Migrations explícitas, nunca usar `fallbackToDestructiveMigration()` em produção.
- Testar migrations com `MigrationTestHelper`.

```java
@Dao
public interface CheckinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkin: CheckinPendente);

    @Query("SELECT * FROM checkins_pendentes ORDER BY timestampCriacao ASC")
    suspend fun getAllPendentes(): List<CheckinPendente>;

    @Query("DELETE FROM checkins_pendentes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>);
}
```

### 4.5. Retrofit + OkHttp (Rede)
- Definir uma única interface Retrofit por domínio (ex: `GuardPointApi`).
- Usar `@Headers("Content-Type: application/json")` ou interceptor global.
- `AuthInterceptor` adiciona `Authorization: Bearer <token>` automaticamente.
- `NetworkMonitor` com `ConnectivityManager.registerDefaultNetworkCallback`.
- Sempre tratar `HttpException`, `IOException`, e erros de timeout.

```java
public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = securePrefs.getAccessToken();
        if (token == null) return chain.proceed(chain.request());

        Request request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer " + token)
            .build();
        return chain.proceed(request);
    }
}
```

### 4.6. Foreground Service
- Declarar `android:foregroundServiceType="location"` no manifest.
- Criar canal de notificação dedicado no `onCreate`.
- `startForeground(id, notification)` em até 5 segundos após `onStartCommand` (obrigatório Android 8+).
- Remover listeners de localização no `onDestroy`.
- Usar `PartialWakeLock` se necessário manter CPU acordada.

```java
@AndroidEntryPoint
public class GuardPointForegroundService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification());
        startLocationUpdates();
        return START_STICKY; // Reinicia se for morto
    }

    @Override
    public void onDestroy() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }
}
```

### 4.7. WorkManager (Tarefas em Background)
- Usar `Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)`.
- `BackoffPolicy.EXPONENTIAL` para retry automático.
- `setExpedited()` para tarefas que precisam executar logo (Android 12+).
- Nomear workers com tag única para cancelamento: `WorkManager.cancelAllWorkByTag("sync")`.

```java
public class SyncWorker extends Worker {
    @NonNull
    @Override
    public Result doWork() {
        List<CheckinPendente> pendentes = database.checkinDao().getAllPendentes();
        if (pendentes.isEmpty()) return Result.success();

        try {
            api.syncLote(mapToRequest(pendentes)).execute();
            database.checkinDao().deleteByIds(ids);
            return Result.success();
        } catch (IOException e) {
            return Result.retry();
        }
    }
}
```

### 4.8. Permissões
- Verificar se a permissão já foi concedida com `ContextCompat.checkSelfPermission`.
- Solicitar com `ActivityResultContracts.RequestPermission` ou `RequestMultiplePermissions`.
- Fornecer justificativa com `shouldShowRequestPermissionRationale`.
- Tratar negação permanente (redirecionar para settings do app via Intent).

```java
private final ActivityResultLauncher<String> requestPermissionLauncher =
    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            startLocationUpdates();
        } else {
            showPermissionDeniedDialog();
        }
    });
```

### 4.9. Lifecycle e Vazamento de Memória
- ViewModels NUNCA devem referenciar Views, Activities ou Contexts (exceto `Application` context).
- Usar `viewModelScope` para Coroutines que devem ser canceladas com o ViewModel.
- Sempre remover observers, listeners e callbacks no `onDestroyView` ou `onCleared`.
- `LeakCanary` ativo em debug builds. Corrigir todo leak detectado.

### 4.10. Segurança
- Tokens JWT armazenados em `EncryptedSharedPreferences`.
- Senhas NUNCA armazenadas localmente (nem em SharedPreferences, nem no Room).
- Biometria como primeiro fator antes de acessar a tela de senha.
- ProGuard/R8 ativo em release builds com regras para Retrofit, Room, Gson.

### 4.11. Testes
- **Unitários**: JUnit 5 + Mockito + `turbine` (para testar Flow).
- **ViewModels**: `unmock` para simular Android framework em JVM.
- **Room**: testes de DAO com `Room.inMemoryDatabaseBuilder`.
- **UI**: Espresso para testes de interface (cenários críticos: login, check-in, sabotagem).

```java
@Test
public void realizarCheckin_offline_salvaNaFila() {
    when(networkMonitor.isOnline()).thenReturn(false);
    LiveData<Resource<Checkin>> result = useCase.executar(turnoId, senha, lat, lon);

    verify(localRepo).salvarPendente(any());
    assertEquals(Resource.loading(), result.getValue());
}
```

---

## 5. Comandos Úteis

```bash
# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Testes unitários
./gradlew test

# Testes instrumentados
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Limpar build
./gradlew clean

# Gerar APK com bundle
./gradlew bundleRelease
```

---

## 6. Anti-Padrões (NÃO FAZER)

- Executar operações de rede ou banco na thread principal (UI thread).
- Passar dados entre Activities via campos estáticos.
- Armazenar grandes objetos em `onSaveInstanceState` (use ViewModel + SavedStateHandle).
- Usar `AsyncTask` (deprecated, substituir por Coroutines).
- Usar `LocalBroadcastManager` (deprecated, substituir por LiveData/Flow).
- Hardcodar dimensões e cores (usar `dimens.xml`, `colors.xml`).
- Esquecer de declarar permissões no `AndroidManifest.xml`.
- Usar `dp` para tamanho de fonte — usar `sp`.
- Concatenar strings de UI — usar `strings.xml` e `getString(R.string.xxx, args)`.
- Ignorar `ActivityNotFoundException` ao abrir Intents externas.

---

## 7. Referências

- [Android Developer Guides](https://developer.android.com/guide)
- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Foreground Services](https://developer.android.com/develop/background-work/services/fg-service-types)
- [Java Code Conventions (Oracle)](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)

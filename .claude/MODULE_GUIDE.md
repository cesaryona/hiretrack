# Guia para criar um módulo

Padrões definidos na construção do módulo `job`. Use este guia como receita para os próximos módulos
(`application`, `notification`, ...). Na dúvida, copie o que o `job` faz.

## 1. Visão geral

- **Monólito modular** com Spring Modulith: um projeto, um deploy, um banco.
- Cada pacote direto em `br.com.hiretrack` é um **módulo**.
- Dentro de cada módulo: **camadas por feature** (`web` → service → `domain` / `infra`).
- As fronteiras são verificadas por `ModularityTests` (`ApplicationModules.verify()`). Ele precisa ficar sempre verde.

## 2. Estrutura de um módulo

```
br.com.hiretrack.<modulo>/
├── package-info.java          ← declara o módulo
├── <Modulo>Service.java       ← API pública do módulo (raiz = público)
├── domain/                    ← entidades e enums
│   ├── <Entidade>.java
│   └── <Entidade>Status.java
├── infra/                     ← repositories
│   └── <Entidade>Repository.java
└── web/                       ← HTTP
    ├── <Entidade>Controller.java
    ├── <Entidade>Mapper.java
    ├── request/Create<Entidade>Request.java
    └── response/<Entidade>Response.java
```

Regra do Modulith:
- O que está na **raiz** do pacote do módulo é **público**: outros módulos podem usar.
- O que está em **subpacotes** (`domain`, `infra`, `web`...) é **interno**: só o próprio módulo usa.
- O módulo `shared` é `OPEN`: todos podem usar qualquer classe dele.

## 3. Passo a passo

### 3.1 Declarar o módulo

`<modulo>/package-info.java`:
```java
@ApplicationModule(displayName = "Job")
package br.com.hiretrack.job;

import org.springframework.modulith.ApplicationModule;
```

### 3.2 Entidade (`domain/`)

- Sempre `extends BaseEntity` (de `br.com.hiretrack.shared`). Ela já traz:
  - `id` (UUID, gerado pelo Hibernate);
  - `createdAt` (`@CreationTimestamp`);
  - `updatedAt` (`@UpdateTimestamp`).
- **Sem setters.** Mudança de estado só por método com nome de negócio (`close()`, `changeStatus(...)`).
- Construtor `protected` sem argumentos, para o JPA, via Lombok.
- Construtor público com os campos obrigatórios.
- Enum sempre com `@Enumerated(EnumType.STRING)`.

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Job extends BaseEntity {

    private String title;
    private String company;
    private String description;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.OPEN;

    public Job(String title, String company, String description) {
        this.title = title;
        this.company = company;
        this.description = description;
    }

    public void close() {
        this.status = JobStatus.CLOSED;
    }
}
```

### 3.3 Repository (`infra/`)

```java
public interface JobRepository extends JpaRepository<Job, UUID> {
}
```

### 3.4 Service (raiz do módulo)

- É a **API pública** do módulo.
- Trabalha **só com tipos de domínio** (entidade, `UUID`, parâmetros simples).
- **Nunca** recebe nem devolve DTO de `web`, e **não** usa mapper.
- `@Transactional` na escrita e `@Transactional(readOnly = true)` na leitura.
- Busca que pode não achar → retorna `Optional`.
- Alteração de entidade dentro da transação não precisa de `save` (o dirty checking salva).

```java
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public Job create(Job job) {
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Optional<Job> findById(UUID id) {
        return jobRepository.findById(id);
    }

    @Transactional
    public Optional<Job> close(UUID id) {
        return jobRepository.findById(id).map(job -> {
            job.close();
            return job;
        });
    }
}
```

### 3.5 DTOs (`web/request`, `web/response`)

- `record` **públicos**, só com dados. Sem métodos, sem lógica e sem conversão.
- A validação (Bean Validation) fica no request.

```java
public record CreateJobRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 150) String company,
        @NotBlank String description) {
}

public record JobResponse(UUID id, String title, String company, String description, JobStatus status,
                          Instant createdAt, Instant updatedAt) {
}
```

### 3.6 Mapper (`web/`)

- MapStruct, com uma interface **package-private** em `web/`.
- Para campos com nomes diferentes: `@Mapping(source = ..., target = ...)`.

```java
@Mapper(componentModel = "spring")
interface JobMapper {

    Job toEntity(CreateJobRequest request);

    JobResponse toResponse(Job job);
}
```

### 3.7 Controller (`web/`)

- Classe e métodos **package-private**.
- Fluxo: request → mapper → service → mapper → response.
- `POST` devolve `201 Created` com o header `Location`.
- Busca por id: `ResponseEntity.of(optional)` devolve 200 se achar e 404 se não achar.
- `@Valid` em todo `@RequestBody`.

```java
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
class JobController {

    private final JobService jobService;
    private final JobMapper jobMapper;

    @PostMapping
    ResponseEntity<JobResponse> create(@Valid @RequestBody CreateJobRequest request) {
        var job = jobMapper.toResponse(jobService.create(jobMapper.toEntity(request)));
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(job.id()).toUri();
        return ResponseEntity.created(location).body(job);
    }

    @GetMapping("/{id}")
    ResponseEntity<JobResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.of(jobService.findById(id).map(jobMapper::toResponse));
    }

    @PatchMapping("/{id}/close")
    ResponseEntity<JobResponse> close(@PathVariable UUID id) {
        return ResponseEntity.of(jobService.close(id).map(jobMapper::toResponse));
    }
}
```

Endpoints: substantivo no plural (`/jobs`). Ação de negócio vira `PATCH /{id}/<acao>`.

### 3.8 Migration (Flyway)

- Pasta: `src/main/resources/db/migration/`.
- Nome: `V<n>__<descricao>.sql`. A numeração é **global** (compartilhada entre todos os módulos). A última é a `V3`, então a próxima é `V4`.
- **Nunca edite uma migration que já rodou.** Toda mudança vai numa migration nova.
- As colunas da `BaseEntity` são obrigatórias em toda tabela:

```sql
CREATE TABLE job (
    id          UUID PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    company     VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);
```

- **Sem FK entre tabelas de módulos diferentes.** Guarde só o id (ex.: `job_id UUID NOT NULL`, sem `REFERENCES job`).

### 3.9 Testes

- Testes de service com banco real via Testcontainers:

```java
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JobServiceTests {

    @Autowired
    JobService jobService;

    @Test
    void closesJob() {
        var job = jobService.create(new Job("Frontend Developer", "Acme", "React"));

        jobService.close(job.getId());

        assertThat(jobService.findById(job.getId())).get().extracting(Job::getStatus).isEqualTo(JobStatus.CLOSED);
    }
}
```

- Teste fica no mesmo pacote do módulo (`src/test/java/br/com/hiretrack/<modulo>/`).
- Precisa do Docker rodando. Comando: `./mvnw test`.

## 4. Comunicação entre módulos

Estas regras deixam cada módulo pronto para virar microsserviço depois.

| Situação | Como fazer |
|---|---|
| Módulo A precisa de um dado do B agora (ex.: a vaga existe?) | Chamar o `Service` público do B (na raiz do pacote) |
| Módulo A avisa que algo aconteceu (ex.: status mudou) | Publicar um **evento de domínio**. Quem se interessa escuta |
| Módulo A referencia uma entidade do B | Guardar **só o id** (`UUID jobId`). Nunca `@ManyToOne` para entidade de outro módulo |

- Nunca importe `domain`, `infra` ou `web` de outro módulo. O `ModularityTests` quebra.
- Se outro módulo precisar de dados, o service público devolve um tipo **público** (na raiz do módulo, ex.: `JobSummary`), nunca a entidade interna.
- Eventos:
  - são `record` na **raiz** do módulo que publica;
  - carregam os dados de que o consumidor precisa (ids, email, título, status), nunca uma entidade JPA;
  - são publicados com `ApplicationEventPublisher` dentro da transação;
  - são escutados com `@ApplicationModuleListener`. O Modulith guarda o evento na tabela `event_publication` (estilo Outbox).

## 5. Próximo módulo: `notification`

O `application` já está pronto (`Candidate`, `JobApplication`, `ApplicationStatus`) e publica
`ApplicationStatusChangedEvent` (na raiz de `application`) ao mudar o status.

- Escutar o evento com `@ApplicationModuleListener`.
- A migration da tabela `event_publication` já existe (`V3`).
- Por enquanto, "enviar" = logar. E-mail real fica para depois.

## 6. Checklist de um módulo novo

- [ ] `package-info.java` com `@ApplicationModule`
- [ ] Entidade em `domain/` estendendo `BaseEntity`, sem setters
- [ ] Repository em `infra/`
- [ ] Service na raiz, só com tipos de domínio
- [ ] DTOs `record` públicos em `web/request` e `web/response`
- [ ] Mapper MapStruct package-private em `web/`
- [ ] Controller package-private em `web/`, usando o mapper
- [ ] Migration `V<n>__...sql` com `id`, `created_at` e `updated_at`, sem FK entre módulos
- [ ] Testes de service com Testcontainers
- [ ] `./mvnw test` verde, incluindo `ModularityTests`

## 7. Pendências conhecidas

- Nenhum teste da camada HTTP (validação 400, 404, header `Location`).
- Imagem do Postgres em `latest`. Vale fixar uma versão (ex.: `postgres:17`) no `compose.yaml` e no `TestcontainersConfiguration`.

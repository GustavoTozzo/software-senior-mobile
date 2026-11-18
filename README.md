# MedSafe Senior (nome temporário) - Aplicativo Mobile (Android)

Aplicativo nativo para Android desenvolvido em **Kotlin com Jetpack Compose**, focado na usabilidade, acessibilidade e controle rigoroso de rotinas medicamentosas para a população idosa.

## Sobre o Projeto e Proposta de Valor
O *MedSafe Senior* resolve problemas críticos de adesão terapêutica na terceira idade. O aplicativo foi desenhado para facilitar a rotina de idosos e tranquilizar familiares/cuidadores através de uma interface simplificada (alto contraste, botões grandes e fontes legíveis) e recursos nativos inteligentes.

## Stack Tecnológica & UI/UX
* **Linguagem:** Kotlin
* **Framework UI:** Jetpack Compose (UI declarativa moderna para Android)
* **Comunicação HTTP:** Retrofit / Ktor para consumo da API Backend (Spring Boot)
* **Recursos Nativos:** Câmera (para leitura de código de barras), Alarmes locais (AlarmManager/WorkManager) e Intents para chamadas de emergência (190) e WhatsApp.

## Principais Funcionalidades do App
1. **Dashboard Diário:** Lista visual clara dos medicamentos que devem ser tomados no dia.
2. **Scanner de Código de Barras:** Facilidade no cadastro de novos remédios utilizando a câmera do celular.
3. **Sistema de Alertas e Notificações:** Alarmes programados para evitar o esquecimento de doses.
4. **Controle de Estoque Preditivo:** Alerta visual quando o medicamento está acabando, com botão de direcionamento rápido para o WhatsApp da farmácia cadastrada.
5. **Cronograma de Farmácia Popular:** Lembretes de prazos para liberação de novos medicamentos.
6. **Rede de Apoio e Emergência:** Botões de acesso rápido para o 190 e contatos de familiares/farmácias.

## Estado atual (Entregas 4, 5 e 6)
* Login e cadastro reais (JWT), sessão guardada no aparelho; token expirado leva de volta ao login.
* Dashboard com as doses do dia vindas da API e botões **Tomei** / **Pular**.
* **Estoque preditivo:** o aviso mostra quantos dias o estoque dura no ritmo das doses ("Dá para 4 dias"), oferece **Pedir na farmácia (WhatsApp)** com a mensagem já escrita e **Já comprei** para somar as unidades compradas.
* Cadastro de medicamento com **leitura de código de barras** pela câmera (CameraX + ML Kit) ou digitação, estoque, frequência e horários.
* Tela de **Histórico** com filtros Hoje / Semana / Mês, agrupado por dia.
* **Lembretes na hora de cada remédio**: alarmes exatos (AlarmManager) com notificação, reagendados após reiniciar o celular e funcionando sem internet.
* Tela de **Contatos**: farmácias (uma delas **preferida**, que recebe o pedido de reposição) e familiares/cuidadores, cada um com **Ligar** (abre o discador, sem permissão de ligação) e **WhatsApp**.
* **Botões de emergência** (SAMU 192 e Polícia 190) no topo do Dashboard e dos Contatos: abrem o discador com o número pronto e a pessoa confirma a chamada.
* **Farmácia Popular:** o remédio pode ser marcado como retirado na Farmácia Popular (data da última retirada e prazo de 30, 60 ou 90 dias). O Dashboard mostra "Libera em 3 dias (25/11)" e o botão **Já retirei** registra a retirada e soma as unidades ao estoque; uma notificação avisa às 9h do dia da liberação.
* **Ajustes:** tema (igual ao celular, claro, escuro ou alto contraste), tamanho do texto (normal, grande, muito grande) e liga/desliga dos lembretes de remédio e da Farmácia Popular.
* **Acessibilidade:** compatível com TalkBack (cabeçalhos, cartões lidos de uma vez, botões com descrição completa), áreas de toque de 48 dp e layout que quebra linha em vez de cortar o texto com a fonte do sistema em 200%.
* A aba Remédios (barra inferior) chega nas próximas entregas.

## Estrutura do Projeto
Projeto Android (Gradle + Kotlin DSL + version catalog) com Jetpack Compose, em `app/src/main/java/com/medsafe/mobile/`:
* `data/` — Retrofit/OkHttp (`api/`), repositório único de acesso à API, sessão (token) e tratamento de erros.
* `model/` — modelos de tela (medicamento, dose, farmácia, contato, sessão) e a lógica pura de telefones/WhatsApp (`Telefones`) e textos de estoque (`Estoque`).
* `ui/screens/` — Login, Dashboard, Cadastro de Medicamento, Histórico e Contatos; `ui/viewmodel/` — estado de cada tela; `ui/components/` — componentes compartilhados; `ui/scanner/` — leitor de código de barras; `ui/theme/` — paleta de alto contraste e tipografia ampliada.
* `alarmes/` — agendamento dos lembretes de remédio e de retirada, notificações (um canal para cada tipo) e reagendamento após o boot.

## Como rodar
1. Suba o backend (veja o README do `software-senior-backend`; o jeito mais rápido não exige instalar PostgreSQL).
2. Abra esta pasta no Android Studio e aguarde o Gradle Sync, ou use o wrapper: `./gradlew installDebug`.
3. O app aponta, por padrão, para `http://10.0.2.2:8080/` (o localhost da máquina visto pelo emulador). Em um celular físico, informe o IP da máquina na rede: `./gradlew installDebug -PapiBaseUrl=http://192.168.0.10:8080/`.

O tráfego HTTP sem TLS é permitido **apenas** no build de debug (`app/src/debug`); o build de release exige HTTPS.

## Testes
* **Unitários** (JVM, rápidos): `./gradlew testDebugUnitTest` — testes unitários da lógica pura (cálculo do próximo horário do alarme, períodos do histórico, mapeamento de resultados, telefones e links do WhatsApp, textos e pedido de reposição do estoque).
* **Instrumentados** (precisam de um emulador ou celular Android ligado): `./gradlew connectedDebugAndroidTest`. Cobrem a leitura de códigos de barras EAN-13 pelo mesmo leitor do scanner (imagens desenhadas no teste) e o caminho real dos avisos: o agendador, o receiver disparado como o AlarmManager faria e a notificação que aparece.
* **CI:** o GitHub Actions compila o app, roda os testes unitários e guarda o APK de debug a cada push (`.github/workflows/ci.yml`).

Observação: o app é nativo Android e não roda em iPhone. Para testar sem um aparelho Android use o emulador do Android Studio.

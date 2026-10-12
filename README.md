# Movimento em Foco

Protótipo Android de visão computacional (miniprojeto da Capacitação em Visão Computacional · IFPE · INPaR · FACEPE). O app estima a **pose do corpo (keypoints)** em imagens e vídeos, **100% no próprio aparelho**, desenha o esqueleto sobre os quadros e calcula o **ângulo do joelho** e ângulos do tronco e dos braços.

> Protótipo educacional. Não é ferramenta médica: não faz diagnóstico, não avalia técnica de exercício e não previne lesões.

## O que o app faz

1. Na tela inicial, o usuário toca em **Nova análise** e escolhe uma imagem ou um vídeo da galeria (ou grava um vídeo).
2. O MediaPipe Pose Landmarker estima 33 pontos do corpo por quadro; o app desenha os 22 pontos do corpo (ombros, braços, mãos, quadril, pernas e pés), sem os 11 pontos do rosto.
3. Calcula o ângulo do joelho (quadril–joelho–tornozelo) nos dois lados e escolhe o lado com mais medidas válidas. Desse lado, calcula também quatro ângulos da parte superior do corpo: quadril (ombro–quadril–joelho), inclinação do tronco, ombro (quadril–ombro–cotovelo) e cotovelo (ombro–cotovelo–punho).
4. Mostra o resultado: um indicador de **qualidade da detecção** (Boa, Regular, Baixa ou Insuficiente) logo abaixo do título, um resumo em texto e quadros com o esqueleto desenhado e os ângulos de cada quadro (joelhos e, do lado principal, quadril, tronco, ombro e cotovelo). Em vídeos, 12 quadros são analisados e até 4 são exibidos.
5. Salva a análise em um **histórico local**, que pode ser reaberto ou removido.
6. No fim do resultado, exibe um **vídeo de referência** (ilustrativo).

## Como funciona

| Etapa | O que acontece |
|---|---|
| Quadros | Em vídeos, a duração é dividida em 12 trechos iguais e o quadro do centro de cada trecho é analisado. Em imagens, a foto é analisada respeitando a orientação EXIF. |
| Pose | O MediaPipe Pose Landmarker (modelo `lite`) devolve 33 keypoints por quadro, cada um com visibilidade. |
| Medida | Ângulo 2D no joelho, em graus, calculado em pixels. Pontos com visibilidade abaixo de 0,5 são descartados. |
| Ângulos extras | Do mesmo lado do joelho: quadril (ombro–quadril–joelho), ombro (quadril–ombro–cotovelo), cotovelo (ombro–cotovelo–punho) e inclinação do tronco (linha quadril→ombro em relação à horizontal, de 0° a 90°). Mesma regra de visibilidade. |
| Qualidade | Fração de quadros com ângulo do joelho válido: boa (90% ou mais), regular (60% ou mais), baixa (menos de 60%) ou insuficiente (nenhum). Com 12 quadros: boa = 11 ou 12 válidos, regular = 8 a 10, baixa = 1 a 7, insuficiente = 0. |
| Seleção | Até 4 quadros são mostrados: o de menor ângulo, o de maior ângulo, o primeiro e o último. |
| Resultado | Quadros com esqueleto (JPEG), resultado (JSON) e miniatura ficam no armazenamento privado do app. |

A análise roda fora da thread da interface e pode ser cancelada. Os limiares (0,5; 90%; 60%) são **experimentais** e não foram validados.

## Tecnologias

- Kotlin 2.0.21, Jetpack Compose (Material 3, BOM 2024.12.01), Android Gradle Plugin 8.8.2
- MediaPipe Tasks Vision 0.10.35 (modelo `pose_landmarker_lite.task`)
- minSdk 24 (Android 7.0), compileSdk/targetSdk 35, Java 17
- Armazenamento em arquivos locais (JSON + JPEG), sem banco de dados e sem permissões

## Como rodar

Requisitos: Android Studio recente (com JDK 17), celular Android 7.0 ou superior (ou emulador) e internet na primeira sincronização do Gradle.

1. Abra a pasta do projeto no Android Studio e aguarde o Gradle Sync terminar.
2. Confira se o modelo está em `app/src/main/assets/pose_landmarker_lite.task` (cerca de 5,8 MB). Se estiver faltando, baixe em https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task e coloque nessa pasta. No Windows, também dá para rodar na raiz do projeto: `powershell -ExecutionPolicy Bypass -File scripts\baixar_modelo.ps1`.
3. No celular, ative as opções do desenvolvedor e a depuração USB, conecte ao computador e clique em **Run**.

**Vídeo de referência:** o app toca o arquivo `app/src/main/res/raw/video_referencia.mp4`. Para trocar o vídeo, substitua o arquivo mantendo o nome (minúsculas, sem espaços). Sem o arquivo, o app mostra um aviso no lugar.

## Estrutura

```
app/src/main/
├── assets/pose_landmarker_lite.task   modelo de pose
├── res/raw/video_referencia.mp4       vídeo de referência
└── java/br/com/movimentoemfoco/app/
    ├── analysis/   geometria, ângulos, qualidade, amostragem, seleção de quadros, resumo
    ├── media/      leitura de imagem e vídeo, desenho do esqueleto
    ├── model/      modelos de dados e índices dos keypoints
    ├── pipeline/   AnalisadorMidia: orquestra todo o fluxo
    ├── pose/       PoseEstimator: envolve o MediaPipe
    ├── storage/    histórico local (JSON + JPEG)
    └── ui/         telas (Compose) e ViewModel
scripts/baixar_modelo.ps1              baixa o modelo (Windows)
```

## Testes

- **Unitários (JUnit)** em `app/src/test`: geometria, ângulos, qualidade, amostragem, seleção de quadros, lado principal, resumo, JSON e histórico. Para rodar: `./gradlew test` (no Windows, `gradlew test`).
- **Manuais:** o app foi compilado e executado em um **Samsung Galaxy S24+ e em um S25 (Android)**, com vídeos prontos da galeria (pessoas se exercitando). Análise, resultado com esqueleto, histórico e vídeo de referência funcionaram. A gravação de vídeo pelo app não foi usada nos testes.
- Não foi testado em outros modelos de celular nem em iPhone (o app é só para Android).
- Os ângulos do tronco e dos braços foram usados nos testes manuais e ainda não têm testes unitários.
- A qualidade da detecção foi observada nos testes manuais; este repositório não traz avaliação quantitativa contra um gabarito.

## Ajustes feitos após os testes no aparelho

- **Legibilidade:** com o Android em modo escuro, o texto dos cards de fundo claro ficava ilegível (texto claro sobre fundo claro). Esses cards agora definem texto escuro.
- **Vídeo de referência:** incluído no app, na tela de resultado.
- **Parte superior do corpo:** além do joelho, o app passou a mostrar quadril, inclinação do tronco, ombro e cotovelo (do mesmo lado do joelho).

## Limitações

- O ângulo é 2D, no plano da imagem: depende do ângulo da câmera, de partes do corpo escondidas e do enquadramento. Corpo inteiro e vista lateral funcionam melhor.
- Em vista lateral, o joelho do lado mais distante da câmera costuma aparecer como "sem dados confiáveis".
- O modelo não tem pontos da coluna, então a curvatura do tronco não é medida. A inclinação do tronco é só uma aproximação e depende de o celular estar em pé e do chão estar na horizontal.
- Os ângulos extras usam o mesmo lado do joelho: se o joelho não for detectado em nenhum quadro, eles não aparecem.
- O app mostra medidas, mas não diz se o exercício está certo, não conta repetições e não compara o usuário com o vídeo de referência (que é apenas ilustrativo).
- Só 12 quadros por vídeo: não detecta fases do movimento nem repetições.
- Limiares experimentais e testes em apenas dois aparelhos (S24+ e S25): o desempenho pode variar em outros celulares.

## Privacidade e ética

- **Nada sai do aparelho:** o app não tem permissão de internet e a inferência é local.
- **O que fica guardado:** só os quadros com o esqueleto desenhado, a miniatura e o resultado (JSON), no armazenamento privado do app. O vídeo analisado não é guardado (o vídeo gravado pelo app é apagado depois da análise). Cada análise pode ser removida pelo usuário.
- **Pessoas nos vídeos:** os testes usaram vídeos prontos de fontes públicas, sem gravar pessoas.
- **Onde o sistema pode errar mais:** o modelo pode ter desempenho pior com certos tipos de corpo, roupas, posições (deitado, de costas), iluminação fraca e ângulos de câmera pouco comuns. Isso não foi medido.
- Não é ferramenta médica nem garante execução correta de exercícios.

## Créditos dos vídeos

Vídeos de terceiros, do Pexels (licença Pexels: uso gratuito; termos em https://www.pexels.com/license/):

- **Vídeo de referência** (tocado no app): mulher fazendo exercício abdominal, de ROMAN ODINTSOV. https://www.pexels.com/video/woman-doing-abdominal-exercise-8233050/
- **Vídeo de demonstração** (tela inicial e capturas de tela do app): homem se exercitando, de cottonbro studio. https://www.pexels.com/video/a-man-working-out-6293127/

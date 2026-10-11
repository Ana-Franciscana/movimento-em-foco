# Baixa o modelo de pose para a pasta de recursos do app.
# Se o download falhar, confira o endereço do modelo na documentação do MediaPipe.
$url = "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/latest/pose_landmarker_lite.task"
$destino = Join-Path $PSScriptRoot "..\app\src\main\assets\pose_landmarker_lite.task"
New-Item -ItemType Directory -Force -Path (Split-Path $destino) | Out-Null
Invoke-WebRequest -Uri $url -OutFile $destino
Write-Host "Modelo salvo em $destino"

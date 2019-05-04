{{/*
Application name
*/}}
{{- define "application.name" -}}
{{- printf "%s-%s" .Chart.Name .Values.app.version | quote -}}
{{- end -}}

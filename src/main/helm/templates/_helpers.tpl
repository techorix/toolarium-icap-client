{{/*
Create docker config json data entry line
*/}}
{{- define "ociConfigJson" }}
{{- if .Values.ociConfigJson.enabled }}
{{ $output := printf "{\"auths\": {\"%s\": {\"auth\": \"%s\"}}}" .Values.ociConfigJson.registry (printf "%s:%s" (required "A username for the pull secret is required" .Values.ociConfigJson.username) (required "A password for the pull secret is required" .Values.ociConfigJson.password) | b64enc) | b64enc}}
{{- print ".dockerconfigjson: " "\"" $output "\"" | indent 2}}
{{- end }}
{{- end }}
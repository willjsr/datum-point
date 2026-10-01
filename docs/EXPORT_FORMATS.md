# Formatos de exportação

## CSV

UTF-8 com cabeçalho. Campos mínimos:

```text
project,point,sequence,latitude,longitude,easting,northing,altitude,crs,timestamp,duration_seconds,accepted_fixes,rejected_fixes,reported_accuracy_median,sigma_e,sigma_n,radial_rms,satellites_used,quality,notes
```

## TXT

Relatório legível com projeto, CRS, pontos, perímetro, área e aviso de finalidade.

## GeoJSON

`FeatureCollection` com:

- `Point` por ponto;
- `LineString` quando houver dois ou mais pontos;
- `Polygon` quando houver três ou mais pontos.

## KML

Documento KML com `Placemark` para pontos e `LineString` para geometria observada.

## DXF

DXF ASCII simples com:

- `POINT`;
- `TEXT`;
- `LWPOLYLINE`.

As coordenadas CAD usam Easting/Northing.

## PDF e KMZ

PDF e KMZ fazem parte do alvo v1.0, mas a primeira base implementa os exportadores textuais e CAD/GIS auditáveis.

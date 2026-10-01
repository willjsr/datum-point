# Sistemas de coordenadas

Datum Point preserva a coordenada primária em latitude/longitude WGS84.

Para Curitiba e Região Metropolitana, a projeção padrão é:

```text
SIRGAS 2000 / UTM zone 22S
EPSG:31982
Elipsoide GRS80
```

A implementação inicial define explicitamente os parâmetros UTM em código para evitar carregar uma base EPSG completa.

## Transparência de datum

O GNSS do smartphone fornece uma solução vinculada ao referencial usado pelo sistema Android e pelo chipset. A conversão para SIRGAS 2000 / UTM 22S é uma transformação prática para reconhecimento de campo e não uma materialização geodésica oficial de época.

Casas decimais adicionais não representam precisão adicional.

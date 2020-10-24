# Mapas Kotlin

2DAM PMYDM App para jugar un poco con los mapas

[![Android](https://img.shields.io/badge/App-Android-g)](https://www.android.com/intl/es_es/)
[![Kotlin](https://img.shields.io/badge/Code-Kotlin-blue)](https://kotlinlang.org/)
[![LICENSE](https://img.shields.io/badge/Lisence-MIT-green)](https://github.com/joseluisgs/MapasKotlin/blob/master/LICENSE)
![GitHub](https://img.shields.io/github/last-commit/joseluisgs/MapasKotlin)

## Descripción

Sencilla app para App para jugar un poco con los mapas

## Consideraciones importantes
No olvides añadir esto a tu Gradle en su versión más alta (por ejemplo la 17 o más):
```xml
implementation 'com.google.android.gms:play-services-maps:17.0.0'
implementation 'com.google.android.gms:play-services-location:17.0.0'
```    
### Manifest y claves Mapas
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<meta-data android:name="com.google.android.geo.API_KEY"
android:value="@string/google_maps_key" />
 ```      
En el modo debug se hace en ese fichero con la huella SHA-1 y se pone, en el Modo Release,
se debe generar con keytool la huella SHA-1 con los datos del paquete Release, crear un proyecto y subirla
https://developers.google.com/maps/documentation/android-sdk/get-api-key  
            
    SI NO VES EL MAPA ES POR ESO
### Referencias
Se ha seguido este tutorial aproximadamente

Documentación Oficial: 
- https://developers.google.com/maps/documentation/android-sdk/map-with-marker
- https://developer.android.com/training/maps?hl=es-419
- https://developers.google.com/android/reference/com/google/android/gms/maps/UiSettings
- https://developers.google.com/maps/documentation/android-sdk/marker
- https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial

Resumen muy completo
- http://www.hermosaprogramacion.com/2016/05/google-maps-android-api-v2/

Permisos
- https://developer.android.com/training/permissions/requesting.html

## Autor
[José Luis González Sánchez](https://twitter.com/joseluisgonsan) 

[![Twitter](https://img.shields.io/twitter/follow/joseluisgonsan?style=social)](https://twitter.com/joseluisgonsan)

[![GitHub](https://img.shields.io/github/followers/joseluisgs?style=social)](https://github.com/joseluisgs)

## Licencia

Este proyecto esta licenciado bajo licencia **MIT**, si desea saber más, visite el fichero [LICENSE](https://github.com/joseluisgs/MapasKotlin/blob/master/LICENSE) para su uso docente y educativo.

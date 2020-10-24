package com.joseluisgs.mapaskotlin

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private lateinit var mMap: GoogleMap
    private val LOCATION_REQUEST_CODE = 1 // Para los permisos

    private var permisos = false

    // Para obtener el punto actual (no es necesario para el mapa)
    // Pero si para obtener las latitud y la longitud
    private var mPosicion: FusedLocationProviderClient? = null

    private var miUltimaLocalizacion: Location? = null
    private val posDefecto = LatLng(38.6901212, -4.1086075)
    private var posActual = posDefecto

    // Marcador actual
    private var marcadorActual: Marker? = null

    // Marcador marcadorTouch
    private var marcadorTouch: Marker? = null

    // Posición actual con eventos y no hilos
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private val CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initPermisos()

        initUI()
    }

    private fun initUI() {
        // Para obtener las coordenadas de la posición actual
        // s decir lleer nosotros manualmente el GPS
        // No es necesario para pintar la bola azul
        // Construct a FusedLocationProviderClient.
        mPosicion = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar IU Mapa
        configurarIUMapa()

        // Añadmimos marcadores
        añadirMarcadores()

        // activa el evento de marcadores Touch
        activarEventosMarcdores()

        // Obtenemos la posición GPS
        // Esto lo hago para informar de la última posición
        // Obteniendo coordenadas GPS directamente
        obtenerPosicion()

        // Situar la camara inicialmente a una posición determinada
        situarCamaraMapa()

        // Acrtualizar cada X Tiempo, implica programar eventos o hacerlo con un hilo
        // Esto consume, por lo que ideal es activarlo y desactivarlo
        // cuando sea necesario
        //autoActualizador();

        // Para usar eventos
        // Yo lo haría con obtener posición, pues hacemos lo mismo. Ya decides
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        // Crear el LocationRequest
        // Es muy similar a lo que yo he hecho manualmente con el reloj en     private void autoActualizador {
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10 * 1000)        // 10 segundos en milisegundos
            .setFastestInterval(1 * 1000) // 1 segundo en milisegundos


        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun activarEventosMarcdores() {
        mMap.setOnMapClickListener { point -> // Creamos el marcador
            // Borramos el marcador Touch si está puesto
            marcadorTouch?.remove()
            marcadorTouch = mMap.addMarker(
                MarkerOptions() // Posición
                    .position(point) // Título
                    .title("Marcador Touch") // Subtitulo
                    .snippet("El Que tú has puesto") // Color o tipo d icono
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
        }
    }

    private fun situarCamaraMapa() {
        // Puedo moverla a una posición que queramos
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(posDefecto));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(posActual))
    }

    private fun configurarIUMapa() {
        // Puedo activar eventos para un solo marcador o varios
        // Con la interfaz  OnMarkerClickListener
        mMap.setOnMarkerClickListener(this)
        // Activar Boton de Posición actual
        if (permisos) {
            // Si tenemos permisos pintamos el botón de la localización actual
            // Esta posición la obtiene google automaticamente y no tiene que ver con
            // obtener posición
            mMap.isMyLocationEnabled = true
        }

        // Mapa híbrido, lo normal es usar el
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        // Que se vea la interfaz y la brújula por ejemplo
        // También podemos quitar gestos
        val uiSettings: UiSettings = mMap.uiSettings
        // Activamos los gestos
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        // Activamos la brújula
        uiSettings.isCompassEnabled = true
        // Activamos los controles de zoom
        uiSettings.isZoomControlsEnabled = true
        // Activamos la brújula
        uiSettings.isCompassEnabled = true
        // Actiovamos la barra de herramientas
        uiSettings.isMapToolbarEnabled = true

        // Hacemos el zoom por defecto mínimo
        mMap.setMinZoomPreference(15.0f)
        // Señalamos el tráfico
        mMap.isTrafficEnabled = true
    }

    private fun añadirMarcadores() {
        // Podemos lerlos de cualquier sitios
        // Añadimos un marcador en la estación
        // Podemos leerlos de un servición, BD SQLite, XML, Arraylist, etc
        val estacion = LatLng(38.69128, -4.111655)
        mMap.addMarker(
            MarkerOptions() // Posición
                .position(estacion) // Título
                .title("Estación de AVE") // Subtitulo
                .snippet("Estación AVE de Puertollano") // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)) //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ayuntamiento))
        )

        // Añadimos el ayuntamiento
        // Añadimos un marcador en la estación
        val ayto = LatLng(38.6866069, -4.1110002)
        mMap.addMarker(
            MarkerOptions() // Posición
                .position(ayto) // Título
                .title("Ayuntamiento") // Subtitulo
                .snippet("Ayuntamiento de Puertollano") // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ayuntamiento))
        )
    }


    override fun onMarkerClick(marker: Marker?): Boolean {
        val titulo = marker!!.title
        when (titulo) {
            "Ayuntamiento" -> Toast.makeText(
                this, marker.title.toString() +
                        " Mal sitio para ir.",
                Toast.LENGTH_SHORT
            ).show()

            "Estación de AVE" -> Toast.makeText(
                this, marker.title.toString() +
                        " Corre que pierdes el tren.",
                Toast.LENGTH_SHORT
            ).show()

            "Marcador Touch" -> Toast.makeText(
                this, "Estás en: " + marker.position.latitude.toString() + "," + marker.position.longitude,
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }

    // Obtenermos y leemos directamente el GPS
    // Esto se puede hacer trabajemos con mapas o no
    // Por ejemplo pata mostrar la localización en etiquetas
    private fun obtenerPosicion() {
        try {
            if (permisos) {
                // Lo lanzamos como tarea concurrente
                val local: Task<Location> = mPosicion!!.lastLocation
                local.addOnCompleteListener(this, object : OnCompleteListener<Location?> {
                    override fun onComplete(task: Task<Location?>) {
                        if (task.isSuccessful) {
                            // Actualizamos la última posición conocida
                            miUltimaLocalizacion = task.result
                            posActual = LatLng(
                                miUltimaLocalizacion!!.latitude,
                                miUltimaLocalizacion!!.longitude
                            )
                            // Añadimos un marcador especial para poder operar con esto
                            marcadorPosicionActual()
                            informarPosocion()
                        } else {
                            Log.d("GPS", "No se encuetra la última posición.")
                            Log.e("GPS", "Exception: %s", task.exception)
                        }
                    }
                })
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message.toString())
        }
    }

    // Para dibujar el marcador actual
    private fun marcadorPosicionActual() {
        /*
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(this.posActual)
                .radius(50)
                .strokeColor(Color.MAGENTA)
                .fillColor(Color.BLUE));

        */

        // Borramos el arcador actual si está puesto
        marcadorActual?.remove()
        // añadimos el marcador actual
        marcadorActual = mMap.addMarker(
            MarkerOptions() // Posición
                .position(posActual) // Título
                .title("Mi Localización") // Subtitulo
                .snippet("Localización actual") // Color o tipo d icono
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }

    private fun informarPosocion() {
        Toast.makeText(
            this,
            """Ultima posición Conocida:
                     - Latitid: ${miUltimaLocalizacion!!.latitude}
                    - Logitud: ${miUltimaLocalizacion!!.longitude}
                    - Altura: ${miUltimaLocalizacion!!.altitude}
                    - Precisón: ${miUltimaLocalizacion!!.accuracy}""",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Hilo con un reloj interno
    // Te acuerdas de los problemas de PSP con un Thread.Sleep()
    // Aqui lo llevas
    private fun autoActualizador() {
        val handler = Handler()
        val timer = Timer()
        val doAsyncTask: TimerTask = object : TimerTask() {
            override fun run() {
                handler.post(Runnable {
                    try {
                        // Obtenemos la posición
                        obtenerPosicion()
                        situarCamaraMapa()
                    } catch (e: Exception) {
                        Log.e("TIMER", "Error: " + e.message)
                    }
                })
            }
        }
        // Actualizamos cada 10 segundos
        // podemos pararlo con timer.cancel();
        timer.schedule(doAsyncTask, 0, 10000)
    }

    // Manejador del evento de nueva conexion
    private fun handleNewLocation(location: Location) {
        Log.d("Mapa", location.toString())
        miUltimaLocalizacion = location
        posActual = LatLng(
            miUltimaLocalizacion!!.latitude,
            miUltimaLocalizacion!!.longitude
        )
        // Añadimos un marcador especial para poder operar con esto
        marcadorPosicionActual()
        informarPosocion()
        situarCamaraMapa()
    }

    // Cuando nos conectamos
    override fun onConnected(bundle: Bundle?) {
        val location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        location?.let { handleNewLocation(it) }
            ?: LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
    }

    override fun onConnectionSuspended(value: Int) {
        Log.d("GPS", "Conexión Suspendida")
    }

    override fun onConnectionFailed(value: ConnectionResult) {
        Log.d("GPS", "Conexión Fallida")
    }

    override fun onLocationChanged(location: Location?) {
        location?.let { handleNewLocation(it) }
    }

    // Permisos
    /**
     * Comprobamos los permisos de la aplicación
     */
    private fun initPermisos() {
        // Indicamos el permisos y el manejador de eventos de los mismos
        Dexter.withContext(this)
            // Lista de permisos a comprobar
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            // Listener a ejecutar
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // ccomprbamos si tenemos los permisos de todos ellos
                    if (report.areAllPermissionsGranted()) {
                        permisos = true
                        Toast.makeText(applicationContext, "¡Permisos de localización concedidos!", Toast.LENGTH_SHORT)
                            .show()
                    }

                    // comprobamos si hay un permiso que no tenemos concedido ya sea temporal o permanentemente
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // abrimos un diálogo a los permisos
                        //openSettingsDialog();
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { Toast.makeText(applicationContext, "¡Existe errores! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
    }
}
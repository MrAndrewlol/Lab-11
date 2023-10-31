package edu.uvg.ubicaciongps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import edu.uvg.ubicaciongps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    val Permission_ID = 100
    private var ispermisos = false
    lateinit var button : Button


    lateinit var mFusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var locationsCallback : LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        findViewById<Button>(R.id.btndetectar)
            .setOnClickListener {

                verificarpermisos()

            }


    }

    private fun verificarpermisos() {
        val permisos = arrayListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION

        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            permisos.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        val permisosArray = permisos.toTypedArray()
        if (tienePermisos(permisosArray)){
            ispermisos = true
            onPermisosConcedidos()
        }
        else{
            solicitarPermisos(permisosArray)
        }
    }

    private fun tienePermisos(permisos: Array<String>) : Boolean{
        return permisos.all {
            return ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun onPermisosConcedidos(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            mFusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it !=null){
                    imprimirUbicacion(it)
                }
                else{
                    Toast.makeText(this, "No se puede obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }



        val locationRequests = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000
        ).apply {
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        locationsCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                for(location in p0.locations){
                    imprimirUbicacion(location)
                }
            }
        }

        mFusedLocationProviderClient.requestLocationUpdates(
            locationRequests,
            locationsCallback,
            Looper.getMainLooper()
        )

        } catch (_: SecurityException){

        }

    }

    private fun solicitarPermisos(permisos: Array<String>){
        requestPermissions(
            permisos,
            Permission_ID
        )
    }

    private fun imprimirUbicacion(ubicacion : Location){
        binding.textView2.text = "Latitud Marcada: ${ubicacion.latitude}"
        binding.lbllongitud.text = "Longitud Marcada: ${ubicacion.longitude}"
        Log.d("GPS", "LAT: ${ubicacion.latitude} - LONGITUD: ${ubicacion.longitude}")

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Permission_ID){
            val todosPermisosConcedidos = grantResults.all{it == PackageManager.PERMISSION_GRANTED}

            if(grantResults.isNotEmpty() && todosPermisosConcedidos){
                ispermisos = true
                onPermisosConcedidos()
            }
        }

    }




}
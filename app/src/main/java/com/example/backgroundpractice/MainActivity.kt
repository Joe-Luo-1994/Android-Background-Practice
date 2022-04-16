package com.example.backgroundpractice

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.backgroundpractice.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  // View Area
  private var _binding: ActivityMainBinding? = null
  private val binding get() = _binding!!

  // Service Area
  lateinit var downloadBinder: MyService.DownloadBinder

  // Permission Area
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    initOnClickListener()
    requestPermissionLauncher =
      registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
          Log.d(TAG, "requestPermission: Granted")
        } else {
          Log.d(TAG, "requestPermission: Not Granted")
        }
      }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun initOnClickListener() {
    binding.apply {
      btnStartService.setOnClickListener {
        val intent = Intent(this@MainActivity, MyService::class.java)
        startService(intent)
      }
      btnStopService.setOnClickListener {
        val intent = Intent(this@MainActivity, MyService::class.java)
        stopService(intent)
      }
      btnBindService.setOnClickListener {
        val intent = Intent(this@MainActivity, MyService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
      }
      btnUnbindService.setOnClickListener {
        unbindService(connection)
      }
      btnStartTimerService.setOnClickListener {
        Log.d(TAG, "btnStartTimerService Clicked")
        val intent = Intent(this@MainActivity, TimerService::class.java)
        startService(intent)
      }
      btnRequestPermission.setOnClickListener {
        requestPermission()
      }
      btnCheckPermission.setOnClickListener {
        checkUsageStatPermission()
      }
    }
  }

  private fun checkUsageStatPermission() {
    if (ContextCompat.checkSelfPermission(
        this@MainActivity,
        Manifest.permission.PACKAGE_USAGE_STATS
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      Log.d(TAG, "checkUsageStatPermission: Granted")
    } else {
      Log.d(TAG, "checkUsageStatPermission: Not Granted")
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun requestPermission() {
    when {
      ContextCompat.checkSelfPermission(
        this@MainActivity,
        Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED -> {
        Log.d(TAG, "requestPermission: Already Granted")
      }
      shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
        Log.d(TAG, "requestPermission: Should show request permission")
      }
      else -> {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
      }
    }
  }

  private val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
      downloadBinder = binder as MyService.DownloadBinder
      downloadBinder.startDownload()
      downloadBinder.getProgress()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
      TODO("Not yet implemented")
    }
  }
}
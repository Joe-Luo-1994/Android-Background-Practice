package com.example.backgroundpractice

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Process.myUid
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.backgroundpractice.databinding.ActivityMainBinding
import com.example.backgroundservice.LaunchListQuery

class MainActivity : AppCompatActivity() {

  // View Area
  private var _binding: ActivityMainBinding? = null
  private val binding get() = _binding!!

  // Service Area
  lateinit var downloadBinder: MyService.DownloadBinder

  // Permission Area
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

  // Manager Area
  private lateinit var usageStatsManager: UsageStatsManager

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
        if (checkUsageStatsPermission()) {
          Log.d(TAG, "initOnClickListener: Permission Granted")
        } else {
          Log.d(TAG, "initOnClickListener: Not Granted")
          Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            startActivity(this)
          }
        }
      }
      btnCheckUsageStats.setOnClickListener {
        usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        val dataUsageStats: List<UsageStats> = usageStatsManager.queryUsageStats(
          UsageStatsManager.INTERVAL_DAILY,
          currentTime - 3 * 60 * 1000,
          currentTime
        )
        dataUsageStats.forEach {
          Log.d(TAG, "${it.packageName} _ last time used: ${it.lastTimeUsed}")
        }
      }
    }
  }

  private fun checkUsageStatsPermission(): Boolean {
    val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      appOpsManager.unsafeCheckOpNoThrow("android:get_usage_stats", myUid(), packageName)
    } else {
      appOpsManager.checkOpNoThrow("android:get_usage_stats", myUid(), packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
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

  override fun onResume() {
    super.onResume()
    lifecycleScope.launchWhenResumed {
      val response = apolloClient.query(LaunchListQuery()).execute()
      Log.d(TAG, "Success ${response.data}")
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
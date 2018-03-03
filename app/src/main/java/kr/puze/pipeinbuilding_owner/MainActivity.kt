package kr.puze.pipeinbuilding_owner

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import app.akexorcist.bluetotohspp.library.BluetoothSPP
import app.akexorcist.bluetotohspp.library.BluetoothState
import kotlinx.android.synthetic.main.activity_main.*
import kr.puze.pipeinbuilding_owner.Server.RetrofitService
import kr.puze.pipeinbuilding_owner.Server.Schema
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    var status: Boolean = false

    lateinit var retrofitService: RetrofitService
    lateinit var progressDialog: ProgressDialog
    lateinit var bt: BluetoothSPP
    lateinit var btAdapter: BluetoothAdapter
    lateinit var token: String
    lateinit var pipeEnergy: Number
    lateinit var energy: Number

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //전체화면 만들기
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        //퍼미션
        permisionCheck()
    }

    fun permisionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("puze", "PERMISSION")
                    requestPermissions(arrayOf<String>(android.Manifest.permission.INTERNET,
                            android.Manifest.permission.BLUETOOTH,
                            android.Manifest.permission.BLUETOOTH_ADMIN),
                            200)
                    Log.d("puze", "initApp0")
                    initApp()
                }
            } else {
                Log.d("puze", "initApp1")
                initApp()
            }
            initApp()
        } else {
            Log.d("puze", "initApp2")
            initApp()
        }
    }

    fun initApp(): Unit {
        bt = BluetoothSPP(applicationContext)
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        retrofitSetting()
        bluetooth()

        btn_switch.setOnClickListener {
            status = (!status)
            if(status){
                txt_switch.text = "On"
                bt.send("o", true)
                retrofitPost()
            }
            else{
                txt_switch.text = "Off"
                bt.send("x",true)
                retrofitPut()
            }
        }
    }

    fun retrofitPost(){
        progressDialogSetting()
        var call: Call<Schema.History> = retrofitService.post(status)
        call.enqueue(object : Callback<Schema.History> {
            override fun onResponse(call: Call<Schema.History>?, response: Response<Schema.History>?) {
                Log.d("puze", "onResponse")
                Log.d("puze", response?.code().toString())
                if (response?.code() == 200) {
                    progressDialog.dismiss()
                    Log.d("check", response.message().get(1).toString())
                    token = response.message().get(1).toString()
                } else {
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<Schema.History>?, t: Throwable?) {
                Log.d("puze", "onFailure")
                progressDialog.dismiss()
            }
        })
    }

    fun retrofitPut(){
        progressDialogSetting()
        var call: Call<Schema.History> = retrofitService.put(token, status, pipeEnergy, energy)
        call.enqueue(object : Callback<Schema.History> {
            override fun onResponse(call: Call<Schema.History>?, response: Response<Schema.History>?) {
                Log.d("puze", "onResponse")
                Log.d("puze", response?.code().toString())
                if (response?.code() == 200) {
                    progressDialog.dismiss()
                    startActivity(intent)
                    finish()
//                    } else if (response?.code() == 204) {
//                        progressDialog.dismiss()
//                        Toast.makeText(this@SplashActivity, "이미 등록 되어있는 호스트입니다 ... ", Toast.LENGTH_SHORT).show()
                } else {
                    progressDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<Schema.History>?, t: Throwable?) {
                Log.d("puze", "onFailure")
                progressDialog.dismiss()
            }
        })
    }

    fun bluetooth(){

        if (!bt.isBluetoothEnabled()) {
            bt.enable();
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER)
                bt.autoConnect("PIPE")
            }
        }

        BluetoothAdapter.getDefaultAdapter()
        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(application, "블루투스를 켜주세요", Toast.LENGTH_SHORT).show()
        }

        bt.setBluetoothConnectionListener(object : BluetoothSPP.BluetoothConnectionListener {
            override fun onDeviceConnected(name: String, address: String) {
                Toast.makeText(application, "연결되었습니다", Toast.LENGTH_SHORT).show()
                txt_bluetooth.text = "On"
            }

            override fun onDeviceDisconnected() {
                Toast.makeText(application, "연결이끊겼습니다", Toast.LENGTH_SHORT).show()
                txt_bluetooth.text = "Off"
            }

            override fun onDeviceConnectionFailed() {
                Toast.makeText(application, "연결에 실패하였습니다", Toast.LENGTH_SHORT).show()
                txt_bluetooth.text = "Fail"
            }
        })

        bt.setBluetoothStateListener(object: BluetoothSPP.BluetoothStateListener{
            override fun onServiceStateChanged(state: Int) {
                if(state == BluetoothState.STATE_CONNECTED)
                    Log.d("state: ", state.toString())
                // Do something when successfully connected
                else if(state == BluetoothState.STATE_CONNECTING)
                    Log.d("state: ", state.toString())
                // Do something while connecting
                else if(state == BluetoothState.STATE_LISTEN)
                    Log.d("state: ", state.toString())
                // Do something when device is waiting for connection
                else if(state == BluetoothState.STATE_NONE)
                    Log.d("state: ", state.toString())
                // Do something when device don't have any connection
            }
        })
        bt.setAutoConnectionListener(object : BluetoothSPP.AutoConnectionListener {
            override fun onNewConnection(name: String, address: String) {
                bt.connect("00:21:13:00:E3:F8")
                Log.d("autolistener", "new connection")
            }

            override fun onAutoConnectionStarted() {
                bt.connect("00:21:13:00:E3:F8")
                Log.d("autolistener", "start")
            }
        })

        bt.setOnDataReceivedListener { data, message ->
            var string: String = ""
            for(i in data){
                Log.d("data : ",i.toString())
                string += i
            }
            pipeEnergy = string.toInt()
            energy = pipeEnergy as Int * 2
        }
    }

    fun retrofitSetting() {
        var retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://localhost:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        retrofitService = retrofit.create(RetrofitService::class.java)
    }

    fun progressDialogSetting(){
        progressDialog = ProgressDialog(this@MainActivity)
        progressDialog.setMessage("서버 작업 처리 중입니다 . . .")
        progressDialog.show()
    }
}

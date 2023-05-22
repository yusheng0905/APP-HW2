package com.example.hw2

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var textView2 = findViewById<TextView>(R.id.textView2)
        textView2.movementMethod= ScrollingMovementMethod.getInstance()

        //[POST]URL取得API Token
        val client0 = OkHttpClient()
        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", "s0853012-426d5633-231f-4d49")
            .add("client_secret", "ff6c06e5-abf7-476b-8116-3b0cd9945955")
            .build()
        val request0 = Request.Builder()
            .url("https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token")
            .header("content-type", "application/x-www-form-urlencoded")
            .post(body)
            .build()
        client0.newCall(request0).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) = if (response.isSuccessful) {
                val responseBody0 = response.body?.string()
                println(responseBody0)

                //儲存Token
                val jsonObject0 = JSONObject(responseBody0)
                val token = jsonObject0.get("access_token")

                //查詢車站代號
                val button = findViewById<Button>(R.id.button)
                button.setOnClickListener{
                    val intent1 = Intent();
                    intent1.setAction(Intent.ACTION_VIEW);
                    intent1.setData(Uri.parse("http://web.archive.org/web/20230321233115/https://www.railway.gov.tw/tra-tip-web/tip/tip001/tip111/view"));
                    startActivity(intent1);
                }


                val button2 = findViewById<Button>(R.id.button2)
                button2.setOnClickListener{
                    //輸入欲查詢之車站代號
                    val stationNumber = findViewById<TextView>(R.id.editTextNumber).text
                    val client = OkHttpClient()
                    //[GET]URL
                    val url = "https://tdx.transportdata.tw/api/basic/v3/Rail/TRA/StationLiveBoard/Station/"+stationNumber+"?%24top=30&%24format=JSON"
                    val request = Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer "+ token)
                        .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                        }
                        override fun onResponse(call: Call, response: Response) = if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            println(responseBody)

                            val jsonObject = JSONObject(responseBody)

                            textView2.text = ""

                            //取得查詢時間
                            val updateTime = jsonObject.get("UpdateTime")
                            var dataOfTime = "更新時間:"+updateTime.toString().substring(0,10)+" "+updateTime.toString().substring(11,19)
                            textView2.append(dataOfTime)

                            //取得StationLiveBoards資料
                            val boardsArray = jsonObject.getJSONArray("StationLiveBoards")

                            //若無資料
                            if (boardsArray.length()==0) {textView2.append("\n\n_____________________\n\n"+"查無資料")}


                            //若有資料
                            else {
                                //取得StationLiveBoards的資料內容
                                for (i in 0 until boardsArray.length()) {
                                    val boardsObject = boardsArray.getJSONObject(i)

                                    //取得boardsObject結構
                                    val trainNo = boardsObject.get("TrainNo")
                                    val trainType =
                                        boardsObject.getJSONObject("TrainTypeName").get("Zh_tw")
                                    var direction = boardsObject.get("Direction")
                                    if (direction == 0) {
                                        direction = "北上"
                                    } else {
                                        direction = "南下"
                                    }

                                    val arrTime = boardsObject.get("ScheduleArrivalTime")
                                    val depTime = boardsObject.get("ScheduleDepartureTime")

                                    var delayTime = boardsObject.get("DelayTime")
                                    if (delayTime == 0) {
                                        delayTime = "準點"
                                    } else {
                                        delayTime = "誤點" + delayTime + "分"
                                    }

                                    val endStation =
                                        boardsObject.getJSONObject("EndingStationName").get("Zh_tw")

                                    val data =
                                        "車次:" + trainNo + "\n車種:" + trainType + "\n方向:" + direction + "\n到站時間:" + arrTime + "\n離站時間:" + depTime + "\n狀態:" + delayTime + "\n終點站:" + endStation

                                    //直接將內容回傳給id名稱為textView2
                                    textView2.append("\n\n_____________________\n\n" + data)
                                }
                            }


                            //取得回應回來內容
                            runOnUiThread {
                                println(textView2.text)
                            }
                        } else
                        {
                            println("Request failed")
                            runOnUiThread {
                                //直接將內容回傳給id名稱為textView2
                                textView2.text = "資料錯誤"
                            }

                        }
                    })
                }
            } else {
                println("Request failed")
                runOnUiThread {
                    //直接將內容回傳給id名稱為textView2
                    textView2.text = "驗證錯誤"
                }
            }
        })
    }
}
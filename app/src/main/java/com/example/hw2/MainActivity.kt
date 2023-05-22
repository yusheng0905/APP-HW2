package com.example.hw2

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
                            var dataOfAll = String()

                            //取得查詢時間
                            val updateTime = jsonObject.get("UpdateTime")
                            var dataOfTime = "更新時間:"+updateTime.toString().substring(0,10)+" "+updateTime.toString().substring(11,19)

                            //取得StationLiveBoards資料
                            val boardsArray = jsonObject.getJSONArray("StationLiveBoards")

                            if (boardsArray.length()==0) {dataOfAll = dataOfTime+"\n\n_____________________\n\n"+"查無資料"}


                            //若只有一筆資料
                            else if (boardsArray.length()==1) {
                                //取得StationLiveBoards的資料內容
                                val boardsObject0 = boardsArray.getJSONObject(0)

                                //取得boardsObject0結構
                                val trainNo0 = boardsObject0.get("TrainNo")
                                val trainType0 = boardsObject0.getJSONObject("TrainTypeName").get("Zh_tw")
                                var direction0 = boardsObject0.get("Direction")
                                if (direction0==0){direction0 = "北上"} else {direction0 = "南下"}

                                val arrTime0 = boardsObject0.get("ScheduleArrivalTime")
                                val depTime0 = boardsObject0.get("ScheduleDepartureTime")

                                var delayTime0 = boardsObject0.get("DelayTime")
                                if (delayTime0==0){delayTime0 = "準點"} else {delayTime0 = "誤點"+delayTime0+"分"}

                                val endStation0 = boardsObject0.getJSONObject("EndingStationName").get("Zh_tw")

                                val data0 = "車次:"+trainNo0+"\n車種:"+trainType0+"\n方向:"+direction0+"\n到站時間:"+arrTime0+"\n離站時間:"+depTime0+"\n狀態:"+delayTime0+"\n終點站:"+endStation0

                                dataOfAll = dataOfTime+"\n\n_____________________\n\n"+data0
                            }




                            //2筆資料以上
                            else {
                                //取得StationLiveBoards的資料內容
                                val boardsObject0 = boardsArray.getJSONObject(0)
                                val boardsObject1 = boardsArray.getJSONObject(1)

                                //取得boardsObject0結構
                                val trainNo0 = boardsObject0.get("TrainNo")
                                val trainType0 = boardsObject0.getJSONObject("TrainTypeName").get("Zh_tw")
                                var direction0 = boardsObject0.get("Direction")
                                if (direction0 == 0) {
                                    direction0 = "北上"
                                } else {
                                    direction0 = "南下"
                                }

                                val arrTime0 = boardsObject0.get("ScheduleArrivalTime")
                                val depTime0 = boardsObject0.get("ScheduleDepartureTime")

                                var delayTime0 = boardsObject0.get("DelayTime")
                                if (delayTime0 == 0) {
                                    delayTime0 = "準點"
                                } else {
                                    delayTime0 = "誤點" + delayTime0 + "分"
                                }

                                val endStation0 =
                                    boardsObject0.getJSONObject("EndingStationName").get("Zh_tw")

                                val data0 =
                                    "車次:" + trainNo0 + "\n車種:" + trainType0 + "\n方向:" + direction0 + "\n到站時間:" + arrTime0 + "\n離站時間:" + depTime0 + "\n狀態:" + delayTime0 + "\n終點站:" + endStation0

                                //取得boardsObject1結構
                                val trainNo1 = boardsObject1.get("TrainNo")
                                val trainType1 = boardsObject1.getJSONObject("TrainTypeName").get("Zh_tw")
                                var direction1 = boardsObject1.get("Direction")
                                if (direction1 == 0) {
                                    direction1 = "北上"
                                } else {
                                    direction1 = "南下"
                                }

                                val arrTime1 = boardsObject1.get("ScheduleArrivalTime")
                                val depTime1 = boardsObject1.get("ScheduleDepartureTime")

                                var delayTime1 = boardsObject1.get("DelayTime")
                                if (delayTime1 == 0) {
                                    delayTime1 = "準點"
                                } else {
                                    delayTime1 = "誤點" + delayTime1 + "分"
                                }

                                val endStation1 =
                                    boardsObject1.getJSONObject("EndingStationName").get("Zh_tw")

                                val data1 =
                                    "車次:" + trainNo1 + "\n車種:" + trainType1 + "\n方向:" + direction1 + "\n到站時間:" + arrTime1 + "\n離站時間:" + depTime1 + "\n狀態:" + delayTime1 + "\n終點站:" + endStation1

                                dataOfAll = dataOfTime+"\n\n_____________________\n\n"+data0+"\n\n_____________________\n\n"+data1

                            }


                            //取得回應回來內容
                            runOnUiThread {
                                //直接將內容回傳給id名稱為textView2
                                findViewById<TextView>(R.id.textView2).text = dataOfAll



                            }
                        } else
                        {
                            println("Request failed")
                            runOnUiThread {
                                //直接將內容回傳給id名稱為textView2
                                findViewById<TextView>(R.id.textView2).text = "資料錯誤"
                            }

                        }
                    })
                }
            } else {
                println("Request failed")
                runOnUiThread {
                    //直接將內容回傳給id名稱為textView2
                    findViewById<TextView>(R.id.textView2).text = "驗證錯誤"
                }
            }
        })







    }
}
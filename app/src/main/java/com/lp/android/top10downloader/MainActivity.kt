package com.lp.android.top10downloader

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")
        val downloadData = DownloadData()
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
        Log.d(TAG, "onCreate: done")

    }
    companion object {
        private class DownloadData : AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"
            override fun doInBackground(vararg URL: String?): String {
                Log.d(TAG, "onInBackground: starts with ${URL[0]}")
                val rssFeed = downloadXML(URL[0])
                if(rssFeed.isEmpty()){
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                Log.d(TAG, "onPostExecute: parameter is $result")

            }

            private fun downloadXML(urlPath: String?): String{
                return URL(urlPath).readText()
            }
        }
    }

}
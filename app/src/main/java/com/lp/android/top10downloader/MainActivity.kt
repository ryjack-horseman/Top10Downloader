package com.lp.android.top10downloader

import FeedAdapter
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ListView
import java.net.URL
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val downloadData by lazy {DownloadData(this, xmlListView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=25/xml")
        Log.d(TAG, "onCreate: done")

    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData.cancel(true)
    }
    companion object {
        private const val TAG = "DownloadData"
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {

            var ctx : Context by Delegates.notNull()
            var view : ListView by Delegates.notNull()

            init {
                ctx = context
                view = listView
            }
            override fun doInBackground(vararg URL: String?): String {
                Log.d(TAG, "onInBackground: starts with ${URL[0]}")
                val rssFeed = downloadXML(URL[0])
                if(rssFeed.isEmpty()){
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

                //val arrayAdapter = ArrayAdapter(ctx, R.layout.list_item, parseApplications.applications)
               // view.adapter = arrayAdapter

                val feedAdapter = FeedAdapter(ctx, R.layout.list_record, parseApplications.applications)
                view.adapter = feedAdapter
            }

            private fun downloadXML(urlPath: String?): String{
                return URL(urlPath).readText()
            }
        }
    }

}
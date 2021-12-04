package com.lp.android.top10downloader

import FeedAdapter
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.ListView
import java.net.URL
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var downloadData: DownloadData? = null
    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10
    private var feedCachedUrl = "INVALIDATED"

    private val CURR_LIST = "curr_list"
    private val FEED_LIMIT = "feed_limit"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    //could remove onResume and then load values out of on create instead of on RestoreInstanceState
    override fun onResume() {
        super.onResume()
        downloadUrl(feedUrl.format(feedLimit))
    }

    private fun downloadUrl(feedUrl: String){
        if(feedUrl != feedCachedUrl){
            Log.d(TAG, "downloadUrl starting Asynctask")
            downloadData = DownloadData(this, xmlListView)
            downloadData?.execute(feedUrl)
            feedCachedUrl = feedUrl
            Log.d(TAG, "downloadUrl done")
        }else{
            Log.d(TAG,"dowloadURL - URL not changed")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feeds_menu, menu)
        if(feedLimit == 10){
            menu?.findItem(R.id.mnu10)?.isChecked = true
        }else{
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mnuFree -> {
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            }
            R.id.mnuPaid -> {
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            }
            R.id.mnuSongs -> {
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            }
            R.id.mnu10, R.id.mnu25 -> {
                if(!item.isChecked){
                    item.isChecked = true
                    feedLimit = 35  - feedLimit
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit")
                }else{
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit unchanged")
                }
            }
            R.id.mnuRefresh -> {
                feedCachedUrl = "INVALIDATED"
            }
            else ->
                return super.onOptionsItemSelected(item)
        }

        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURR_LIST, feedUrl)
        outState.putInt(FEED_LIMIT, feedLimit)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        feedLimit = savedInstanceState.getInt(FEED_LIMIT)
        feedUrl = savedInstanceState.getString(CURR_LIST, "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml")
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
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
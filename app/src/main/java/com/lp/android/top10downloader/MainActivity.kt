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
    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/%s/limit=%d/xml"
    private var currList: String = "topfreeapplications"
    private var feedLimit = 10
    private val CURR_LIST = "curr_list"
    private val FEED_LIMIT = "feed_limit"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Log.d(TAG, "onCreate: done")

    }

    override fun onResume() {
        super.onResume()
        downloadUrl(feedUrl.format(currList,feedLimit))
    }

    private fun downloadUrl(feedUrl: String){
        Log.d(TAG, "downloadUrl starting Asynctask")
        downloadData = DownloadData(this, xmlListView)
        downloadData?.execute(feedUrl)
        Log.d(TAG, "downloadUrl done")
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
        var reload: Boolean = false
        when (item.itemId){
            R.id.mnuFree -> {
                if(!currList.equals("topfreeapplications")){
                    reload = true
                }
                currList = "topfreeapplications"
            }
            R.id.mnuPaid -> {
                if(!currList.equals("toppaidapplications")){
                    reload = true
                }
                currList = "toppaidapplications"
            }

            R.id.mnuSongs -> {
                if(!currList.equals("topsongs")){
                    reload = true
                }
                currList = "topsongs"
            }
            R.id.mnu10, R.id.mnu25 -> {
                if(!item.isChecked){
                    item.isChecked = true
                    reload = true
                    feedLimit = 35  - feedLimit
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit to $feedLimit")
                }else{
                    Log.d(TAG, "onOptionsItemSelected: ${item.title} setting feedLimit unchanged")
                }
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        if(reload) {
            downloadUrl(feedUrl.format(currList, feedLimit))
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CURR_LIST, currList)
        outState.putInt(FEED_LIMIT, feedLimit)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        feedLimit = savedInstanceState.getInt(FEED_LIMIT)
        currList = savedInstanceState.getString(CURR_LIST, "topfreeapplications")
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
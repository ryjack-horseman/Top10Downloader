package com.lp.android.top10downloader

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.lang.Exception

private const val TAG = "ParseApplications"
class ParseApplications {

    val applications = ArrayList<FeedEntry>()

    fun parse(xmlData: String): Boolean {
        Log.d(TAG, "parse called with $xmlData")
        var status = true
        var inEntry = false
        var textValue = ""

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader())
            var eventType = xpp.eventType
            var currentRecord = FeedEntry()
            while(eventType != XmlPullParser.END_DOCUMENT){
                val tagName = xpp.name?.lowercase()
                when(eventType){
                    XmlPullParser.START_TAG -> {
                        //Log.d(TAG, "parse: Starting tag for $tagName")
                        if(tagName == "entry"){
                            inEntry = true
                        }
                    }
                    XmlPullParser.TEXT -> textValue = xpp.text

                    XmlPullParser.END_TAG -> {
                       // Log.d(TAG, "parse: Ending tag for $tagName")
                        if(inEntry){
                            when(tagName){
                                //tagnames are always lowercase
                                "entry" -> {
                                    applications.add(currentRecord)
                                    inEntry = false
                                    currentRecord = FeedEntry()
                                }
                                "name" -> currentRecord.name = textValue
                                "artist" -> currentRecord.artist = textValue
                                "releaseDate" -> currentRecord.releaseDate = textValue
                                "summary" -> currentRecord.summary = textValue
                                "image" -> currentRecord.imageURL = textValue
                            }
                        }
                    }
                }
                //get next item
                eventType = xpp.next()
            }

            //log printout
          //  for(app in applications){
             //   Log.d(TAG, "**************")
             //   Log.d(TAG, app.toString())
          //  }

        }catch (e: Exception){
            e.printStackTrace()
            status = false
        }
        return status
    }
}
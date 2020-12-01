package course.examples.ui.listlayout

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class WikiHowSearchTask internal constructor(private val mAppContext: Context) : Thread() {
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var rawHtml : String? = null

    private val titleRegex = Regex("""<div class="result_title">(.*)</div>""")
    private val hrefRegex = Regex("""<a class="result_link" href=(https://www.wikihow.com/.*) >""")

    // Variables in ListViewActivity //
    private var mWikiDataMap : MutableMap<String, String>? = null
    private var listView : ListView? = null
    private var hobbyArrayList : ArrayList<String>? = null
    private var mWikiToast : Toast? = null
    ///////////////////////////////////

    private var searchQuery : String? = null

    internal fun setListViewActivityVariables(mWikiDataMapIn : MutableMap<String, String>?, listViewIn : ListView,
                                              hobbyArrayListIn : ArrayList<String>, mWikiToastIn : Toast?, searchQueryIn : String) : WikiHowSearchTask {
        this.mWikiDataMap = mWikiDataMapIn
        this.listView = listViewIn
        this.hobbyArrayList = hobbyArrayListIn
        this.mWikiToast = mWikiToastIn
        this.searchQuery = searchQueryIn
        return this
    }


    override fun run() {
        if ( mWikiDataMap == null || listView == null || hobbyArrayList == null || mWikiToast == null || searchQuery == null ) return

        rawHtml = httpRequestData("https://www.wikihow.com/wikiHowTo?search=$searchQuery")

        if ( rawHtml != null ) {

            // Removes <b> and </b> from raw html
            rawHtml = rawHtml!!.replace("<b>", "")
            rawHtml = rawHtml!!.replace("</b>", "")

            val titleMatches = titleRegex.findAll(rawHtml!!)
            val hrefMatches = hrefRegex.findAll(rawHtml!!)

            val titleList = titleMatches.map { it.groups[1]?.value }.toList()
            val hrefList = hrefMatches.map { it.groups[1]?.value }.toList()

            var currentTitle : String? = null
            var currentHref : String? = null

            mWikiDataMap!!.clear()

            for (i in titleList.indices) {

                // This block protects from an out of bounds exception for whatever reason if hrefList is unexpectedly smaller than titleList //
                currentTitle = titleList[i]
                if ( i < hrefList.size ) {
                    currentHref = hrefList[i]
                }
                else {
                    currentHref = null
                }

                // This block filters titles that don't have "How to " and hrefs that don't have atleast one "/"
                if ( currentTitle != null && currentHref != null && currentTitle.contains("How to ") && currentHref.contains("/")) {

                    val parsedTitle = currentTitle.substring("How to ".length)
                    val parsedHref = currentHref.substring(currentHref.lastIndexOf("/") + 1).replace("-", " ")

                    // This block checks that the title matches the href
                    if ( parsedTitle == parsedHref ) {
                        mHandler.post { mWikiDataMap!!.put(currentTitle, currentHref)}
                    }
                }

            }

            mHandler.post {
                hobbyArrayList!!.clear()
                hobbyArrayList!!.addAll(ArrayList(mWikiDataMap!!.keys))
                (listView!!.adapter as ArrayAdapter<String>).notifyDataSetChanged()
            }

        }
        else {
            println("No Data was received from WikiHow!!!")
            mHandler.post {
                mWikiToast!!.setText("There was an error receiving data from WikiHow.")
                mWikiToast!!.show()
            }
        }

    }

    private fun httpRequestData(url : String): String? {
        var data: String? = null
        var httpUrlConnection: HttpURLConnection? = null

        try {
            httpUrlConnection = URL(url)
                    .openConnection() as HttpURLConnection

            val inputStream = BufferedInputStream(
                    httpUrlConnection.inputStream
            )
            data = readStream(inputStream)

        } catch (exception: MalformedURLException) {
            println("MalformedURLException")
        } catch (exception: IOException) {
            println("Network IOException")
            println(exception.toString())
        } finally {
            httpUrlConnection?.disconnect()
        }
        return data
    }


    private fun readStream(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val data = StringBuilder()
        val sep = System.getProperty("line.separator")
        try {
            reader.forEachLine {
                data.append(it + sep)
            }
        } catch (e: IOException) {
            println(e.toString())
        } finally {
            try {
                reader.close()
            } catch (e: IOException) {
                println(e.toString())
            }
        }
        return data.toString()
    }
}
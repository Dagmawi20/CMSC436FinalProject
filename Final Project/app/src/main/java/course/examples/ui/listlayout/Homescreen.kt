package course.examples.ui.listlayout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView

class Homescreen: Activity()  {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homescreen)

        var mTracker = findViewById<View>(R.id.tracker) as TextView
        mTracker.setOnClickListener {
            val trackerIntent = Intent(
                    this,
                    HobbyManagerActivity::class.java
            )
            startActivity(trackerIntent)
        }

        var mSearcher = findViewById<View>(R.id.searcher) as TextView
        mSearcher.setOnClickListener {
            val searcherIntent = Intent(
                    this,
                    HobbySearchActivity::class.java
            )
            startActivity(searcherIntent)
        }
    }
}
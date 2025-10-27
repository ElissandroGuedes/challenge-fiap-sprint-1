package br.com.fiap.challengefiap

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject

class CampaignDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campaign_detail)

        val title = intent.getStringExtra("title") ?: "Campanha"
        val body = intent.getStringExtra("body") ?: ""
        val actionsJason = intent.getStringExtra("actions") ?: "[]"
        val actionUrlJason = intent.getStringExtra("actionUrls") ?: "{}"

        val titleView = findViewById<TextView>(R.id.campaignTitle)
        val bodyView = findViewById<TextView>(R.id.campaignBody)
        val layoutButtons = findViewById<LinearLayout>(R.id.layoutButtons)

        titleView.text = title
        bodyView.text = body

        val actions = JSONArray(actionsJason)
        val actionUrls = JSONObject(actionUrlJason)


        for (i in 0 until actions.length()) {
            val action = actions.getJSONObject(i)
            val actionKey = action.optString("action")
            val buttonLabel = action.optString("title", "Ação")
            val url = actionUrls.optString(actionKey, "")

            val button = MaterialButton(this).apply {
                text = buttonLabel
                setOnClickListener {
                    if (url.isNotEmpty()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@CampaignDetailActivity,
                                "URL inválida",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@CampaignDetailActivity,
                            "Nenhum link disponível",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            layoutButtons.addView(button)
        }

    }

}

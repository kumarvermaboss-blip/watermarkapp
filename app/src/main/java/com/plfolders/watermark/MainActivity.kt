package com.plfolders.watermark
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

class MainActivity : AppCompatActivity() {
    val WATERMARK_TEXT = "@PLfolders (Tg Search)"
    val INPUT_FOLDER = "/storage/emulated/0/best"
    val OUTPUT_FOLDER = "/storage/emulated/0/best_WM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40,40,40,40) }
        val title = TextView(this).apply { text = "PLfolders Watermark\nInput: $INPUT_FOLDER\nOutput: $OUTPUT_FOLDER"; textSize = 16f }
        val log = TextView(this).apply { text = "Ready...\n"; textSize = 13f }
        val btn = Button(this).apply { text = "START WATERMARKING" }
        layout.addView(title); layout.addView(btn); layout.addView(log)
        scroll.addView(layout)
        setContentView(scroll)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        btn.setOnClickListener {
            val inDir = File(INPUT_FOLDER)
            val outDir = File(OUTPUT_FOLDER)
            outDir.mkdirs()
            if (!inDir.exists()) { log.append("\nFolder nahi mila: $INPUT_FOLDER"); return@setOnClickListener }
            val videos = inDir.listFiles { f -> f.extension.lowercase() in listOf("mp4","mkv","mov") }?: emptyArray()
            log.append("\nTotal ${videos.size} videos\n")
            Thread {
                videos.forEachIndexed { i, video ->
                    runOnUiThread { log.append("\n[${i+1}/${videos.size}] ${video.name}") }
                    val out = File(outDir, "WM_${video.name}")
                    val filter = "drawtext=text='$WATERMARK_TEXT':fontcolor=white@1.0:fontsize=20:x=if(lt(mod(t*60\\,2*(w-tw))\\,w-tw)\\,mod(t*60\\,2*(w-tw))\\,2*(w-tw)-mod(t*60\\,2*(w-tw))):y=if(lt(mod(t*45\\,2*(h-th))\\,h-th)\\,mod(t*45\\,2*(h-th))\\,2*(h-th)-mod(t*45\\,2*(h-th))):box=0"
                    FFmpegKit.execute("-y -i ${video.absolutePath} -vf $filter -c:v libx264 -preset ultrafast -crf 23 -c:a copy ${out.absolutePath}")
                }
                runOnUiThread { log.append("\n\nDone! Check: $OUTPUT_FOLDER") }
            }.start()
        }
    }
}
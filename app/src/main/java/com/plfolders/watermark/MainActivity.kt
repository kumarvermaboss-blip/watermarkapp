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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40,40,40,40) }
        val title = TextView(this).apply { text = "PLfolders Watermark\nInput: /storage/emulated/0/best\nOutput: /storage/emulated/0/best_WM"; textSize = 16f }
        val log = TextView(this).apply { text = "Ready...\n"; textSize = 13f }
        val btn = Button(this).apply { text = "START WATERMARKING" }
        layout.addView(title); layout.addView(btn); layout.addView(log)
        scroll.addView(layout)
        setContentView(scroll)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        btn.setOnClickListener {
            val inDir = File("/storage/emulated/0/best")
            val outDir = File("/storage/emulated/0/best_WM")
            outDir.mkdirs()
            if (!inDir.exists()) { log.append("\nFolder nahi mila: /storage/emulated/0/best"); return@setOnClickListener }
            val videos = inDir.listFiles { f -> f.name.endsWith(".mp4") || f.name.endsWith(".mkv") } ?: emptyArray()
            log.append("\nTotal ${videos.size} videos\n")
            Thread {
                for (video in videos) {
                    runOnUiThread { log.append("\nProcessing: ${video.name}") }
                    val out = File(outDir, "WM_${video.name}")
                    val cmd = "-y -i ${video.absolutePath} -vf drawtext=text='@PLfolders (Tg Search)':fontcolor=white:fontsize=24:x=(w-tw)/2:y=(h-th)/2+100*sin(t):box=1:boxcolor=black@0.5 -c:v libx264 -preset ultrafast -crf 23 -c:a copy ${out.absolutePath}"
                    FFmpegKit.execute(cmd)
                }
                runOnUiThread { log.append("\n\nDone!") }
            }.start()
        }
    }
}
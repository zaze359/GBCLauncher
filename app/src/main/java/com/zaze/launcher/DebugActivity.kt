package com.zaze.launcher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zaze.utils.ZActivityUtil
import kotlinx.android.synthetic.main.activity_debug.*

/**
 * Description :
 * @author : ZAZE
 * @version : 2018-05-22 - 20:21
 */
class DebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        start_launcher_btn.setOnClickListener {
            ZActivityUtil.startActivity(this, LauncherActivity::class.java)
        }
    }

}
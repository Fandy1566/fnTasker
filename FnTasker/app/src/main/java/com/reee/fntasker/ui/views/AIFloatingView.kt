package com.reee.fntasker.ui.views

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.reee.fntasker.R
import com.reee.fntasker.autoClicker.services.AutoClickerService
import com.reee.fntasker.model.EventData
import com.reee.fntasker.model.EventType
import com.reee.fntasker.utils.Constant
import com.reee.fntasker.utils.CustomDialog
import com.reee.fntasker.utils.TouchAndDragListener
import java.io.File
import java.io.FileOutputStream

class AIFloatingView : Service() {
    private lateinit var mWindowManager : WindowManager
    private lateinit var autoClickerMenu: View
    private lateinit var clWidget: ConstraintLayout
    private lateinit var rlPointer: RelativeLayout
    private lateinit var ivPlay: ImageView
    private lateinit var ivAdd: ImageView
    private lateinit var ivMin: ImageView
    private lateinit var ivSave: ImageView
    private lateinit var ivSettings: ImageView
    private lateinit var ivClose: ImageView
    private val location = IntArray(2)
    private var LAYOUT_FLAG = 0 // getting the type of layout parameter
    private val eventList : MutableList<EventData> = mutableListOf()

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
//        val filter = IntentFilter(AutoClickerService.ACTION_AUTO_SERVICE_COMPLETE)
//        registerReceiver(autoServiceCompletionReceiver, filter)

        // getting the widget layout from xml using layout inflater ...dynamic layout use inflater
        autoClickerMenu = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        // find view
        // autoClickerMenu
        clWidget = autoClickerMenu.findViewById(R.id.lfw_cl_widget)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Application overlay windows are displayed above all activity windows but below critical system windows like the status bar or IME
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // These windows are normally placed above all applications, but behind the status bar
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, // width
            WindowManager.LayoutParams.WRAP_CONTENT, // height
            LAYOUT_FLAG, // type
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, // flags
            PixelFormat.TRANSLUCENT // format
        )
        // specify the view position
        params.gravity = Gravity.CENTER or Gravity.LEFT
        params.x = 5
        params.y = 50

        // getting image view id from layout
        ivPlay = autoClickerMenu.findViewById(R.id.lfw_iv_play)
        ivAdd = autoClickerMenu.findViewById(R.id.lfw_iv_add)
        ivMin = autoClickerMenu.findViewById(R.id.lfw_iv_min)
        ivSave = autoClickerMenu.findViewById(R.id.lfw_iv_save)
        ivSettings = autoClickerMenu.findViewById(R.id.lfw_iv_settings)
        ivClose = autoClickerMenu.findViewById(R.id.lfw_iv_close)

        // getting windows services and adding the floating view to it
        mWindowManager = getSystemService(Service.WINDOW_SERVICE) as WindowManager
        mWindowManager.addView(autoClickerMenu, params)

        clWidget.setOnTouchListener(TouchAndDragListener(params, 0, { }, { mWindowManager.updateViewLayout(autoClickerMenu, params) }))
        ivPlay.setOnTouchListener(TouchAndDragListener(params, 0, { onClickPlay() }, { mWindowManager.updateViewLayout(autoClickerMenu, params) }))
        ivAdd.setOnTouchListener(TouchAndDragListener(params, 0, { onClickAdd() }, { mWindowManager.updateViewLayout(autoClickerMenu, params) }))
        ivMin.setOnTouchListener(TouchAndDragListener(params, 0, { onClickMin() }, { mWindowManager.updateViewLayout(autoClickerMenu, params) }))
        ivSave.setOnTouchListener(TouchAndDragListener(params, 0, { onClickSave("test",eventList) }, { mWindowManager.updateViewLayout(autoClickerMenu, params) }))
        ivSettings.setOnTouchListener(TouchAndDragListener(params, 0, { onClickSettings() }, { mWindowManager.updateViewLayout(autoClickerMenu, params) }))
        ivClose.setOnTouchListener(TouchAndDragListener(params, 0, { onClickClose() }, { mWindowManager.updateViewLayout(autoClickerMenu, params) }))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (autoClickerMenu != null || pointers != null) {
            mWindowManager.removeView(autoClickerMenu)
            for (i in pointerWindowManager.indices) {
                pointerWindowManager[i].removeView(pointers[i])
            }
        }
        unregisterReceiver(autoServiceCompletionReceiver)
    }

    private fun itsAClick() {
        Log.d("TAG", "itsAClick: click")
    }

    private fun togglePointersVisibility() {
        try {
            if (pointers.isNotEmpty()) {
                val currentVisibility = pointers[0].visibility
                for (pointer in pointers) {
                    if (currentVisibility == View.VISIBLE) {
                        pointer.visibility = View.INVISIBLE
                    } else {
                        pointer.visibility = View.VISIBLE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setTouchable(view: View, touchable: Boolean) {
        try {
            val pointerWManager = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val params = view.layoutParams as WindowManager.LayoutParams
            if (touchable) {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
            } else {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }
            pointerWManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toggleAllPointerTouchable() {
        val touchable = isTouchable(pointers[0])
        for (pointer in pointers) {
            setTouchable(pointer, !touchable)
        }
    }

    private fun isTouchable(view: View): Boolean {
        val params = view.layoutParams as WindowManager.LayoutParams
        return params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE == 0
    }

    private val pointerWindowManager = ArrayList<WindowManager>()
    private val pointers = ArrayList<View>()

    private fun addPointer() {
        try {
            val pointer = LayoutInflater.from(this).inflate(R.layout.layout_pointer, null)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER or Gravity.CENTER
            val windowManager = getSystemService(Service.WINDOW_SERVICE) as WindowManager
            windowManager.addView(pointer, params)
            pointerWindowManager.add(windowManager)
            pointers.add(pointer)

            if (pointers.isNotEmpty() && pointerWindowManager.isNotEmpty()) {
                val currentIndex = pointers.size-1
                val currentPointer = pointers[currentIndex]
                val currentWindowManager = pointerWindowManager[currentIndex]
                rlPointer = currentPointer.findViewById(R.id.lp_rl_pointer)
                val tvNumber = currentPointer.findViewById<TextView>(R.id.lp_tv_number)
                tvNumber.text = (pointers.size).toString()

                pointer.getLocationOnScreen(location)
                val eventParams = HashMap<String, Any>()
                eventParams["to"] = Point(location[0], location[1])
                eventParams["delay"] = 100
                eventParams["hold"] = 200
                eventParams["loop"] = 1

                fun pointerOnClick() {
                    val pointerDialog = CustomDialog(this, R.layout.dialog_pointer_setting)
                    pointerDialog.findViewById<TextView>(R.id.dps_tv_pointer_id).setText((currentIndex + 1).toString())
                    pointerDialog.findViewById<EditText>(R.id.dps_et_delay).setText(eventParams["delay"].toString())
                    pointerDialog.findViewById<EditText>(R.id.dps_et_loop).setText(eventParams["loop"].toString())
                    pointerDialog.findViewById<EditText>(R.id.dps_et_hold).setText(eventParams["hold"].toString())

                    val tvDelete : TextView = pointerDialog.findViewById(R.id.dps_tv_delete)
                    tvDelete.setOnClickListener(View.OnClickListener {
                        pointerDialog.dismiss()
                        removePointer(currentIndex)
                    })

                    val tvCancel : TextView = pointerDialog.findViewById(R.id.dps_tv_cancel)
                    tvCancel.setOnClickListener(View.OnClickListener {
                        pointerDialog.dismiss()
                    })

                    val tvSave : TextView = pointerDialog.findViewById(R.id.dps_tv_save)
                    tvSave.setOnClickListener(View.OnClickListener {
                        pointerDialog.dismiss()
                        val delayText = pointerDialog.findViewById<EditText>(R.id.dps_et_delay).text.toString()
                        val delay = delayText.toIntOrNull() ?: 0 // Handle invalid input gracefully, here defaulting to 0
                        eventParams["delay"] = delay

                        val loopText = pointerDialog.findViewById<EditText>(R.id.dps_et_loop).text.toString()
                        val loop = loopText.toIntOrNull() ?: 0 // Handle invalid input gracefully, here defaulting to 0
                        eventParams["loop"] = loop

                        val holdText = pointerDialog.findViewById<EditText>(R.id.dps_et_hold).text.toString()
                        val hold = holdText.toIntOrNull() ?: 0 // Handle invalid input gracefully, here defaulting to 0
                        eventParams["hold"] = hold

                        eventList[currentIndex] = EventData(EventType.CLICK, eventParams)
                    })

                    pointerDialog.show()
                }

                rlPointer.setOnTouchListener(TouchAndDragListener(params, 0, { pointerOnClick() }, { currentWindowManager.updateViewLayout(currentPointer, params) }))

                val eventData = EventData(EventType.CLICK, eventParams)
                eventList.add(eventData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removePointer(index: Int) {
        try {
            if (index < pointerWindowManager.size) {
                val windowManager = pointerWindowManager[index]
                val pointer = pointers[index]
                windowManager.removeView(pointers[index])
                pointerWindowManager.remove(windowManager)
                pointers.remove(pointer)
            }
        } catch (e: IndexOutOfBoundsException) {
            throw e
        }
    }

    private val autoServiceCompletionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            toggleAllPointerTouchable()
        }
    }

    private fun onClickPlay() {
        Log.d("TAG", "onClickPlay: Button clicked")
        val loop = 1
        toggleAllPointerTouchable()
        val eventListBundle = Bundle()
        val intent = Intent(applicationContext, AutoClickerService::class.java)
        intent.putExtra("eventList", eventListBundle)
        intent.putExtra("loop", loop)
        application.startService(intent)
    }

    private fun onClickStop() {
        val intent = Intent(applicationContext, AutoClickerService::class.java)
        application.stopService(intent)
    }

    private fun onClickAdd() {
        addPointer()
    }

    private fun onClickMin() {
        if (pointerWindowManager.isNotEmpty()) {
            removePointer(pointerWindowManager.size - 1)
        }
    }

    private fun onClickSave(filename: String, data: Any) {
        try {
            val gson = Gson()
            val json = gson.toJson(data)

            // Create directory if it doesn't exist
            val directory = File(this.filesDir, Constant.SAVE_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdir()
            }

            val file = File(directory, filename)

            val outputStream: FileOutputStream = FileOutputStream(file)
            outputStream.write(json.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun onClickSettings() {}
    private fun onClickClose() {
        if (eventList.isEmpty()){
            stopSelf()
        } else {
            // set a dialog to save config or cancel
        }
    }
}
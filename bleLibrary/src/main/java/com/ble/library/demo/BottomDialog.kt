package com.ble.library.demo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.ble.library.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * 底部确认窗口
 */
class BottomDialog : BottomSheetDialogFragment() {

    private lateinit var tvDisplayReceiveData: TextView
    private var mDialogCloseListener: OnDialogCloseListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 圆角阴影控制
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.layout_display_ble_receive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 不可取消
        dialog?.setCancelable(false)
        // 不可点击外部取消
        dialog?.setCanceledOnTouchOutside(false)

        val content = arguments?.getString("content")

        tvDisplayReceiveData = view.findViewById(R.id.tvDisplayReceiveData)
        tvDisplayReceiveData.gravity = Gravity.START
        // 配置可拖动
        tvDisplayReceiveData.movementMethod = ScrollingMovementMethod()
        tvDisplayReceiveData.text = content


        val ivBottomDialogClose = view.findViewById<ImageView>(R.id.iv_bottom_dialog_close)
        ivBottomDialogClose.setOnClickListener {
            mDialogCloseListener?.onCloseListener()
            dismiss()
        }
    }

    /**
     * 窗口关闭回调监听
     */
    fun addDialogCloseListener(listener: OnDialogCloseListener) {
        this.mDialogCloseListener = listener
    }

    /**
     * 窗口关闭回调接口
     */
    interface OnDialogCloseListener {
        fun onCloseListener()
    }

    /**
     * 刷新展示数据
     */
    fun setNewData(content: String) {
        tvDisplayReceiveData.text = content
    }
}

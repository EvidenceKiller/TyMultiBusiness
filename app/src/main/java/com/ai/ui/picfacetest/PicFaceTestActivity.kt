package com.ai.ui.picfacetest

import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.adl.service.entity.StudentEntity
import com.bumptech.glide.Glide
import com.ty.bus.multi.R
import com.ty.bus.multi.demo.databinding.ActivityPicFaceTestBinding
import com.ty.bus.multi.demo.databinding.ItemFileInfoBinding
import com.ty.identifty.base.FailType
import com.ty.identifty.base.TyFaceInfo
import com.ty.identifty.stface.SaveBitmapUtil
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.face.FaceDetectorHelper
import com.ty.tybusbase.face.OnFaceCallback
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.ToastUtil
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PicFaceTestActivity : BaseActivity<ActivityPicFaceTestBinding, BaseViewModel>() {

    private var fileList = mutableListOf<String>()
    private var executor: ExecutorService =
        Executors.newSingleThreadExecutor(Executors.defaultThreadFactory())

    override fun initObserver() {

    }

    override fun initData() {
        executor.execute {
            fileList = File(picPath).listFiles()?.filter { it.isFile && (it.absolutePath.endsWith("jpg") || it.absolutePath.endsWith("png")) }?.map { it.absolutePath }
                ?.toMutableList() ?: mutableListOf()
            runOnUiThread {
                mBinding.rv.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        FaceDetectorHelper.getInstance().setFaceCallback(object : OnFaceCallback {
            override fun onFindStudent(user: StudentEntity?) {
                runOnUiThread {
                    Glide.with(this@PicFaceTestActivity).load(user?.userFaceImgUrl)
                        .placeholder(R.mipmap.ic_head_default)
                        .error(R.mipmap.ic_head_default).into(mBinding.civHeader)
                    mBinding.tvName.text = user?.accountName
                }
            }

            override fun onFindUserId(userId: String?, score: Float) {
            }

            override fun onFindFail(info: TyFaceInfo<*>?,type: FailType?) {
                LogUtil.d("onFindFail: ${info?.faceIconData?.size} --type: ${type?.name}")
            }

        })

        mBinding.btnIdentify.setOnClickListener {
            identifyFace()
        }

        mBinding.btnPause.setOnClickListener {
            identifyRunning = false
        }

        mBinding.rv.layoutManager = GridLayoutManager(this, 3)
        mBinding.rv.adapter = object : BaseBindingAdapter<ItemFileInfoBinding, String>(fileList) {
            override fun convert(
                holder: VBViewHolder<ItemFileInfoBinding>,
                item: String
            ) {
                holder.vb.tvFileName.text = item
                holder.vb.root.setOnClickListener {
                    bitmapDic = item
                    mBinding.tvCuPic.text = bitmapDic
                }
            }
        }

        mBinding.tvCuPic.text = bitmapDic
    }

    private var picPath = "/sdcard/zy.sport.cache/resCollect"
//    private var bitmapDic = "/sdcard/zy.sport.cache/resCollect/pic_1.jpg"
    private var bitmapDic = "/sdcard/zy.sport.cache/pic/2026-04-07/2026-04-07_19-25-12_1987760563068407808_小海_0.7567313.jpg"

    private var identifyRunning = false

    private fun identifyFace() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(2, Executors.defaultThreadFactory())
        }
        executor?.execute {
            identifyRunning = true
            val triple =
                SaveBitmapUtil.imageFileToYuv(bitmapDic)
            if (triple == null) {
                ToastUtil.getInstance(this, "图片不存在").show()
                return@execute
            }
            while (identifyRunning) {
                FaceDetectorHelper.getInstance().detectFace(
                    null, triple.first, triple.second, triple.third,
                    RectF(0f, 0f, triple.second.toFloat(), triple.third.toFloat())
                )
                Thread.sleep(60)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        identifyRunning = false
        executor?.shutdown()
    }

}
package com.ty.tybusbase.dialog

import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.AdlService
import com.bumptech.glide.Glide
import com.ty.tybusbase.R
import com.ty.tybusbase.base.BaseBindingAdapter
import com.ty.tybusbase.base.BaseDialogFragment
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.base.VBViewHolder
import com.ty.tybusbase.base.getContext
import com.ty.tybusbase.databinding.DialogChooseStudentBinding
import com.ty.tybusbase.databinding.ItemChooseStudentBinding
import com.ty.tybusbase.expand.Action
import com.ty.tybusbase.expand.Action1
import com.ty.tybusbase.helper.GridSpaceDecoration
import com.ty.tybusbase.utils.DisplayUtil

class ChooseStudentDialog(private val studentList: MutableList<StudentEntity>) :
    BaseDialogFragment<DialogChooseStudentBinding, BaseViewModel>() {

    private var canChooseClass = true
    private var title: String = ""
    private var chooseStudentCallback: Action1<StudentEntity>? = null
    private var dismissCallback: Action? = null
    private var chooseIndex = -1
    private var classInfoList = mutableListOf<String>()
    private var showStudentList = mutableListOf<StudentEntity>()
    override fun initViewObservable() {
    }

    override fun initData() {
        if (canChooseClass) {
            studentList.associateBy { "${it.gradeName}${it.className}" }
                .map { classInfoList.add(it.key) }
        }
        showStudentList.addAll(studentList)
    }

    override fun initView() {
        mBinding.groupClass.visibility = if (canChooseClass) View.VISIBLE else View.GONE
        if (title.isNotEmpty()) {
            mBinding.tvTitle.text = title
        }

        //初始化  spinner
        if (canChooseClass) {
            //初始化Spinner
            val arrayAdapter: ArrayAdapter<String?> = ArrayAdapter(
                requireContext(), R.layout.item_spinner_bk,
                classInfoList.toList()
            )
            mBinding.spClass.dropDownVerticalOffset =
                resources.getDimensionPixelOffset(R.dimen.dp_50)
            mBinding.spClass.adapter = arrayAdapter
            mBinding.spClass.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    resetChoose()
                    showStudentList =
                        studentList.filter { "${it.gradeName}${it.className}" == classInfoList[position] }
                            .toMutableList()
                    (mBinding.rvStudent.adapter as BaseBindingAdapter<ItemChooseStudentBinding, StudentEntity>).setNewInstance(
                        showStudentList
                                                                                                                              )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }



        mBinding.rvStudent.layoutManager = GridLayoutManager(context, 4)
        mBinding.rvStudent.addItemDecoration(GridSpaceDecoration(
            DisplayUtil.px2dp(requireContext(), 16f),
            DisplayUtil.px2dp(requireContext(), 24f),
                                                                )
        )
        mBinding.rvStudent.adapter =
            object : BaseBindingAdapter<ItemChooseStudentBinding, StudentEntity>(studentList) {
                private var preIvChoose: ImageView? = null
                private var preRootView: View? = null
                override fun convert(
                    holder: VBViewHolder<ItemChooseStudentBinding>,
                    item: StudentEntity
                ) {
                    holder.vb.tvName.text = item.accountName
                    Glide.with(holder.getContext())
                        .load(AdlService.getService().facePrefix + item.userFaceImgUrl)
                        .placeholder(R.mipmap.ic_head_default)
                        .error(R.mipmap.ic_head_default)
                        .into(holder.vb.civHeader)
                    holder.vb.tvClassName.text = "${item.gradeName}${item.className}"
                    if (item.delFlag == "99") {
                        holder.vb.ivChoose.setBackgroundResource(R.mipmap.ic_rb_pre)
                        holder.vb.root.setBackgroundResource(R.drawable.item_choose_student_select_bg)
                    } else {
                        holder.vb.ivChoose.setBackgroundResource(R.mipmap.ic_rb_normal)
                        holder.vb.root.setBackgroundResource(R.drawable.item_choose_student_bg)
                    }
                }

                override fun setOnItemClick(v: View, position: Int) {
                    super.setOnItemClick(v, position)
                    if (chooseIndex == position) return
                    preIvChoose?.setBackgroundResource(R.mipmap.ic_rb_normal)
                    preRootView?.setBackgroundResource(R.drawable.item_choose_student_bg)
                    if (chooseIndex != -1) {
                        data[chooseIndex].delFlag = "0"
                    }
                    data[position].delFlag = "99"
                    mBinding.tvConfirm.setBackgroundResource(R.drawable.bg_round_r50_gradient_red)
                    mBinding.tvConfirm.isEnabled = true
                    val mBinding = ItemChooseStudentBinding.bind(v)
                    mBinding.ivChoose.setBackgroundResource(R.mipmap.ic_rb_pre)
                    mBinding.root.setBackgroundResource(R.drawable.item_choose_student_select_bg)
                    preIvChoose = mBinding.ivChoose
                    preRootView = mBinding.root
                    chooseIndex = position
                }

            }.apply {
                setOnItemClickListener { adapter, view, position -> }
            }
        mBinding.tvConfirm.setOnClickListener {
            dismiss()
            chooseStudentCallback?.invoke((mBinding.rvStudent.adapter as BaseBindingAdapter<*, StudentEntity>).data[chooseIndex])
        }
        mBinding.tvCancel.setOnClickListener {
            dismiss()
        }
        mBinding.ivClose.setOnClickListener { dismiss() }

        mBinding.btnSearch.setOnClickListener {
            resetChoose()
            val search = mBinding.etSearch.text.toString()

            val filter = if (isAllLetters(search)) {
                showStudentList.filter { it.accountNameFirstLetterPinYin.contains(search) }
            } else {
                showStudentList.filter { it.accountName.contains(search) }
            }
            (mBinding.rvStudent.adapter as BaseBindingAdapter<ItemChooseStudentBinding, StudentEntity>).setNewInstance(
                filter.toMutableList()
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissCallback?.invoke()
    }

    fun isAllLetters(input: String): Boolean {
        return input.matches("[a-zA-Z]+".toRegex())
    }

    fun resetChoose() {
        if (chooseIndex != -1) {
            showStudentList[chooseIndex].delFlag = "0"
        }
        mBinding.tvConfirm.isEnabled = false
        mBinding.tvConfirm.setBackgroundResource(R.drawable.bg_round_r50_c4c4c4)
        chooseIndex = -1
    }


    fun setTitle(title: String): ChooseStudentDialog {
        this.title = title
        return this
    }

    fun setCanChooseClass(canChooseClass: Boolean): ChooseStudentDialog {
        this.canChooseClass = canChooseClass
        return this
    }

    fun setChooseStudentCallback(chooseStudentCallback: Action1<StudentEntity>): ChooseStudentDialog {
        this.chooseStudentCallback = chooseStudentCallback
        return this
    }

    fun setDismissListener(dismissListener: Action): ChooseStudentDialog {
        this.dismissCallback = dismissListener
        return this
    }

}
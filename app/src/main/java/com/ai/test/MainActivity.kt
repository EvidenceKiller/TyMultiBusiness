package com.ai.test

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.adl.base.config.AdlUIConfig
import com.adl.base.config.RoleConfigRopeSkip
import com.adl.base.speech.AdlTextSpeaker
import com.adl.mount.core.TsModelResCaller
import com.adl.service.entity.FaceDataEntity
import com.adl.service.entity.MeetGroupDetailsEntity
import com.adl.service.entity.SceneSportInfoEntity
import com.adl.service.entity.StudentEntity
import com.adl.service.outer.UserCaller
import com.adl.sport.standard.ui.SportOverlayRopeSkipEx
import com.ai.ui.bodytest.BodyChooseTestActivity
import com.ai.ui.doubleface.DoubleFaceTestActivity
import com.ai.ui.facetest.FaceTestActivity
import com.ai.ui.facetest2.FaceTest2Activity
import com.ai.ui.facetest3.FaceTest3Activity
import com.ai.ui.gesture.GestureActivity
import com.ai.ui.glframe.OpenGlFrameActivity
import com.ai.ui.multiface.MultiFaceTestActivity
import com.ai.ui.nfc.NfcReaderActivity
import com.ai.ui.picfacetest.PicFaceTestActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ty.bus.multi.core.UIMultiNavigation
import com.ty.bus.multi.core.UIMultiNavigation.goTenSport
import com.ty.bus.multi.core.config.BluetoothSportConfig
import com.ty.bus.multi.core.config.EtTenSportConfig
import com.ty.bus.multi.core.config.FiveSportConfig
import com.ty.bus.multi.core.config.SixSportConfig
import com.ty.bus.multi.core.config.SportModel
import com.ty.bus.multi.core.config.TenSportConfig
import com.ty.bus.multi.core.config.ThirtySportConfig
import com.ty.bus.multi.core.config.ThreeSportConfig
import com.ty.bus.multi.core.dialog.HttpCameraSettingDialog
import com.ty.bus.multi.demo.databinding.AcMainBinding
import com.ty.grpclib.GrpcManager
import com.ty.tybusbase.base.BaseActivity
import com.ty.tybusbase.base.BaseViewModel
import com.ty.tybusbase.config.BusiBaseConfig
import com.ty.tybusbase.config.UINavConfig
import com.ty.tybusbase.constants.BusiBaseConstantsValue
import com.ty.tybusbase.dialog.CommonInputDialog
import com.ty.tybusbase.constants.MMKVBusiKey
import com.ty.tybusbase.constants.SceneConstants
import com.ty.tybusbase.constants.SportConstant
import com.ty.tybusbase.utils.LogUtil
import com.ty.tybusbase.utils.MMKVUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : BaseActivity<AcMainBinding, BaseViewModel>() {
    override fun initObserver() {
    }

    val featureList =
        "[{\"faceData\":\"[-0.09648639,-0.018408572,0.04112154,0.090893775,-0.06010493,0.009876554,0.052136287,-0.09058395,-0.022651013,-0.097423576,-0.070646316,-0.060917705,-0.03958206,-0.016871352,-0.061361436,0.00981136,0.028479166,-0.008638144,-0.004690378,0.039435223,0.07197076,0.0079798065,0.11911637,-0.033840608,0.109220095,-0.0010658875,0.03249878,0.04799022,0.03243176,0.014917352,-0.031189581,0.11712129,0.027647903,0.01989897,-0.14775501,-0.05286744,-0.04809949,0.008823807,0.081744455,-0.026730603,0.002462743,0.1324454,0.026560783,-0.018091839,0.06361625,0.030467745,-0.08805172,-0.04230058,0.024871815,0.01698398,-0.029795457,0.013322804,0.040630598,-0.010965146,0.13999057,0.10588682,0.02866262,-0.053126514,-0.017478313,0.054697398,0.06784553,0.0073135146,0.024589524,-0.049617264,0.03790948,0.08201576,0.09409555,-0.045937594,-0.040977314,0.026713647,-0.07616117,-0.088566124,-0.016791228,0.08959899,-0.035072,-0.08339792,-0.015560633,-0.0033304524,-0.09216654,-0.023396768,-2.9214457E-4,-0.040228605,-0.08793839,-0.077484906,0.0072362977,0.049562495,0.10915627,-0.15229006,-0.04838261,-0.047262657,-0.094980285,-0.016079614,0.0067518023,0.048045263,-0.045489993,0.0389277,-0.017819446,-0.015775584,0.054095816,-0.037077498,-0.07809813,0.10742035,0.059813116,0.09614663,0.05112972,-0.110791884,0.043192264,-0.1246972,0.07438709,-0.042668045,0.018249488,-0.039574858,-0.04283413,0.16703564,0.018152243,-0.050407067,0.051516782,0.031674735,0.0021488082,0.0015531707,-0.03737234,-0.01737584,-0.082946636,-0.009499507,0.02144353,-0.078071244,0.058899965,-0.060014438,-0.09109356,-0.046916813,0.028245144,0.047337644,-0.116848215,-0.0188489,-0.037277482,-0.031267293,0.02122203,0.04677826,0.0442699,-0.012544858,-0.06615253,-0.08372824,-0.0377455,-0.01648827,-0.14100274,-0.082496785,0.015181043,-0.037152585,-0.117547125,-0.07431233,0.04701715,0.06953007,0.0013058183,-2.6623474E-4,-0.035806414,-0.0056355055,-0.07342356,0.03751542,-0.056634147,-0.092133224,-0.1106331,-0.046790186,0.0292592,-0.017974079,-0.04883305,0.058145493,-0.0597177,-0.14410622,-0.06614351,0.12408619,0.0770848,0.12946853,-0.03903971,0.015748842,0.01736425,-0.03609236,0.033453528,0.040763706,-0.004611677,0.0629433,-0.0069171404,-0.029668482,0.010001078,0.009727816,-0.029779484,-0.07716377,-0.062598996,0.113729455,0.009252203,0.03578124,-0.15849225,0.08803255,0.060625162,0.013724534,0.110891774,0.043845735,-0.09637956,0.045912743,0.07961379,0.0387041,0.028632285,0.025533443,0.005027326,0.0073560583,0.06845072,0.032667223,-0.022791037,-0.04352984,-0.039513405,0.009792437,-0.04273789,0.036367234,-0.027405761,0.01770012,0.04277165,0.042653885,0.08247988,-0.0705114,-0.017998964,-0.047355413,-0.040844977,-0.053950727,0.034173544,-0.10943791,0.063182354,0.09244875,0.05751048,-0.022765856,-0.13681483,0.02194718,0.13808808,-0.05773588,0.06927752,0.068238154,0.0808772,0.07233849,5.734244E-4,0.026492193,0.02423866,0.13156994,-0.028693613,-0.04463645,-0.011089746,-0.018015176,-0.066962354,0.01803623,-0.0028615042,-4.0304256E-4,-0.039511267,-0.06024326,-0.035149597,-0.071475424,-0.104219474,0.023451334,-0.052827965,-0.011662232]\",\"type\":\"2\"}]"

    override fun initData() {
//        FaceDetectorHelper.getInstance().init(this) {
//            LogUtil.d("人脸初始化结果：$it")
//        }
        val gson = Gson()
        val stu = StudentEntity().apply {
            accountName = "张三"
            accountId = "123456"
            val ty = object : TypeToken<List<FaceDataEntity>>() {}.type
            faceDataList = gson.fromJson(featureList, ty)
        }
        AdlTextSpeaker.init(this)

        BusiBaseConfig.setCameraType(BusiBaseConstantsValue.CAMERA_TYPE_V4L2)
        BusiBaseConfig.setSkeletonMirror(true)
        MMKVUtil.encode(
            MMKVBusiKey.IDENTITY_TYPE,
            BusiBaseConstantsValue.IDENTITY_TYPE_FACE
        )
//        MMKVUtil.encode(MMKVBusiKey.NFC_READER_TYPE, BusiBaseConstantsValue.DEFAULT_NFC_READER_TYPE)
        BusiBaseConfig.setDoubleSkipGbDefaultRegion(this)
        TsModelResCaller.setSoLog(false)
        startAsServer()
    }

    /** 服务端：启动 gRPC 服务并监听客户端连接 */
    private fun startAsServer() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                GrpcManager.startServer(50051)
//                registerAccountInfoHandler()
                GrpcManager.connectedClientList.collect {
                    it.forEach { cId ->
                        LogUtil.d("有客户端连上,${GrpcManager.getClientIp(cId)}")
                    }
                    LogUtil.d("总连接数：${it.size}")
                }
            } catch (e: Exception) {
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.bnt0.setOnClickListener {
            UIMultiNavigation.goFiveSport(
                this, UINavConfig().apply {
                    sportInfo = SceneSportInfoEntity().apply {
                        sportSkuId = SportConstant.SPORT_SKIP_ROPE + "0001"
                        sportItemCode = SportConstant.SPORT_SKIP_ROPE
                        endValue = "40000"
                        sportSkuName = "五人"
                        sceneId = SceneConstants.INDEPENDENT_TEST
                    }
                }, FiveSportConfig.builder()
                    .setSupportReport(false)
                    .setAutoFinishTime(15_1000)
                    .build()
            )
        }

        mBinding.bnt5RegionSet.setOnClickListener {
            UIMultiNavigation.goMultiRegionSetting(this, 5, 0)
        }

        mBinding.bnt11.setOnClickListener {
            goTenSport(
                this, UINavConfig().apply {
                    sportInfo = SceneSportInfoEntity().apply {
                        sportSkuId = SportConstant.SPORT_SKIP_ROPE + "0001"
                        sportItemCode = SportConstant.SPORT_SKIP_ROPE
                        endValue = "20000"
                        sportSkuName = "十人"
                        sceneId = SceneConstants.INDEPENDENT_TEST
                    }
                    energyPoint = 10
                }, TenSportConfig.builder {
                    setCountdownText("开始倒计时")
                    setResultPageText("测试结束")
                    setSportStartText("开始咯，开始咯")
                    setVoiceBroadcastSupported(true)
                    setSupportNextGroup(false)
                    setSupportReport(true)
                    setSportModel(SportModel.MEETING)
                }
            ) {
                val list = arrayListOf<MeetGroupDetailsEntity>()
                for (index in 1..10) {
                    list.add(MeetGroupDetailsEntity().apply {
                        accountName = "张$index"
                        accountId = "123456$index"
                        accountAvatar =
                            "V2/busi/userFaceImgUrl/2025-12-11/1765450309969_7c033eb3.png"
                    })
                }
                it.putExtra("student_list", list)
            }
        }

        mBinding.btnTenOpenClose.setOnClickListener {
            goTenSport(
                this, UINavConfig().apply {
                    sportInfo = SceneSportInfoEntity().apply {
                        sportItemCode = SportConstant.SPORT_OPEN_CLOSE_JUMP
                        sportSkuId = sportItemCode + "0001"
                        endValue = "20000"
                        sportSkuName = "十人开合跳"
                        sceneId = SceneConstants.INDEPENDENT_TEST
                    }
                }, TenSportConfig.builder {
                    setCountdownText("开始倒计时")
                    setResultPageText("测试结束")
                    setSportStartText("开始咯，开始咯")
                    setVoiceBroadcastSupported(true)
                    setSupportNextGroup(false)
                }
            )
        }

        mBinding.btnTenSquat.setOnClickListener {
            goTenSport(
                this, UINavConfig().apply {
                    sportInfo = SceneSportInfoEntity().apply {
                        sportItemCode = SportConstant.SPORT_SQUATS
                        sportSkuId = sportItemCode + "0001"
                        endValue = "20000"
                        sportSkuName = "十人下蹲"
                        sceneId = SceneConstants.INDEPENDENT_TEST
                    }
                }, TenSportConfig.builder {
                    setCountdownText("开始倒计时")
                    setResultPageText("测试结束")
                    setSportStartText("开始咯，开始咯")
                    setVoiceBroadcastSupported(true)
                    setSupportNextGroup(false)
                }
            )
        }

        mBinding.bnt8.setOnClickListener {
            UIMultiNavigation.goThreeSport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportSkuId = SportConstant.SPORT_SIT_UP + "0001"
                    sportItemCode = SportConstant.SPORT_SIT_UP
                    sportSkuName = "三人仰卧起坐"
                    endValue = "20000"
                    testCount = "3"
                    sceneId = SceneConstants.INDEPENDENT_TEST
                }
            }, ThreeSportConfig.builder().setSupportNextGroup(true).setHideTopOne(true).build())
        }

        mBinding.bnt9.setOnClickListener {
            UIMultiNavigation.goThreeSport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportSkuId = SportConstant.SPORT_PUSH_UP + "0001"
                    sportItemCode = SportConstant.SPORT_PUSH_UP
                    sportSkuName = "三人俯卧撑"
                    endValue = "20000"
                    testCount = "3"
                    sceneId = SceneConstants.INDEPENDENT_TEST
                }
            }, ThreeSportConfig.builder().setSupportNextGroup(false).setHideTopOne(true).build())
        }

        mBinding.btnThreeSet.setOnClickListener {
            UIMultiNavigation.goThreeSportSetting(this)
        }

        mBinding.btn10RegionSet.setOnClickListener {
            UIMultiNavigation.goMultiRegionSetting(
                this,
                10
            )
        }

        mBinding.btnFrameAnimationTest.setOnClickListener {
            startActivity(Intent(this, OpenGlFrameActivity::class.java))
        }

        mBinding.btnIpcSet.setOnClickListener {
            val httpCameraSettingDialog = HttpCameraSettingDialog()
            httpCameraSettingDialog.show(supportFragmentManager, "http_camera")
        }

        mBinding.bnt10.setOnClickListener {
            UIMultiNavigation.goForwardFlexionSetting(this)
        }

        mBinding.bntZw.setOnClickListener {
            UIMultiNavigation.goForwardFlexion(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportSkuId = SportConstant.SPORT_FORWARD_FLEXION + "0001"
                    sportSkuName = "坐位体前屈"
                }
            })
        }

        mBinding.btnFaceTest.setOnClickListener {
            startActivity(Intent(this, FaceTestActivity::class.java))
        }
        mBinding.btnFaceTest2.setOnClickListener {
            startActivity(Intent(this, FaceTest2Activity::class.java))
        }

        mBinding.btnNfcTest.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    NfcReaderActivity::class.java
                )
            )
        }

        mBinding.btnBodyTest.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    BodyChooseTestActivity::class.java
                )
            )
        }

        mBinding.btnDoubleFace.setOnClickListener {
            val intent = Intent(this, DoubleFaceTestActivity::class.java)
            val sportInfo = SceneSportInfoEntity().apply {
                sportSkuId = SportConstant.SPORT_PUSH_UP + "0001"
                sportItemCode = SportConstant.SPORT_PUSH_UP
                endValue = "20000"
                sportSkuName = "三人俯卧撑"
                testCount = "3"
            }
            intent.putExtra("sport_info", sportInfo)
            intent.putExtra("scene_id", SceneConstants.INDEPENDENT_TEST)
            startActivity(intent)

        }
        mBinding.btnTenFace.setOnClickListener {
            startActivity(Intent(this, MultiFaceTestActivity::class.java))
        }

        val org = UserCaller.instance().getCurrentOrg()
        if (org != null) {
            mBinding.tvOrgId.setText(org.getOrgCode())
        }

        mBinding.btnForward6.setOnClickListener {
            UIMultiNavigation.goForwardFlexionSix(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportSkuId = SportConstant.SPORT_FORWARD_FLEXION + "0001"
                    sportSkuName = "坐位体前屈"
                }
            })
        }

        mBinding.btnForward6Set.setOnClickListener {
            UIMultiNavigation.goForwardFlexionSixSetting(this)
        }

        mBinding.btnPicIdentify.setOnClickListener {
            startActivity(Intent(this, PicFaceTestActivity::class.java))
        }

        mBinding.btnProcessorIdentify.setOnClickListener {
            startActivity(Intent(this, FaceTest3Activity::class.java))
        }

        mBinding.btnBmiSetting.setOnClickListener {
            UIMultiNavigation.goBmiSetting(this)
        }

        mBinding.btnBmiSport.setOnClickListener {
            UIMultiNavigation.goBluetoothSport(
                this, UINavConfig().apply {
                    sportInfo = SceneSportInfoEntity().apply {
                        sportItemCode = SportConstant.BODY_HEIGHT_WEIGHT
                        sportSkuId = sportItemCode + "0001"
                        sportSkuName = "BMI"
                        testCount = "3"
                        sceneId = SceneConstants.FREE
                    }
                }, BluetoothSportConfig.builder()
                    .setVoiceBroadcastSupported(true)
                    .setSupportNextGroup(true)
                    .setSupportChoose(true)
                    .build()
            )
        }

        mBinding.btnLungSetting.setOnClickListener {
            UIMultiNavigation.goLungSetting(this)
        }

        mBinding.btnLungSport.setOnClickListener {
            UIMultiNavigation.goBluetoothSport(
                this, UINavConfig().apply {
                    sportInfo = SceneSportInfoEntity().apply {
                        sportItemCode = SportConstant.LUNG_CAPACITY
                        sportSkuId = sportItemCode + "0001"
                        sportSkuName = "肺活量"
                        testCount = "3"
                        sceneId = SceneConstants.FREE
                    }
                }, BluetoothSportConfig.builder()
                    .setVoiceBroadcastSupported(true)
                    .setSupportNextGroup(true)
                    .setSupportChoose(false)
                    .build()
            )
        }

        mBinding.btnSixYwqz.setOnClickListener {
            UIMultiNavigation.goSixSport(
                this, UINavConfig().apply {
                    sportInfo = SceneSportInfoEntity().apply {
                        sportItemCode = SportConstant.SPORT_SIT_UP
                        sportSkuId = sportItemCode + "0001"
                        sportSkuName = "6人仰卧起坐"
                        testCount = "6"
                        endValue = "20000"
                        sceneId = SceneConstants.FREE
                    }
                }, SixSportConfig.builder()
                    .setSupportNextGroup(true)
                    .build()
            )
        }

        mBinding.btnSixYwqzSet.setOnClickListener {
            UIMultiNavigation.goMultiRegionSetting(this, 6, 1)
        }

        mBinding.btnGesture.setOnClickListener {
            startActivity(Intent(this, GestureActivity::class.java))
        }

        mBinding.btnSkipEight.setOnClickListener {
            UIMultiNavigation.goSkipEightSport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportItemCode = SportConstant.SPORT_SKIP_EIGHT_JUMP
                    sportSkuId = sportItemCode + "0001"
                    sportSkuName = "八字跳绳"
                    endValue = "120000"
                    endType = SportConstant.END_TYPE_TIME
                    sceneId = SceneConstants.FREE
                }
                energyPoint = 10
            }) {
                it.putExtra("student", MeetGroupDetailsEntity().apply {
                    classId = "1995771139539406848"
                    serialCode = "01"
                })
            }
        }

        initDouble()
        initThirty()
    }

    private fun initThirty() {
        mBinding.btnThirtySkip.setOnClickListener {
            UIMultiNavigation.goThirtySport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportSkuId = SportConstant.SPORT_SKIP_ROPE + "0024"
                    sportItemCode = SportConstant.SPORT_SKIP_ROPE
                    endValue = "20000"
                    sportSkuName = "30人跳绳"
                    sceneId = SceneConstants.INDEPENDENT_TEST
                }
                energyPoint = 10
            }, ThirtySportConfig.builder {
                setCountdownText("开始倒计时")
                setResultPageText("测试结束")
                setSportStartText("开始咯，开始咯")
                setVoiceBroadcastSupported(true)
                setSupportNextGroup(false)
                setSupportReport(true)
            }
            )
        }
        mBinding.btnEtTenSkip.setOnClickListener {
            UIMultiNavigation.goEtTenSport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportSkuId = SportConstant.SPORT_SKIP_ROPE + "0001"
                    sportItemCode = SportConstant.SPORT_SKIP_ROPE
                    endValue = "20000"
                    sportSkuName = "30人跳绳"
                    sceneId = SceneConstants.INDEPENDENT_TEST
                }
                energyPoint = 10
            }, EtTenSportConfig.builder {
                setCountdownText("开始倒计时")
                setResultPageText("测试结束")
                setSportStartText("开始咯，开始咯")
                setVoiceBroadcastSupported(true)
                setSupportNextGroup(false)
                setSupportReport(true)
            }
            )
        }
        mBinding.btnLeftScreenIp.setOnClickListener {
            showSubScreenIpDialog(
                "设置左副屏IP",
                BusiBaseConfig.readLeftSubScreenIp().orEmpty()
            ) { ip ->
                BusiBaseConfig.saveLeftSubScreenIp(ip)
            }
        }
        mBinding.btnRightScreenIp.setOnClickListener {
            showSubScreenIpDialog(
                "设置右副屏IP",
                BusiBaseConfig.readRightSubScreenIp().orEmpty()
            ) { ip ->
                BusiBaseConfig.saveRightSubScreenIp(ip)
            }
        }
    }

    private fun showSubScreenIpDialog(title: String, content: String, onConfirm: (String) -> Unit) {
        CommonInputDialog()
            .setTitle(title)
            .setLabel("IP地址")
            .setHint("请输入IP地址")
            .setContent(content)
            .setCallback(onConfirm)
            .show(supportFragmentManager, "sub_screen_ip_dialog")
    }

    private fun initDouble() {
        mBinding.btnSkipGb.setOnClickListener {
            UIMultiNavigation.goDoubleGbSport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportItemCode = SportConstant.SPORT_SKIP_ROPE
                    sportSkuId = sportItemCode + "0001"
                    sportSkuName = "双人跳绳"
                    endValue = "20000"
                    endType = SportConstant.END_TYPE_TIME
                    testCount = "2"
                    sceneId = SceneConstants.FREE
                }
                energyPoint = 10
            })
        }

        mBinding.btnSkip.setOnClickListener {
            UIMultiNavigation.goDoubleSport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportItemCode = SportConstant.SPORT_SKIP_ROPE
                    sportSkuId = sportItemCode + "0001"
                    sportSkuName = "双人跳绳"
                    endValue = "20000"
                    endType = SportConstant.END_TYPE_TIME
                    testCount = "2"
                    sceneId = SceneConstants.FREE
                }
                energyPoint = 10
            })
        }

        mBinding.btnDoubleOpenClose.setOnClickListener {
            UIMultiNavigation.goDoubleSport(this, UINavConfig().apply {
                sportInfo = SceneSportInfoEntity().apply {
                    sportItemCode = SportConstant.SPORT_JUMPING_JACK_SKIP_ROPE
                    sportSkuId = sportItemCode + "0001"
                    sportSkuName = "双人跳绳"
                    endValue = "20000"
                    endType = SportConstant.END_TYPE_TIME
                    testCount = "2"
                    sceneId = SceneConstants.FREE
                }
                energyPoint = 10
            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        GrpcManager.stopServer()
    }
}

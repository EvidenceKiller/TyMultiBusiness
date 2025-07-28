package com.ty.identifty.stface;

public class StParameter {
    private Models models;
    private Modules modules;
    private Params params;

    public Modules getModules() {
        return modules;
    }

    public Params getParams() {
        return params;
    }

    public Models getModels() {
        return models;
    }

    public void setModels(Models models) {
        this.models = models;
    }

    public void setModules(Modules modules) {
        this.modules = modules;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public class Models {
        private String trackModelName;
        private String detectModelName;
        private String faceAttrModelName;
        private String bodyAttrModelName;
        private String faceFeatureModelName;
        private String livenessModelName;
        private String liveness2iModelName;
        private String liveness3dModelName;
        private String augustModelName;
        private String alignModelName;
        private String headposeModelName;
        private String blurModelName;
        private String eyestateModelName;
        private String structDetectModelName;
        private String bodyFeatureModelName;
        private String carPlateDetectionModelName;
        private String carPlateRavenModelName;
        private String vehiFilterModelName;
        private String vehiDirectionModelName;
        private String textDetectionModelName;
        private String textRecognitionModelName;

        public String getTrackModelName() {
            return trackModelName;
        }

        public String getDetectModelName() {
            return detectModelName;
        }

        public String getFaceAttrModelName() {
            return faceAttrModelName;
        }

        public String getBodyAttrModelName() {
            return bodyAttrModelName;
        }

        public String getFaceFeatureModelName() {
            return faceFeatureModelName;
        }

        public String getLivenessModelName() {
            return livenessModelName;
        }

        public String getLiveness2iModelName() {
            return liveness2iModelName;
        }

        public String getLiveness3dModelName() {
            return liveness3dModelName;
        }

        public String getAugustModelName() {
            return augustModelName;
        }

        public String getAlignModelName() {
            return alignModelName;
        }

        public String getHeadposeModelName() {
            return headposeModelName;
        }

        public String getBlurModelName() {
            return blurModelName;
        }

        public String getEyestateModelName() {
            return eyestateModelName;
        }

        public String getStructDetectModelName() {
            return structDetectModelName;
        }

        public String getBodyFeatureModelName() {
            return bodyFeatureModelName;
        }

        public String getCarPlateDetectionModelName() {
            return carPlateDetectionModelName;
        }

        public String getCarPlateRavenModelName() {
            return carPlateRavenModelName;
        }

        public String getVehiFilterModelName() {
            return vehiFilterModelName;
        }

        public String getVehiDirectionModelName() {
            return vehiDirectionModelName;
        }

        public String getTextDetectionModelName() {
            return textDetectionModelName;
        }

        public String getTextRecognitionModelName() {
            return textRecognitionModelName;
        }

        public void setTrackModelName(String trackModelName) {
            this.trackModelName = trackModelName;
        }

        public void setDetectModelName(String detectModelName) {
            this.detectModelName = detectModelName;
        }

        public void setFaceAttrModelName(String faceAttrModelName) {
            this.faceAttrModelName = faceAttrModelName;
        }

        public void setBodyAttrModelName(String bodyAttrModelName) {
            this.bodyAttrModelName = bodyAttrModelName;
        }

        public void setFaceFeatureModelName(String faceFeatureModelName) {
            this.faceFeatureModelName = faceFeatureModelName;
        }

        public void setLivenessModelName(String livenessModelName) {
            this.livenessModelName = livenessModelName;
        }

        public void setLiveness2iModelName(String liveness2iModelName) {
            this.liveness2iModelName = liveness2iModelName;
        }

        public void setLiveness3dModelName(String liveness3dModelName) {
            this.liveness3dModelName = liveness3dModelName;
        }

        public void setAugustModelName(String augustModelName) {
            this.augustModelName = augustModelName;
        }

        public void setAlignModelName(String alignModelName) {
            this.alignModelName = alignModelName;
        }

        public void setHeadposeModelName(String headposeModelName) {
            this.headposeModelName = headposeModelName;
        }

        public void setBlurModelName(String blurModelName) {
            this.blurModelName = blurModelName;
        }

        public void setEyestateModelName(String eyestateModelName) {
            this.eyestateModelName = eyestateModelName;
        }

        public void setStructDetectModelName(String structDetectModelName) {
            this.structDetectModelName = structDetectModelName;
        }

        public void setBodyFeatureModelName(String bodyFeatureModelName) {
            this.bodyFeatureModelName = bodyFeatureModelName;
        }

        public void setCarPlateDetectionModelName(String carPlateDetectionModelName) {
            this.carPlateDetectionModelName = carPlateDetectionModelName;
        }

        public void setCarPlateRavenModelName(String carPlateRavenModelName) {
            this.carPlateRavenModelName = carPlateRavenModelName;
        }

        public void setVehiFilterModelName(String vehiFilterModelName) {
            this.vehiFilterModelName = vehiFilterModelName;
        }

        public void setVehiDirectionModelName(String vehiDirectionModelName) {
            this.vehiDirectionModelName = vehiDirectionModelName;
        }

        public void setTextDetectionModelName(String textDetectionModelName) {
            this.textDetectionModelName = textDetectionModelName;
        }

        public void setTextRecognitionModelName(String textRecognitionModelName) {
            this.textRecognitionModelName = textRecognitionModelName;
        }
    }

    public class Modules {
        private int detectTrack;
        private int attribute;
        private int faceFeature;
        private int liveness;
        private int livenessCommon;
        private int db;
        private int carplate;

        public boolean getDetectTrack() {
            return (detectTrack > 0 ? true : false);
        }

        public boolean getAttribute() {
            return (attribute > 0 ? true : false);
        }

        public boolean getFaceFeature() {
            return (faceFeature > 0 ? true : false);
        }

        public boolean getLiveness() {
            return (liveness > 0 ? true : false);
        }

        public boolean getLivenessCommon() {
            return (livenessCommon > 0 ? true : false);
        }

        public boolean getDb() {
            return (db > 0 ? true : false);
        }

        public void setLiveness(int liveness) {
            this.liveness = liveness;
        }

        public void setLivenessCommon(int livenessCommon) {
            this.livenessCommon = livenessCommon;
        }

        public boolean getCarplate() {return (carplate > 0 ? true : false); }

        public void setDetectTrack(int detectTrack) {
            this.detectTrack = detectTrack;
        }

        public void setAttribute(int attribute) {
            this.attribute = attribute;
        }

        public void setFaceFeature(int faceFeature) {
            this.faceFeature = faceFeature;
        }

        public void setDb(int db) {
            this.db = db;
        }

        public void setCarplate(int carplate) {
            this.carplate = carplate;
        }
    }

    public class Params {
        private Common common;
        private DetectTrack detectTrack;
        private Attribute attribute;
        private FaceFeature faceFeature;
        private Select select;
        private Db db;
        private Liveness liveness;
        private LivenessCommon livenessCommon;
        private Carplate carplate;

        public Common getCommon() {
            return common;
        }

        public DetectTrack getDetectTrack() {
            return detectTrack;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public FaceFeature getFaceFeature() {
            return faceFeature;
        }

        public Select getSelect() {
            return select;
        }

        public Db getDb() {
            return db;
        }

        public Liveness getLiveness() {
            return liveness;
        }

        public LivenessCommon getLivenessCommon() {
            return livenessCommon;
        }

        public Carplate getCarplate() {
            return carplate;
        }

        public void setCommon(Common common) {
            this.common = common;
        }

        public void setDetectTrack(DetectTrack detectTrack) {
            this.detectTrack = detectTrack;
        }

        public void setAttribute(Attribute attribute) {
            this.attribute = attribute;
        }

        public void setFaceFeature(FaceFeature faceFeature) {
            this.faceFeature = faceFeature;
        }

        public void setSelect(Select select) {
            this.select = select;
        }

        public void setDb(Db db) {
            this.db = db;
        }

        public void setLiveness(Liveness liveness) {
            this.liveness = liveness;
        }

        public void setLivenessCommon(LivenessCommon livenessCommon) {
            this.livenessCommon = livenessCommon;
        }

        public void setCarplate(Carplate carplate) {
            this.carplate = carplate;
        }

        public class Common {
            private String licenseName;
            private String licenseSignCode;

            private String newLicenseName;
            private String newLicenseSignCode;
            private int width;
            private int height;
            private int fps;
            private int rgbVideoIndex;
            private int nirVideoIndex;
            private int detectTrackCpuId;
            private int analyzerCpuId;
            private int faceFeatureCpuId;
            private int attributeCpuId;
            private String basePath;
            private String oclbinaryPath;

            public String getNewLicenseName() {
                return newLicenseName;
            }

            public String getNewLicenseSignCode() {
                return newLicenseSignCode;
            }

            public String getLicenseName() {
                return licenseName;
            }

            public String getLicenseSignCode() {
                return licenseSignCode;
            }

            public int getWidth() {
                return width;
            }

            public int getHeight() {
                return height;
            }

            public int getFps() {
                return fps;
            }

            public int getRgbVideoIndex() {
                return rgbVideoIndex;
            }

            public int getNirVideoIndex() {
                return nirVideoIndex;
            }

            public int getDetectTrackCpuId() {
                return detectTrackCpuId;
            }

            public int getAnalyzerCpuId() {
                return analyzerCpuId;
            }

            public int getFaceFeatureCpuId() {
                return faceFeatureCpuId;
            }

            public int getAttributeCpuId() {
                return attributeCpuId;
            }

            public String getBasePath() {
                return basePath;
            }

            public String getOclbinaryPath() {
                return oclbinaryPath;
            }

            public void setLicenseName(String licenseName) {
                this.licenseName = licenseName;
            }

            public void setLicenseSignCode(String licenseSignCode) {
                this.licenseSignCode = licenseSignCode;
            }

            public void setNewLicenseName(String newLicenseName) {
                this.newLicenseName = newLicenseName;
            }

            public void setNewLicenseSignCode(String newLicenseSignCode) {
                this.newLicenseSignCode = newLicenseSignCode;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public void setFps(int fps) {
                this.fps = fps;
            }

            public void setRgbVideoIndex(int rgbVideoIndex) {
                this.rgbVideoIndex = rgbVideoIndex;
            }

            public void setNirVideoIndex(int nirVideoIndex) {
                this.nirVideoIndex = nirVideoIndex;
            }

            public void setDetectTrackCpuId(int detectTrackCpuId) {
                this.detectTrackCpuId = detectTrackCpuId;
            }

            public void setAnalyzerCpuId(int analyzerCpuId) {
                this.analyzerCpuId = analyzerCpuId;
            }

            public void setFaceFeatureCpuId(int faceFeatureCpuId) {
                this.faceFeatureCpuId = faceFeatureCpuId;
            }

            public void setAttributeCpuId(int attributeCpuId) {
                this.attributeCpuId = attributeCpuId;
            }

            public void setBasePath(String basePath) {
                this.basePath = basePath;
            }

            public void setOclbinaryPath(String oclbinaryPath) {
                this.oclbinaryPath = oclbinaryPath;
            }
        }

        public class DetectTrack {
            public String type;
            private int minFaceSize;
            private int maxFaceNum;
            private float confidence;

            public String getType() {
                return type;
            }

            public int getMinFaceSize() {
                return minFaceSize;
            }

            public int getMaxFaceNum() {
                return maxFaceNum;
            }

            public float getConfidence() {
                return confidence;
            }

            public void setType(String type) {
                this.type = type;
            }

            public void setMinFaceSize(int minFaceSize) {
                this.minFaceSize = minFaceSize;
            }

            public void setMaxFaceNum(int maxFaceNum) {
                this.maxFaceNum = maxFaceNum;
            }

            public void setConfidence(float confidence) {
                this.confidence = confidence;
            }
        }

        public class Select {
            private float threshold;
            private float step;
            private String method;
            private int highQualityCount;

            public float getThreshold() {
                return threshold;
            }

            public float getStep() {
                return step;
            }

            public String getMethod() {
                return method;
            }

            public int getHighQualityCount() {
                return highQualityCount;
            }

            public void setThreshold(float threshold) {
                this.threshold = threshold;
            }

            public void setStep(float step) {
                this.step = step;
            }

            public void setMethod(String method) {
                this.method = method;
            }

            public void setHighQualityCount(int highQualityCount) {
                this.highQualityCount = highQualityCount;
            }
        }

        public class Attribute {
            public String type;
            private String platform;
            private String algorithms;

            public String getType() {
                return type;
            }

            public String getPlatform() {
                return platform;
            }

            public String getAlgorithms() {
                return algorithms;
            }

            public void setType(String type) {
                this.type = type;
            }

            public void setPlatform(String platform) {
                this.platform = platform;
            }

            public void setAlgorithms(String algorithms) {
                this.algorithms = algorithms;
            }
        }

        public class FaceFeature {
            private String type;
            private String platform;
            private String algorithms;

            public String getType() {
                return type;
            }

            public String getPlatform() {
                return platform;
            }

            public String getAlgorithms() {
                return algorithms;
            }

            public void setType(String type) {
                this.type = type;
            }

            public void setPlatform(String platform) {
                this.platform = platform;
            }

            public void setAlgorithms(String algorithms) {
                this.algorithms = algorithms;
            }
        }

        public class Liveness {
            private float threshold;
            private int minSize;
            private String platform;
            private String algorithms;

            public float getThreshold() {
                return threshold;
            }

            public int getMinSize() {
                return minSize;
            }

            public String getPlatform() {
                return platform;
            }

            public String getAlgorithms() {
                return algorithms;
            }

            public void setThreshold(float threshold) {
                this.threshold = threshold;
            }

            public void setMinSize(int minSize) {
                this.minSize = minSize;
            }

            public void setPlatform(String platform) {
                this.platform = platform;
            }

            public void setAlgorithms(String algorithms) {
                this.algorithms = algorithms;
            }
        }

        public class LivenessCommon {
            private float iouThreshold;
            private float threshold;
            private int minSize;
            private String platform;
            private String algorithms;

            public float getIouThreshold() {
                return iouThreshold;
            }

            public float getThreshold() {
                return threshold;
            }

            public int getMinSize() {
                return minSize;
            }

            public String getPlatform() {
                return platform;
            }

            public String getAlgorithms() {
                return algorithms;
            }

            public void setIouThreshold(float iouThreshold) {
                this.iouThreshold = iouThreshold;
            }

            public void setThreshold(float threshold) {
                this.threshold = threshold;
            }

            public void setMinSize(int minSize) {
                this.minSize = minSize;
            }

            public void setPlatform(String platform) {
                this.platform = platform;
            }

            public void setAlgorithms(String algorithms) {
                this.algorithms = algorithms;
            }
        }

        public class Db {
            private String name;
            private String tableName;
            private int size;
            private float threshold;
            private int version;

            public String getName() {
                return name;
            }

            public String getTableName() {
                return tableName;
            }

            public int getSize() {
                return size;
            }

            public float getThreshold() {
                return threshold;
            }

            public int getVersion() {
                return version;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setTableName(String tableName) {
                this.tableName = tableName;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public void setThreshold(float threshold) {
                this.threshold = threshold;
            }

            public void setVersion(int version) {
                this.version = version;
            }
        }

        public class Carplate {
            public String type;
            private float confidence;

            public String getType() {
                return type;
            }

            public float getConfidence() {
                return confidence;
            }

            public void setType(String type) {
                this.type = type;
            }

            public void setConfidence(float confidence) {
                this.confidence = confidence;
            }
        }
    }
}

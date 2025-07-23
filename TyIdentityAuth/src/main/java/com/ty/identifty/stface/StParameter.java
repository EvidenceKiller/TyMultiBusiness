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
        }
    }
}

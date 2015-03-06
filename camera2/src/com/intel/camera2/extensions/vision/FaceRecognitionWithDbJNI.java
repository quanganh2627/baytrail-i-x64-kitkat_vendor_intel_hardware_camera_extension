package com.intel.camera2.extensions.vision;

import com.intel.camera2.extensions.IaFrame;
import com.intel.camera2.extensions.vision.FaceData.EyeInfo;
import com.intel.camera2.extensions.vision.FaceData.RecognitionInfo;

final class FaceRecognitionWithDbJNI extends PVLibraryLoader {
    public native static long create(String dbPath);
    public native static void destroy(long instance);
    public native static RecognitionInfo[] runInImage(long instance, IaFrame frame, EyeInfo[] edInfo);
    public native static void registerFace(long instance, RecognitionInfo result);
    public native static void unregisterFace(long instance, long faceId);
    public native static void updatePerson(long instance, long faceId, int personId);
    public native static void unregisterPerson(long instance, int personId);
    public native static int getNumFacesInDatabase(long instance);
    public native static int getNewPersonId(long instance);

    public native static void setParam(long instance, Param param);
    public native static Param getParam(long instance);
    public native static Config getConfig(long instance);

    public static class Param {
        /** The maximum number of recognizable faces in one frame.
         *  The maximum allowable value is 'max_supported_faces_in_preview' in the structure 'pvl_face_recognition',
         *  and the minimum allowable value is 1. The default value is set to maximum while the component created.
         *  (i.e. 1 <= max_faces_inpreview <= max_supported_faces_in_preview)
         */
        final int max_faces_in_preview;

        public Param(int max_faces_in_preview) {
            this.max_faces_in_preview = max_faces_in_preview;
        }

        public String toString() {
            return "max_faces_in_preview("+max_faces_in_preview+")";
        }
    }

    public static class Config {
        /** The version information. */
        final Version version;
        /** The maximum number of faces supported by this component. */
        final int max_supported_faces_in_preview;
        /** The maximum number of faces that the database can hold in the current version. */
        final int max_faces_in_database;
        /** The maximum number of persons that the database can hold in the current version. */
        final int max_persons_in_database;
        /** The maximum number of faces per person in the current version. */
        final int max_faces_per_person;
        /** The fixed size of the face data (in bytes) for the current version. */
        final int facedata_size;

        public Config(Version version, int max_supported_faces_in_preview, int max_faces_in_database,
                int max_persons_in_database, int max_faces_per_person, int facedata_size) {
            this.version = version;
            this.max_supported_faces_in_preview = max_supported_faces_in_preview;
            this.max_faces_in_database = max_faces_in_database;
            this.max_persons_in_database = max_persons_in_database;
            this.max_faces_per_person = max_faces_per_person;
            this.facedata_size = facedata_size;
        }

        public String toString() {
            return "version("+version.toString()+") max_supported_faces_in_preview("+max_supported_faces_in_preview+") max_faces_in_database("+max_faces_in_database+")" +
                   "max_persons_in_database("+max_persons_in_database+") max_faces_per_person("+max_faces_per_person+") facedata_size("+facedata_size+")";
        }
    }
}

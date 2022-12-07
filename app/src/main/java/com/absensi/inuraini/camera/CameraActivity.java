package com.absensi.inuraini.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.absensi.inuraini.GetServerTime;
import com.absensi.inuraini.Preferences;
import com.absensi.inuraini.R;
import com.absensi.inuraini.user.UserActivity;
import com.absensi.inuraini.user.absen.AbsenData;
import com.absensi.inuraini.user.absen.AbsenFragment;
import com.absensi.inuraini.user.ui.HomeActivityUser;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce;
    DateFormat dateRekap = new SimpleDateFormat("ddMMyyyy");
    DateFormat jamAbsen = new SimpleDateFormat("HH:mm");
    Calendar calendar = Calendar.getInstance();
    FaceDetector detector;
    Interpreter tfLite;
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    PreviewView camera_view;
    CameraSelector cameraSelector;
    TextView namaface, daftar;
    int cam_face = CameraSelector.LENS_FACING_FRONT; //Default Front Camera
    ProcessCameraProvider cameraProvider;
    private final float OPEN_THRESHOLD = 0.85f;
    private final float CLOSE_THRESHOLD = 0.4f;

    private double leftEyeOpenProbability = -1.0;
    private double rightEyeOpenProbability = -1.0;

    private int state = 0;

    float distance= 1.00f;
    boolean start=true, flipX=false;
    Context context = this;

    int[] intValues;
    int inputSize=112;  //Input size for model
    boolean isModelQuantized=false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE=192; //Output size of model
    boolean wajahid;

    String modelFile = "mobile_face_net.tflite"; //model name
    String myname;
    HashMap<String, SimilarityClassifier.Recognition> retrievedMap;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user");

    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        firebaseUser = Preferences.mAuth.getCurrentUser();
        camera_view = findViewById(R.id.previewCamera);
        namaface = findViewById(R.id.deskripsi);
        daftar = findViewById(R.id.daftardulu);
        registered = readFromSP();
        wajahid = getIntent().getBooleanExtra("faceid", false);
//        loadFaceData(registered);

        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        //Load model
        try {
            tfLite = new Interpreter(loadModelFile(CameraActivity.this,modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);

        actionListeners();
    }

    private void actionListeners() {
        databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    myname = snapshot.child("sNama").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean allPermissionsGranted(){
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void addFace() {
        start = false;
        //Create and Initialize new object with Face embeddings and Name.
        SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "", "", -1f);
        result.setExtra(embeedings);

        String nama = myname;
        registered.put(nama, result);
        insertToSP(registered);
        start = true;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //Bind camera and preview view
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {

                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1080, 1920))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        preview.setSurfaceProvider(camera_view.getSurfaceProvider());
        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            try {
                Thread.sleep(0);  //Camera preview refreshed every 10 millisec(adjust as required)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            InputImage image = null;

            @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
            // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

            Image mediaImage = imageProxy.getImage();

            if (mediaImage != null) {
                image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//                    System.out.println("Rotation "+imageProxy.getImageInfo().getRotationDegrees());
            }

            //Process acquired image to detect faces
                    detector.process(image)
                            .addOnSuccessListener(faces -> {
                                if(faces.size()!=0) {

                                    Face face = faces.get(0); //Get first face from detected faces
//                                                    System.out.println(face);

                                    //mediaImage to Bitmap
                                    Bitmap frame_bmp = toBitmap(mediaImage);

                                    int rot = imageProxy.getImageInfo().getRotationDegrees();

                                    //Adjust orientation of Face
                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);

                                    //Get bounding box of face
                                    RectF boundingBox = new RectF(face.getBoundingBox());

                                    //Crop out bounding box from whole Bitmap(image)
                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                                    if(flipX)
                                        cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
                                    //Scale the acquired Face to 112*112 which is required input for model
                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                                    //Process Image With TFLite
                                    imgProcess(scaled);

                                    if(start) {
                                        if (wajahid){
                                            scanWajah(face);
                                        } else {
                                            recognizeImage(face); //Send scaled bitmap to create face embeddings.
                                        }
                                    }

                                } else {
                                    if (registered.isEmpty()) {
                                        namaface.setText("Wajah anda belum terdaftar\nTambahkan data wajah");
                                    } else {
                                        namaface.setText("Tidak ada wajah terdeteksi\nArahkan wajah anda pada kamera");
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Task failed with an exception
                                // ...
                            })
                            .addOnCompleteListener(task -> {
                                imageProxy.close(); //v.important to acquire next frame for analysis
                            });
        });
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    boolean blink(float value) {
        switch (state) {
            case 0:
                if (value > OPEN_THRESHOLD) {
                    // Both eyes are initially open
                    state = 1;
                }
                break;

            case 1:
                if (value < CLOSE_THRESHOLD ) {
                    // Both eyes become closed
                    state = 2;
                }
                break;

            case 2:
                if (value > OPEN_THRESHOLD)  {
                    // Both eyes are open again
                    state = 0;
                    return true;
                }
                break;
        }
        return false;
    }

    private boolean isEyeBlinked(Face face){
        float currentLeftEyeOpenProbability = face.getLeftEyeOpenProbability();
        float currentRightEyeOpenProbability = face.getRightEyeOpenProbability();
        if(currentLeftEyeOpenProbability == -1.0 || currentRightEyeOpenProbability == -1.0){
            return false;
        }
        if(leftEyeOpenProbability > 0.9 || rightEyeOpenProbability > 0.9){
            boolean blinked = false;
            if(currentLeftEyeOpenProbability < 0.6 || rightEyeOpenProbability < 0.6){
                blinked = true;
            }
            leftEyeOpenProbability = currentLeftEyeOpenProbability;
            rightEyeOpenProbability = currentRightEyeOpenProbability;
            return blinked;
        } else {
            leftEyeOpenProbability = currentLeftEyeOpenProbability;
            rightEyeOpenProbability = currentRightEyeOpenProbability;
            return false;
        }
    }

    public void imgProcess(Bitmap bitmap){
        // set Face to Preview
//        face_preview.setImageBitmap(bitmap);

        //Create ByteBuffer to store normalized image
        ByteBuffer imgData = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
    }

    public void recognizeImage(Face face) {
        databaseReference.child(firebaseUser.getUid()).child("faceID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    float distance_local = Float.MAX_VALUE;
                    String id = "0";
                    String label = "?";

                    //Compare new face with saved Faces.
                    if (registered.size() > 0) {

                        final List<Pair<String, Float>> nearest = findNearest(embeedings[0]);//Find 2 closest matching face

                        if (nearest.get(0) != null) {

                            final String name = nearest.get(0).first; //get name and distance of closest matching face
                            // label = name;
                            distance_local = nearest.get(0).second;
                            if(distance_local<distance) { //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                                float left = face.getLeftEyeOpenProbability();
                                float right = face.getRightEyeOpenProbability();
                                namaface.setText("Kedipkan mata anda");
                                daftar.setText("Scan Wajah untuk absen");
                                float value = Math.min(left, right);

                                if (blink(value)){
                                    boolean getOffice = getIntent().getBooleanExtra("atOffice", false);
                                    boolean absenOut = getIntent().getBooleanExtra("absenOut", false);
                                    if (absenOut){
                                        absenKeluar();
                                    } else {
                                        if (getOffice) {
                                            absenKantor();
                                        } else {
                                            absenLuarKantor();
                                        }
                                    }
                                    finish();
                                }
                            } else {
                                databaseReference.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            String namasaya = snapshot.child("sNama").getValue(String.class);
                                            namaface.setText("Ini bukan wajah "+ namasaya);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    } else {
                        namaface.setText("Wajah anda belum terdaftar\nTambahkan data wajah");
                    }
                } else {
                    float left = face.getLeftEyeOpenProbability();
                    float right = face.getRightEyeOpenProbability();
                    namaface.setText("Kedipkan mata anda");
                    daftar.setText("Scan wajah\nPastikan wajah anda terlihat jelas dan posisi tidak terpotong pada tampilan kamera");
                    float value = Math.min(left, right);
                    if (blink(value)) {
                        addFace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void scanWajah(Face face) {
        float left = face.getLeftEyeOpenProbability();
        float right = face.getRightEyeOpenProbability();
        namaface.setText("Kedipkan mata anda");
        daftar.setText("Anda belum mendaftarkan wajah anda\n\nScan wajah\nPastikan wajah anda terlihat jelas dan posisi tidak terpotong pada tampilan kamera");
        float value = Math.min(left, right);
        if (blink(value)) {
            addFace();
            if (Preferences.getDataStatus(context).equals("user")) {
                HomeActivityUser.firstExit = true;
            } else {
                UserActivity.firstExit = true;
            }
            finish();
        }
    }

    private void absenKeluar(){
        GetServerTime serverTime = new GetServerTime(this);
        serverTime.getDateTime((date, time) -> {
            String tggl = Preferences.getOnlyDigits(Preferences.tgglFormatId(date));
            boolean lembur = getIntent().getBooleanExtra("lembur", false);
            Map<String, Object> postValues = new HashMap<>();
            postValues.put("sJamKeluar", AbsenFragment.setTimeTr ? AbsenFragment.traveler : time);
            postValues.put("sLembur", lembur);

            databaseReference.child(firebaseUser.getUid()).child("sAbsensi").child(tggl).updateChildren(postValues).addOnFailureListener(e -> Toast.makeText(context, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
        });
    }

    private void absenKantor(){
        GetServerTime serverTime = new GetServerTime(this);
        serverTime.getDateTime((date, time) -> {
            String tggl = Preferences.getOnlyDigits(Preferences.tgglFormatId(date));
            boolean absenKantor = AbsenFragment.atOffice;
            boolean telat = getIntent().getBooleanExtra("telat", false);
            boolean lembur = getIntent().getBooleanExtra("lembur", false);
            boolean hadir = true;
            Object[][] lokAbsen = Preferences.getMyLocation(context, this);
            boolean acc = false;
            boolean konfirmAdmin = false;

            AbsenData absenData = new AbsenData(AbsenFragment.setTimeTr ? AbsenFragment.traveler : time, "", "", String.valueOf(lokAbsen[0][0]), String.valueOf(lokAbsen[0][1]), "Kantor", absenKantor, hadir, telat, lembur, acc, konfirmAdmin);
            databaseReference.child(firebaseUser.getUid()).child("sAbsensi").child(tggl).setValue(absenData).addOnFailureListener(e -> Toast.makeText(context, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
        });
    }

    private void absenLuarKantor(){
        GetServerTime serverTime = new GetServerTime(this);
        serverTime.getDateTime((date, time) -> {
            String tggl = Preferences.getOnlyDigits(Preferences.tgglFormatId(date));
            boolean absenKantor = AbsenFragment.atOffice;
            boolean telat = getIntent().getBooleanExtra("telat", false);
            boolean lembur = getIntent().getBooleanExtra("lembur", false);
            boolean hadir = true;
            Object[][] lokAbsen = Preferences.getMyLocation(context, this);
            boolean acc = false;
            boolean konfirmAdmin = false;

            AbsenData absenData = new AbsenData(AbsenFragment.setTimeTr ? AbsenFragment.traveler : time, "", "", String.valueOf(lokAbsen[0][0]), String.valueOf(lokAbsen[0][1]), (String) lokAbsen[1][1], absenKantor, hadir, telat, lembur, acc, konfirmAdmin);
            databaseReference.child(firebaseUser.getUid()).child("sAbsensi").child(tggl).setValue(absenData).addOnFailureListener(e -> Toast.makeText(context, "Terjadi kesalahan, periksa koneksi internet dan coba lagi!", Toast.LENGTH_SHORT).show());
        });
    }

    //Compare Faces by distance between face embeddings
    private List<Pair<String, Float>> findNearest(float[] emb) {
        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();
        Pair<String, Float> ret = null; //to get closest match
        Pair<String, Float> prev_ret = null; //to get second closest match
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet())
        {

            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                prev_ret=ret;
                ret = new Pair<>(name, distance);
            }
        }
        if(prev_ret==null) prev_ret=ret;
        neighbour_list.add(ret);
        neighbour_list.add(prev_ret);

        return neighbour_list;

    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    //IMPORTANT. If conversion not done ,the toBitmap conversion does not work on some devices.
    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width*height;
        int uvSize = width*height/4;

        byte[] nv21 = new byte[ySize + uvSize*2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert(image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos<ySize; pos+=width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert(rowStride == image.getPlanes()[1].getRowStride());
        assert(pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte)~savePixel);
                if (uBuffer.get(0) == (byte)~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            }
            catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row=0; row<height/2; row++) {
            for (int col=0; col<width/2; col++) {
                int vuPos = col*pixelStride + row*rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    private Bitmap toBitmap(Image image) {

        byte[] nv21=YUV_420_888toNV21(image);


        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        //System.out.println("bytes"+ Arrays.toString(imageBytes));

        //System.out.println("FORMAT"+image.getFormat());

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    //Save Faces to Shared Preferences.Conversion of Recognition objects to json string
    private void insertToSP(HashMap<String, SimilarityClassifier.Recognition> jsonMap) {
//        jsonMap.putAll(readFromSP());
        String jsonString = new Gson().toJson(jsonMap);
//        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : jsonMap.entrySet())
//        {
//            System.out.println("Entry Input "+entry.getKey()+" "+  entry.getValue().getExtra());
//            Toast.makeText(context, "Entry Input "+entry.getKey()+" "+entry.getValue().getExtra()+" "+jsonString, Toast.LENGTH_LONG).show();
//            Toast.makeText(context, jsonString, Toast.LENGTH_LONG).show();
//        }
        if (Preferences.getFaceId(context) != null) {
            Map<String, Object> updatesFaceID = new HashMap<>();
            updatesFaceID.put("faceID", jsonString);
            databaseReference.child(firebaseUser.getUid()).updateChildren(updatesFaceID).addOnSuccessListener(unused -> {
                Toast.makeText(context, "Recognitions Saved", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Cant saved data, Something error!", Toast.LENGTH_SHORT).show();
            });
        } else {
            databaseReference.child(firebaseUser.getUid()).child("faceID").setValue(jsonString).addOnSuccessListener(unused -> {
                Toast.makeText(context, "Recognitions Saved", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Cant saved data, Something error!", Toast.LENGTH_SHORT).show();
            });
        }
//        SharedPreferences sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("map", jsonString);
//        //System.out.println("Input josn"+jsonString.toString());
//        editor.apply();
//        Toast.makeText(context, "Recognitions Saved", Toast.LENGTH_SHORT).show();
    }

    //Load Faces from Shared Preferences.Json String to Recognition object
    private HashMap<String, SimilarityClassifier.Recognition> readFromSP(){
        TypeToken<HashMap<String,SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String,SimilarityClassifier.Recognition>>() {};
        retrievedMap = new Gson().fromJson(Preferences.getFaceId(context), token.getType());
        // System.out.println("Output map"+retrievedMap.toString());

        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet())
        {
            float[][] output=new float[1][OUTPUT_SIZE];
            ArrayList arrayList= (ArrayList) entry.getValue().getExtra();
            arrayList = (ArrayList) arrayList.get(0);
            for (int counter = 0; counter < arrayList.size(); counter++) {
                output[0][counter]= ((Double) arrayList.get(counter)).floatValue();
            }
            entry.getValue().setExtra(output);

            //System.out.println("Entry output "+entry.getKey()+" "+entry.getValue().getExtra() );
        }
//        System.out.println("OUTPUT"+ Arrays.deepToString(outut));
        return retrievedMap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Preferences.isConnected(context)){
            Preferences.dialogNetwork(context);
        }
    }

    @Override
    public void onBackPressed() {
        if (wajahid) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                finishAffinity();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_exit), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            finish();
        }
    }
}
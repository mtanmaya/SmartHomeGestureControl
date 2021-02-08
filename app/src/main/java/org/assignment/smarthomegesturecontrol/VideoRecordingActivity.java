package org.assignment.smarthomegesturecontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.assignment.smarthomegesturecontrol.ui.main.config.ApiConfig;
import org.assignment.smarthomegesturecontrol.ui.main.config.AppConfig;
import org.assignment.smarthomegesturecontrol.ui.main.model.ServerResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoRecordingActivity extends AppCompatActivity {

    private static final String TAG = "videoRecordingActivity";

//    private static final int VIDEO_CAPTURE = 101;
//    private Uri fileUri;

    // code to upload start
    Button btnUpload, btnMulUpload, btnPickImage, btnPickVideo;
    String mediaPath, mediaPath1;
    ImageView imgView;
    String[] mediaColumns = {MediaStore.Video.Media._ID};
    ProgressDialog progressDialog;
    TextView str1, str2;
    // code to upload end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recording);

        // code to upload start

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        btnUpload = (Button) findViewById(R.id.upload);
        btnMulUpload = (Button) findViewById(R.id.uploadMultiple);
        btnPickImage = (Button) findViewById(R.id.pick_img);
        btnPickVideo = (Button) findViewById(R.id.pick_vdo);
        imgView = (ImageView) findViewById(R.id.preview);
        str1 = (TextView) findViewById(R.id.filename1);
        str2 = (TextView) findViewById(R.id.filename2);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        btnMulUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadMultipleFiles();
            }
        });

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 0);
            }
        });

        // Video must be low in Memory or need to be compressed before uploading...
        btnPickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 1);
            }
        });
        // code to upload end

        dispatchTakeVideoIntent();

    }

//    public void startRecording(View view) {
//        File mediaFile = new
//                File(Environment.getExternalStorageDirectory().getAbsolutePath()
//                + "/myvideo.mp4");
//
//        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        fileUri = Uri.fromFile(mediaFile);
//
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//        startActivityForResult(intent, VIDEO_CAPTURE);
//    }
//
//    protected void onActivityResult(int requestCode,
//                                    int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == VIDEO_CAPTURE) {
//            if (resultCode == RESULT_OK) {
//                Toast.makeText(this, "Video has been saved to:\n" +
//                        data.getData(), Toast.LENGTH_LONG).show();
//            } else if (resultCode == RESULT_CANCELED) {
//                Toast.makeText(this, "Video recording cancelled.",
//                        Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(this, "Failed to record video",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
//    }

    private static final int REQUEST_VIDEO_CAPTURE = 100;

    private void dispatchTakeVideoIntent() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_VIDEO_CAPTURE);
        }

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra("android.intent.extra.durationLimit", 5);
        //if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
        startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i(TAG, "requestCode: " + requestCode);
        Log.i(TAG, "resultCode: " + resultCode);
        Log.i(TAG, "intent.getData: " + intent.getData());

        // code for video recording start
//        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
//            Uri videoUri = intent.getData();
//            Log.i("videorecording", "videoUri: " + videoUri.getPath());
//            mediaPath = videoUri.getPath();
//            uploadFile();
//        }
        // code for video recording end

        // code for file upload start

        try {
            // When an Image is picked
            if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK && null != intent) {

                // Get the Image from data
                Uri selectedImage = intent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mediaPath = cursor.getString(columnIndex);
                str1.setText(mediaPath);
                // Set the Image in ImageView for Previewing the Media
                imgView.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                cursor.close();

            } // When an Video is picked
            else if (requestCode == 1 && resultCode == RESULT_OK && null != intent) {

                // Get the Video from data
                Uri selectedVideo = intent.getData();
                String[] filePathColumn = {MediaStore.Video.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedVideo, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                mediaPath1 = cursor.getString(columnIndex);
                str2.setText(mediaPath1);
                // Set the Video Thumb in ImageView Previewing the Media
                imgView.setImageBitmap(getThumbnailPathForLocalFile(VideoRecordingActivity.this, selectedVideo));
                cursor.close();

            } else {
                Toast.makeText(this, "You haven't picked Image/Video", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }

        // code for file upload end
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    // code for file upload start

    // Providing Thumbnail For Selected Image
    public Bitmap getThumbnailPathForLocalFile(Activity context, Uri fileUri) {
        long fileId = getFileId(context, fileUri);
        return MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(),
                fileId, MediaStore.Video.Thumbnails.MICRO_KIND, null);
    }

    // Getting Selected File ID
    public long getFileId(Activity context, Uri fileUri) {
        Cursor cursor = context.managedQuery(fileUri, mediaColumns, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            return cursor.getInt(columnIndex);
        }
        return 0;
    }

    // Uploading Image/Video
    private void uploadFile() {
        progressDialog.show();

        // Map is used to multipart the file using okhttp3.RequestBody
        File file = new File(mediaPath);

        // Parsing any Media type file
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());

        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);
        Call<ServerResponse> call = getResponse.uploadFile(fileToUpload, filename);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    // Uploading Image/Video
    private void uploadMultipleFiles() {
        progressDialog.show();

        // Map is used to multipart the file using okhttp3.RequestBody
        File file = new File(mediaPath);
        File file1 = new File(mediaPath1);

        // Parsing any Media type file
        RequestBody requestBody1 = RequestBody.create(MediaType.parse("*/*"), file);
        RequestBody requestBody2 = RequestBody.create(MediaType.parse("*/*"), file1);

        MultipartBody.Part fileToUpload1 = MultipartBody.Part.createFormData("file1", file.getName(), requestBody1);
        MultipartBody.Part fileToUpload2 = MultipartBody.Part.createFormData("file2", file1.getName(), requestBody2);

        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);
        Call<ServerResponse> call = getResponse.uploadMulFile(fileToUpload1, fileToUpload2);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    // code for file upload end

//    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
//    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
//    private static final int STATE_PREVIEW = 0;
//    private static final int STATE_WAIT_LOCK = 1;
//    private int mCaptureState = STATE_PREVIEW;
//    private TextureView mTextureView;
//    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            setupCamera(width, height);
//            connectCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//        }
//    };
//
//    private CameraDevice mCameraDevice;
//
//    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            mCameraDevice = camera;
//            mMediaRecorder = new MediaRecorder();
//            if (mIsRecording) {
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                startRecord();
//                mMediaRecorder.start();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mChronometer.setBase(SystemClock.elapsedRealtime());
//                        mChronometer.setVisibility(View.VISIBLE);
//                        mChronometer.start();
//                    }
//                });
//            } else {
//                startPreview();
//            }
//            // Toast.makeText(getApplicationContext(),
//            //         "Camera connection made!", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            camera.close();
//            mCameraDevice = null;
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            camera.close();
//            mCameraDevice = null;
//        }
//    };
//
//    private HandlerThread mBackgroundHandlerThread;
//    private Handler mBackgroundHandler;
//    private String mCameraId;
//    private Size mPreviewSize;
//    private Size mVideoSize;
//    private Size mImageSize;
//    private ImageReader mImageReader;
//    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new
//            ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));
//                }
//            };
//
//    private class ImageSaver implements Runnable {
//
//        private final Image mImage;
//
//        public ImageSaver(Image image) {
//            mImage = image;
//        }
//
//        @Override
//        public void run() {
//            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
//            byte[] bytes = new byte[byteBuffer.remaining()];
//            byteBuffer.get(bytes);
//
//            FileOutputStream fileOutputStream = null;
//            try {
//                fileOutputStream = new FileOutputStream(mImageFileName);
//                fileOutputStream.write(bytes);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                mImage.close();
//
//                Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mImageFileName)));
//                sendBroadcast(mediaStoreUpdateIntent);
//
//                if (fileOutputStream != null) {
//                    try {
//                        fileOutputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        }
//    }
//
//    private MediaRecorder mMediaRecorder;
//    private Chronometer mChronometer;
//    private int mTotalRotation;
//    private CameraCaptureSession mPreviewCaptureSession;
//
//    private final CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new
//            CameraCaptureSession.CaptureCallback() {
//
//                private void process(CaptureResult captureResult) {
//                    switch (mCaptureState) {
//                        case STATE_PREVIEW:
//                            // Do nothing
//                            break;
//                        case STATE_WAIT_LOCK:
//                            mCaptureState = STATE_PREVIEW;
//                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
//                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
//                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
//                                Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
//                                startStillCaptureRequest();
//                            }
//                            break;
//                    }
//                }
//
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//
//                    process(result);
//                }
//            };
//
//    private CameraCaptureSession mRecordCaptureSession;
//
//    private CameraCaptureSession.CaptureCallback mRecordCaptureCallback = new
//            CameraCaptureSession.CaptureCallback() {
//
//                private void process(CaptureResult captureResult) {
//                    switch (mCaptureState) {
//                        case STATE_PREVIEW:
//                            // Do nothing
//                            break;
//                        case STATE_WAIT_LOCK:
//                            mCaptureState = STATE_PREVIEW;
//                            Integer afState = captureResult.get(CaptureResult.CONTROL_AF_STATE);
//                            if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
//                                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
//                                Toast.makeText(getApplicationContext(), "AF Locked!", Toast.LENGTH_SHORT).show();
//                                startStillCaptureRequest();
//                            }
//                            break;
//                    }
//                }
//
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//
//                    process(result);
//                }
//            };
//
//    private CaptureRequest.Builder mCaptureRequestBuilder;
//
//    private ImageButton mRecordImageButton;
//    private ImageButton mStillImageButton;
//    private boolean mIsRecording = false;
//    private boolean mIsTimelapse = false;
//
//    private File mVideoFolder;
//    private String mVideoFileName;
//    private File mImageFolder;
//    private String mImageFileName;
//
//    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 0);
//        ORIENTATIONS.append(Surface.ROTATION_90, 90);
//        ORIENTATIONS.append(Surface.ROTATION_180, 180);
//        ORIENTATIONS.append(Surface.ROTATION_270, 270);
//    }
//
//    private static class CompareSizeByArea implements Comparator<Size> {
//
//        @Override
//        public int compare(Size lhs, Size rhs) {
//            return Long.signum( (long)(lhs.getWidth() * lhs.getHeight()) -
//                    (long)(rhs.getWidth() * rhs.getHeight()));
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_video_recording);
//
//        createVideoFolder();
//        createImageFolder();
//
//        mChronometer = (Chronometer) findViewById(R.id.chronometer);
//        mTextureView = (TextureView) findViewById(R.id.textureView);
//        mStillImageButton = (ImageButton) findViewById(R.id.cameraImageButton2);
//        mStillImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!(mIsTimelapse || mIsRecording)) {
//                    checkWriteStoragePermission();
//                }
//                lockFocus();
//            }
//        });
//        mRecordImageButton = (ImageButton) findViewById(R.id.videoOnlineImageButton);
//        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mIsRecording || mIsTimelapse) {
//                    mChronometer.stop();
//                    mChronometer.setVisibility(View.INVISIBLE);
//                    mIsRecording = false;
//                    mIsTimelapse = false;
//                    mRecordImageButton.setImageResource(R.mipmap.btn_video_online);
//
//                    // Starting the preview prior to stopping recording which should hopefully
//                    // resolve issues being seen in Samsung devices.
//                    startPreview();
//                    mMediaRecorder.stop();
//                    mMediaRecorder.reset();
//
//                    Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                    mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mVideoFileName)));
//                    sendBroadcast(mediaStoreUpdateIntent);
//
//                } else {
//                    mIsRecording = true;
//                    mRecordImageButton.setImageResource(R.mipmap.btn_video_busy);
//                    checkWriteStoragePermission();
//                }
//            }
//        });
//        mRecordImageButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                mIsTimelapse =true;
//                mRecordImageButton.setImageResource(R.mipmap.btn_timelapse);
//                checkWriteStoragePermission();
//                return true;
//            }
//        });
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        startBackgroundThread();
//
//        if(mTextureView.isAvailable()) {
//            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
//            connectCamera();
//        } else {
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
//            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getApplicationContext(),
//                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
//            }
//            if(grantResults[1] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getApplicationContext(),
//                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
//            }
//        }
//        if(requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
//            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                if(mIsRecording || mIsTimelapse) {
//                    mIsRecording = true;
//                    mRecordImageButton.setImageResource(R.mipmap.btn_video_busy);
//                }
//                Toast.makeText(this,
//                        "Permission successfully granted!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this,
//                        "App needs to save video to run", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        closeCamera();
//
//        stopBackgroundThread();
//
//        super.onPause();
//    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocas) {
//        super.onWindowFocusChanged(hasFocas);
//        View decorView = getWindow().getDecorView();
//        if(hasFocas) {
//            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
//    }
//
//    private void setupCamera(int width, int height) {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            for(String cameraId : cameraManager.getCameraIdList()){
//                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
//                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
//                        CameraCharacteristics.LENS_FACING_FRONT){
//                    continue;
//                }
//                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
//                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
//                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
//                int rotatedWidth = width;
//                int rotatedHeight = height;
//                if(swapRotation) {
//                    rotatedWidth = height;
//                    rotatedHeight = width;
//                }
//                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
//                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
//                mImageSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
//                mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
//                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
//                mCameraId = cameraId;
//                return;
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void connectCamera() {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
//                        PackageManager.PERMISSION_GRANTED) {
//                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
//                } else {
//                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
//                        Toast.makeText(this,
//                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
//                    }
//                    requestPermissions(new String[] {android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
//                    }, REQUEST_CAMERA_PERMISSION_RESULT);
//                }
//
//            } else {
//                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void startRecord() {
//
//        try {
//            if(mIsRecording) {
//                setupMediaRecorder();
//            } else if(mIsTimelapse) {
//                setupTimelapse();
//            }
//            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            Surface previewSurface = new Surface(surfaceTexture);
//            Surface recordSurface = mMediaRecorder.getSurface();
//            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//            mCaptureRequestBuilder.addTarget(previewSurface);
//            mCaptureRequestBuilder.addTarget(recordSurface);
//
//            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface, mImageReader.getSurface()),
//                    new CameraCaptureSession.StateCallback() {
//                        @Override
//                        public void onConfigured(CameraCaptureSession session) {
//                            mRecordCaptureSession = session;
//                            try {
//                                mRecordCaptureSession.setRepeatingRequest(
//                                        mCaptureRequestBuilder.build(), null, null
//                                );
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(CameraCaptureSession session) {
//                            Log.d(TAG, "onConfigureFailed: startRecord");
//                        }
//                    }, null);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void startPreview() {
//        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        Surface previewSurface = new Surface(surfaceTexture);
//
//        try {
//            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mCaptureRequestBuilder.addTarget(previewSurface);
//
//            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
//                    new CameraCaptureSession.StateCallback() {
//                        @Override
//                        public void onConfigured(CameraCaptureSession session) {
//                            Log.d(TAG, "onConfigured: startPreview");
//                            mPreviewCaptureSession = session;
//                            try {
//                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
//                                        null, mBackgroundHandler);
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(CameraCaptureSession session) {
//                            Log.d(TAG, "onConfigureFailed: startPreview");
//
//                        }
//                    }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void startStillCaptureRequest() {
//        try {
//            if(mIsRecording) {
//                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
//            } else {
//                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            }
//            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
//            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);
//
//            CameraCaptureSession.CaptureCallback stillCaptureCallback = new
//                    CameraCaptureSession.CaptureCallback() {
//                        @Override
//                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
//                            super.onCaptureStarted(session, request, timestamp, frameNumber);
//
//                            try {
//                                createImageFileName();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    };
//
//            if(mIsRecording) {
//                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
//            } else {
//                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void closeCamera() {
//        if(mCameraDevice != null) {
//            mCameraDevice.close();
//            mCameraDevice = null;
//        }
//        if(mMediaRecorder != null) {
//            mMediaRecorder.release();
//            mMediaRecorder = null;
//        }
//    }
//
//    private void startBackgroundThread() {
//        mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
//        mBackgroundHandlerThread.start();
//        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
//    }
//
//    private void stopBackgroundThread() {
//        mBackgroundHandlerThread.quitSafely();
//        try {
//            mBackgroundHandlerThread.join();
//            mBackgroundHandlerThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
//        int sensorOrienatation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
//        return (sensorOrienatation + deviceOrientation + 360) % 360;
//    }
//
//    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
//        List<Size> bigEnough = new ArrayList<Size>();
//        for(Size option : choices) {
//            if(option.getHeight() == option.getWidth() * height / width &&
//                    option.getWidth() >= width && option.getHeight() >= height) {
//                bigEnough.add(option);
//            }
//        }
//        if(bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new CompareSizeByArea());
//        } else {
//            return choices[0];
//        }
//    }
//
//    private void createVideoFolder() {
//        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//        mVideoFolder = new File(movieFile, "camera2VideoImage");
//        if(!mVideoFolder.exists()) {
//            mVideoFolder.mkdirs();
//        }
//    }
//
//    private File createVideoFileName() throws IOException {
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String prepend = "VIDEO_" + timestamp + "_";
//        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
//        mVideoFileName = videoFile.getAbsolutePath();
//        return videoFile;
//    }
//
//    private void createImageFolder() {
//        File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        mImageFolder = new File(imageFile, "camera2VideoImage");
//        if(!mImageFolder.exists()) {
//            mImageFolder.mkdirs();
//        }
//    }
//
//    private File createImageFileName() throws IOException {
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String prepend = "IMAGE_" + timestamp + "_";
//        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
//        mImageFileName = imageFile.getAbsolutePath();
//        return imageFile;
//    }
//
//    private void checkWriteStoragePermission() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if(mIsTimelapse || mIsRecording) {
//                    startRecord();
//                    mMediaRecorder.start();
//                    mChronometer.setBase(SystemClock.elapsedRealtime());
//                    mChronometer.setVisibility(View.VISIBLE);
//                    mChronometer.start();
//                }
//            } else {
//                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
//                }
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
//            }
//        } else {
//            try {
//                createVideoFileName();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if(mIsRecording || mIsTimelapse) {
//                startRecord();
//                mMediaRecorder.start();
//                mChronometer.setBase(SystemClock.elapsedRealtime());
//                mChronometer.setVisibility(View.VISIBLE);
//                mChronometer.start();
//            }
//        }
//    }
//
//    private void setupMediaRecorder() throws IOException {
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setOutputFile(mVideoFileName);
//        mMediaRecorder.setVideoEncodingBitRate(1000000);
//        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mMediaRecorder.setOrientationHint(mTotalRotation);
//        mMediaRecorder.prepare();
//    }
//
//    private void setupTimelapse() throws IOException {
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
//        mMediaRecorder.setOutputFile(mVideoFileName);
//        mMediaRecorder.setCaptureRate(2);
//        mMediaRecorder.setOrientationHint(mTotalRotation);
//        mMediaRecorder.prepare();
//    }
//
//    private void lockFocus() {
//        mCaptureState = STATE_WAIT_LOCK;
//        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
//        try {
//            if(mIsRecording) {
//                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), mRecordCaptureCallback, mBackgroundHandler);
//            } else {
//                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), mPreviewCaptureCallback, mBackgroundHandler);
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

}
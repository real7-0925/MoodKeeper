 package com.example.a0324new;

 import androidx.annotation.Nullable;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.content.FileProvider;

 import android.Manifest;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.res.ColorStateList;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Picture;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;

//import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
 import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
 import com.amazonaws.services.rekognition.model.DetectLabelsResult;
 import com.amazonaws.services.rekognition.model.Label;
 import com.amazonaws.util.IOUtils;
 import com.amplifyframework.AmplifyException;
//import com.amplifyframework.auth.AuthUserAttributeKey;
//import com.amplifyframework.auth.options.AuthSignUpOptions;
 import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
 import com.amplifyframework.core.Amplify;

 import com.amazonaws.services.rekognition.AmazonRekognition;
//import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
//import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
 import com.amazonaws.services.rekognition.model.Image;
 import com.amazonaws.services.rekognition.model.S3Object;
 import com.amazonaws.services.rekognition.model.AgeRange;
 import com.amazonaws.services.rekognition.model.Attribute;
 import com.amazonaws.services.rekognition.model.DetectFacesRequest;
 import com.amazonaws.services.rekognition.model.DetectFacesResult;
 import com.amazonaws.services.rekognition.model.FaceDetail;
 import com.amplifyframework.AmplifyException;
 import com.amplifyframework.core.Amplify;
 import com.amplifyframework.core.AmplifyConfiguration;
 import com.amplifyframework.predictions.aws.AWSPredictionsPlugin;
 import com.amplifyframework.predictions.aws.configuration.IdentifyEntitiesConfiguration;
 import com.amplifyframework.predictions.models.EntityDetails;
 import com.amplifyframework.predictions.models.EntityMatch;
 import com.amplifyframework.predictions.models.IdentifyActionType;
 import com.amplifyframework.predictions.models.LabelType;
 import com.amplifyframework.predictions.result.IdentifyEntitiesResult;
 import com.amplifyframework.predictions.result.IdentifyEntityMatchesResult;
 import com.amplifyframework.predictions.result.IdentifyLabelsResult;
 import com.amplifyframework.storage.options.StorageDownloadFileOptions;
 import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
 import com.bumptech.glide.Glide;
 import com.google.android.material.slider.Slider;
//import com.fasterxml.jackson.databind.ObjectMapper;

 import java.io.BufferedWriter;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.List;

//import com.amazonaws.services.rekognition.AmazonRekognition;
//import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
//import com.amazonaws.services.rekognition.model.Image;
//import com.amazonaws.services.rekognition.model.S3Object;
//import com.amazonaws.services.rekognition.model.AgeRange;
//import com.amazonaws.services.rekognition.model.Attribute;
//import com.amazonaws.services.rekognition.model.DetectFacesRequest;
//import com.amazonaws.services.rekognition.model.DetectFacesResult;
//import com.amazonaws.services.rekognition.model.FaceDetail;
//import com.fasterxml.jackson.databind.ObjectMapper;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;

 import static android.app.Service.START_STICKY;




 public class MainActivity extends AppCompatActivity {

     private static final String TAG = "Faces";

     private String mPath = "/storage/emulated/0/Detectface";//?????????????????????
     public static final int CAMERA_PERMISSION = 100;//?????????????????????
     public static final int REQUEST_HIGH_IMAGE = 101;//??????????????????

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);

         try {
             Amplify.addPlugin(new AWSCognitoAuthPlugin());//without credential log in
             Amplify.addPlugin(new AWSPredictionsPlugin());//rekognition translate polly high level client

             AmplifyConfiguration config = AmplifyConfiguration.builder(getApplicationContext())
                     .devMenuEnabled(false)
                     .build();
             Amplify.configure(config, getApplicationContext());
         } catch (AmplifyException e) {
             Log.e("Tutorial", "Could not initialize Amplify", e);
         }

         //create DetectFace folder in android for picture
         String DetectFacedir = "/DetectFace/";
         File PrimaryStorage = Environment.getExternalStorageDirectory();
         File PICDir = new File("/storage/emulated/0/DetectFace/");
         File ReadyPath = new File("/storage/emulated/0/DetectFace/" + "Ready.txt");
         Log.e("str", String.valueOf(PrimaryStorage));
         try {
             Log.i("test", "delete CMD");
             String deleteCmd = "rm -r " + ReadyPath;
             Runtime runtime = Runtime.getRuntime();
             runtime.exec(deleteCmd);


         } catch (FileNotFoundException e) {
             Log.e("NOTFOUND", "file notfound");
         } catch (IOException e) {
             Log.e("IOERROR", "some IO error");
         }

         Task task = new Task();

         ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(0);
         executor.scheduleWithFixedDelay(task, 1, 300, TimeUnit.SECONDS);

         //??????
         Button btHigh = findViewById(R.id.buttonHigh);
         //??????????????????
         if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
             requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
         /**???????????????????????????*/
         btHigh.setOnClickListener(v->{
             Intent highIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             //???????????????????????????
             if (highIntent.resolveActivity(getPackageManager()) == null) return;
             //?????????????????????URI???????????????????????????
             File imageFile = getImageFile();
             if (imageFile == null) return;
             //?????????????????????URI??????
             Uri imageUri = FileProvider.getUriForFile(
                     this,
                     "com.jetec.cameraexample.CameraEx",//????????????AndroidManifest.xml??????authorities ??????
                     imageFile
             );
             highIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
             startActivityForResult(highIntent,REQUEST_HIGH_IMAGE);//????????????
         });
     }
     /**?????????????????????URI???????????????????????????*/
     private File getImageFile()  {
         Date date = new Date();

         SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
         String time = sdf.format(date);
         String fileName = time;
         File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
         File f = new File(mPath);
         try {
             //?????????????????????????????????
             File imageFile = File.createTempFile(fileName,".jpg", f);
             //???????????????????????????????????????????????????????????????
             Log.e("aa", "imageFile = " + imageFile.getAbsolutePath());
             mPath = imageFile.getAbsolutePath();
             return imageFile;
         } catch (IOException e) {
             return null;
         }
     }

     /**??????????????????*/
     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         /**???????????????????????????????????????requestCode?????????????????????resultCode???-1??????????????????0????????????????????????*/
         Log.d(TAG, "onActivityResult: requestCode: "+requestCode+", resultCode "+resultCode);
         /**????????????????????????*/
         if (requestCode == REQUEST_HIGH_IMAGE && resultCode == -1) {
             ImageView imageHigh = findViewById(R.id.imageViewHigh);
             new Thread(() -> {
                 //???BitmapFactory????????????URI???????????????????????????????????????AtomicReference<Bitmap>???????????????????????????
                 AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                 Matrix matrix = new Matrix();
                 matrix.setRotate(90f);//???90???
                 getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                         , 0, 0
                         , getHighImage.get().getWidth()
                         , getHighImage.get().getHeight()
                         , matrix, true));
                 runOnUiThread(() -> {
                     //???Glide????????????(?????????????????????????????????????????????LAG????????????????????????Thread?????????)
                     Glide.with(this)
                             .load(getHighImage.get())
                             .centerCrop()
                             .into(imageHigh);
                 });
             }).start();
         }
             else{
                 Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
             }



         //??????
         Slider slider1 = (Slider) findViewById(R.id.seekbar_Red);
         Slider slider2 = (Slider) findViewById(R.id.seekbar_Green);
         Slider slider3 = (Slider) findViewById(R.id.seekbar_Blue);

//        slider1.showContextMenu();// ??????????????????????????????
//        slider2.showContextMenu();
//        slider3.showContextMenu();

         slider1.setTrackTintList(ColorStateList.valueOf(0xFF881515));// ????????????
         slider2.setTrackTintList(ColorStateList.valueOf(0xFF308014));
         slider3.setTrackTintList(ColorStateList.valueOf(0xFF3D59AB));

         slider1.setValue(0);// ??????????????????
         slider2.setValue(0);
         slider3.setValue(0);

         slider1.setValueTo(255);// ??????????????????
         slider2.setValueTo(255);
         slider3.setValueTo(255);

         slider1.setHaloTintList(ColorStateList.valueOf(0xFFFF0000));// ??????????????????
         slider2.setHaloTintList(ColorStateList.valueOf(0xFF00FF00));
         slider3.setHaloTintList(ColorStateList.valueOf(0xFF0000FF));

         slider1.setThumbTintList(ColorStateList.valueOf(0xFFFFFAFA));// ??????????????????
         slider2.setThumbTintList(ColorStateList.valueOf(0xFFFFFAFA));
         slider3.setThumbTintList(ColorStateList.valueOf(0xFFFFFAFA));

         slider1.setTrackActiveTintList(ColorStateList.valueOf(0xFFE3170D));//??????????????????????????????
         slider2.setTrackActiveTintList(ColorStateList.valueOf(0xFF32CD32));
         slider3.setTrackActiveTintList(ColorStateList.valueOf(0xFF1E90FF));

         slider1.setTrackHeight(20);//??????????????????
         slider2.setTrackHeight(20);
         slider3.setTrackHeight(20);
//        slider1.setBackgroundColor(getResources().getColor(R.color.red));// ????????????// ????????????
//        slider1.setOnValueChangedListener(new OnValueChangedListener() {
//
//            @Override
//            public void onValueChanged(int value) {
//                // TODO ???????????????????????????
//                System.out.println("now value = "+ value);
//            }
//        });

         slider1.setThumbElevation(30);// ???????????????????????????
         slider2.setThumbElevation(30);// ???????????????????????????
         slider3.setThumbElevation(30);// ???????????????????????????
     }

     public class detectBinder extends Binder {
         public MainActivity getService() {
             return MainActivity.this;
         }
     }

     public int onStartCommand(Intent intent, int flags, int startId) {

         //DriveServiceHelper mDriveServiceHelper = (DriveServiceHelper) intent.getExtras().get("test");
         return START_STICKY;
     }

     @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
     @Override
     public void onDestroy() {
         super.onDestroy();
     }


     class Task implements Runnable {
         public void run() {
             Log.i("test", "run started");
//             File PrimaryStorage = Environment.getExternalStorageDirectory();
//             //Log.e("str", String.valueOf(PrimaryStorage));
//             String Facedir = "/DetectFace/";
//             String ReadyFil = "READY.txt";
             File imageFile = new File("/storage/emulated/0/Detectface/yyMMdd.jpeg");
             imageFile.mkdir();
//             //Log.i("test","create file");
//             //File imageFile = new File(System.currentTimeMillis() + ".jpg");
//             File ReadyPath = new File("/storage/emulated/0/Detectface2/" + ReadyFil);


//            File file = new File(ReadyPath, ReadyFil);
//            FileOutputStream outputStream = null;
//            try {
//                outputStream = new FileOutputStream(ReadyFil);
//                outputStream.write("0".getBytes());
//                outputStream.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
             //if (ReadyPath.exists()) {
             //Log.e("try","ReadyPath exists");
//                try {
//                    String deleteCmd = "rm -r " + ReadyPath;
//                    Runtime runtime = Runtime.getRuntime();
//                    runtime.exec(deleteCmd);
//
//                } catch (FileNotFoundException e) {
//                    Log.e("NOTFOUND", "file notfound");
//                } catch (IOException e) {
//                    Log.e("IOERROR", "some IO error");
//                }


             try {
                 Log.i("try", "DetectEntities");
                 BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                 Bitmap image = BitmapFactory.decodeFile(String.valueOf(imageFile), bmOptions); //???????????????jpg
                 DetectEntities(image); //??????????????????AWS
             } catch (Exception e) {
                 Log.e("DETECT", "detect error" + e.getMessage());
             }
             //}
         }
     }


     private void DetectEntities(Bitmap image) {
         try {
             Log.i("DETECTENTITIES", "started");

             Amplify.Predictions.identify(  //????????????????????????
                     IdentifyActionType.DETECT_ENTITIES,
                     image,
                     result -> LabelDataHold((IdentifyEntitiesResult) result, image),
                     error -> Log.e("AmplifyQuickstart", "Identify failed ", error)// + error.getMessage())
             );
             Log.i("DETECTENTITIES", "finished");

         } catch (Exception e) {
             Log.e("DETECT", "DetectEntities error "); //+ e.getMessage());
         }
     }


     private void LabelDataHold(IdentifyEntitiesResult result, Bitmap image) {
         final String[] printout = new String[result.getEntities().size()];
         double[][] Xnumber = new double[result.getEntities().size()][];
         int max = result.getEntities().size();

         for (int m = 0; m < max; m++) {
             printout[m] = String.valueOf(result.getEntities().get(m).getEmotions().get(m).getValue());
             printout[m] = String.valueOf(result.getEntities().get(m).getBox());
             printout[m] = String.valueOf(result.getEntities().get(m).getAgeRange());
             printout[m] = String.valueOf(result.getEntities().get(m).getGender());
             printout[m] = String.valueOf(result.getEntities().get(m).getLandmarks());
             printout[m] = String.valueOf(result.getEntities().get(m).getPolygon());
             printout[m] = String.valueOf(result.getEntities().get(m).getPose());


             //result.getEntities().get(0).getAgeRange().getLow();

             //Log.i("result", result.toString());
             Log.i("Emotions  Result", result.getEntities().get(m).getEmotions().get(m).getValue()
                     + ", Confidence: " + result.getEntities().get(m).getEmotions().get(m).getConfidence());

             Log.i("AgeRange  Result", "Age: " + result.getEntities().get(0).getAgeRange().getLow()
                     + " - " + result.getEntities().get(0).getAgeRange().getHigh());

             Log.i("Gender    Result", result.getEntities().get(0).getGender().getValue()
                     + ", Confidence: " + result.getEntities().get(0).getGender().getConfidence());

//         Log.i("Try           Result", result.getEntities().get(0).
//                 + ", Confidence: " + result.getEntities().get(0).getEmotions().get(0).getConfidence());

             //Log.i("Landmarks Result", String.valueOf(result.getEntities().get(0).getLandmarks()));
         }

     }
 }



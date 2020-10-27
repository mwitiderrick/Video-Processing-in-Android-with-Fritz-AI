package com.namespace.labs;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import ai.fritz.core.Fritz;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionModels;
import ai.fritz.vision.ModelVariant;
import ai.fritz.vision.imagesegmentation.FritzVisionSegmentationPredictor;
import ai.fritz.vision.imagesegmentation.MaskClass;
import ai.fritz.vision.imagesegmentation.SegmentationOnDeviceModel;
import ai.fritz.vision.styletransfer.FritzVisionStylePredictor;
import ai.fritz.vision.styletransfer.PatternStyleModels;
import ai.fritz.vision.video.FritzVisionImageFilter;
import ai.fritz.vision.video.FritzVisionVideo;
import ai.fritz.vision.video.filters.StylizeImageCompoundFilter;
import ai.fritz.vision.video.filters.imagesegmentation.MaskOverlayFilter;
import android.widget.MediaController;
import android.net.Uri;
import android.widget.VideoView;
import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity{
  VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fritz.configure(this, "YOUR_API_KEY");
        setContentView(R.layout.activity_main);

        Uri uri = Uri.parse("https://namespacelabs.com/ml/fritz.mp4");
        VideoView videoView = (VideoView) findViewById(R.id.videoView);

        PatternStyleModels patternModels = FritzVisionModels.getPatternStyleModels();
        FritzOnDeviceModel styleOnDeviceModel = patternModels.getComic();

        FritzVisionStylePredictor stylePredictor = FritzVision.StyleTransfer.getPredictor(styleOnDeviceModel);

        SegmentationOnDeviceModel peopleModel = FritzVisionModels.getPeopleSegmentationOnDeviceModel(ModelVariant.FAST);

        FritzVisionSegmentationPredictor peoplePredictor = FritzVision.ImageSegmentation.getPredictor(peopleModel);

        FritzVisionImageFilter[] filters = new FritzVisionImageFilter[]{
                new StylizeImageCompoundFilter(stylePredictor),
                new MaskOverlayFilter(peoplePredictor, MaskClass.PERSON)
        };

        FritzVisionVideo visionVideo = new FritzVisionVideo(uri, filters);


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(1, 1);
                mediaPlayer.start();
            }
        });


        File exportFile = null;
        try {
            exportFile = File.createTempFile("export",".mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File finalExportFile = exportFile;
        visionVideo.export(exportFile.getAbsolutePath(), new FritzVisionVideo.ExportProgressCallback() {
            @Override
            public void onProgress(Float response) {
                // Show updated progress.
            }

            @Override
            public void onComplete() {
                // when the file has completed, add the video to the view to play it.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = Uri.fromFile(finalExportFile);
                        videoView.setVideoURI(uri);
                    }
                });
            }
        });
        MediaController mediaController = new MediaController(this);

        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);

    }
}
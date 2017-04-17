package io.fotoapparat.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.log.LogcatLogger;
import io.fotoapparat.photo.BitmapPhoto;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.result.PendingResult;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.extender.ObservableExtender;
import io.fotoapparat.view.CameraView;
import rx.functions.Action1;

import static io.fotoapparat.parameter.selector.AspectRatioSelectors.standardRatio;
import static io.fotoapparat.parameter.selector.FlashSelectors.autoRedEye;
import static io.fotoapparat.parameter.selector.FlashSelectors.torch;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.auto;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.back;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;
import static io.fotoapparat.parameter.selector.SizeSelectors.biggestSize;

public class MainActivity extends AppCompatActivity {

	private Fotoapparat fotoapparat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		CameraView cameraView = (CameraView) findViewById(R.id.camera_view);

		fotoapparat = Fotoapparat
				.with(this)
				.into(cameraView)
				.photoSize(biggestSize(standardRatio()))
				.lensPosition(back())
				.focusMode(firstAvailable(
						continuousFocus(),
						auto(),
						fixed()
				))
				.flash(firstAvailable(
						autoRedEye(),
						auto(),
						torch()
				))
				.frameProcessor(new SampleFrameProcessor())
				.logger(new LogcatLogger())
				.build();

		cameraView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				takePicture();
			}
		});
	}

	private void takePicture() {

		PhotoResult photoResult = fotoapparat.takePicture();

		photoResult.saveToFile(new File(
				getExternalFilesDir("photos"),
				"photo.jpg"
		));

		photoResult
				.toBitmap()
				.whenAvailable(new PendingResult.Callback<BitmapPhoto>() {
					@Override
					public void onResult(BitmapPhoto result) {
						ImageView imageView = (ImageView) findViewById(R.id.result);

						imageView.setImageBitmap(result.bitmap);
						imageView.setRotation(-result.rotationDegrees);
					}
				});
		photoResult
				.toBitmap()
				.extend(ObservableExtender.<BitmapPhoto>observableExtender())
				.subscribe(new Action1<BitmapPhoto>() {
					@Override
					public void call(BitmapPhoto bitmapPhoto) {

					}
				});

	}

	@Override
	protected void onResume() {
		super.onResume();

		fotoapparat.start();
	}

	@Override
	protected void onPause() {
		super.onPause();

		fotoapparat.stop();
	}

	private class SampleFrameProcessor implements FrameProcessor {

		@Override
		public void processFrame(Frame frame) {
			// Do nothing
		}

	}

}

[![](https://jitpack.io/v/sminrana/exosimplify.svg)](https://jitpack.io/#sminrana/exosimplify)


# ExoSimplify Video Player for Android

ExoSimplify is a background video player with full screen and notification support. 


### Installation

1. Add it in your root build.gradle at the end of repositories:



```gradle
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```

2. Add the dependency

```gradle
dependencies {
	implementation 'com.github.sminrana:ExoSimplify:0.9.2'
}
```

--- 

### Example

Extends SimplifyVideoActivity and create a new activity. 
```java
import com.sminrana.exosimplify.ui.SimplifyVideoActivity;

public class VideoPlayerActivity extends SimplifyVideoActivity {

    @Override
    public void onAppKill() {
        super.onAppKill();
    }
}

```

Update your AndroidMenifest.xml 

```xml
<activity
   android:name=".VideoPlayerActivity"
   android:configChanges="orientation|keyboardHidden|screenSize"
   android:excludeFromRecents="true"
   android:launchMode="singleTask"
   android:exported="true"
   android:screenOrientation="fullSensor"
   android:theme="@style/ExoSimplify.Fullscreen">
</activity>

```

Now call the VideoPlayerActivity

```java
Intent intent = new Intent(getContext(), VideoPlayerActivity.class);
intent.putExtra("title", "Demo Video");

// Make sure video URL is valid
// this one has no sound
intent.putExtra("url", "https://www.shutterstock.com/shutterstock/videos/1094984573/preview/stock-footage-zombie-hand-rising-up-smartphone-with-green-screen-out-of-grave-holiday-event-halloween-concept.mp4");
startActivity(intent);
```

You must add an icon (ic_notification_icon.png) for the notification in your app drawable directory.

---

**See more in the demo application (app folder)**

#### Video of working App

[<img src="https://raw.githubusercontent.com/sminrana/ExoSimplify/main/app/video_cover.png" width="50%">](https://raw.githubusercontent.com/sminrana/ExoSimplify/main/app/video.mp4 "ExoSimplify")

Inspired by https://github.com/muslimtv/flutter_playout.


## Contributing

You're contribution is welcome here!

If you find this library useful, share with friend or star it. 



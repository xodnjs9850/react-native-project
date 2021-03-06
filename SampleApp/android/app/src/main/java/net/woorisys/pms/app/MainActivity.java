package net.woorisys.pms.app;

import android.os.Bundle;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;

public class MainActivity extends ReactActivity {

  private static MainActivity mInstance = null;

  public boolean isOnNewIntent = false;

  // @Override
  // public void onConfigurationChanged(Configuration newConfig) {
  //     super.onConfigurationChanged(newConfig);
  //     Intent intent = new Intent("onConfigurationChanged");
  //     intent.putExtra("newConfig", newConfig);
  //     this.sendBroadcast(intent);
  // }

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "SmartParking";
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      //SplashScreen.show(this, true);  // here
      super.onCreate(savedInstanceState);

      mInstance = this;
  }

  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new ReactActivityDelegate(this, getMainComponentName()) {
      @Override
      protected ReactRootView createRootView() {
        return new RNGestureHandlerEnabledRootView(MainActivity.this);
      }
    };
  }

  public static MainActivity  getInstance() {
    return mInstance;
  }

}

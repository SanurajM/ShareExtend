package com.zt.shareextend;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.PluginRegistry; // ✅ <-- ADD THIS LINE

/**
 * Plugin method host for presenting a share sheet via Intent
 */
public class ShareExtendPlugin implements FlutterPlugin, ActivityAware, PluginRegistry.RequestPermissionsResultListener {

    /// the authorities for FileProvider
    private static final int CODE_ASK_PERMISSION = 100;
    private static final String CHANNEL = "com.zt.shareextend/share_extend";

    private FlutterPluginBinding pluginBinding;
    private ActivityPluginBinding activityBinding;

    private MethodChannel methodChannel;
    private MethodCallHandlerImpl callHandler;
    private Share share;

    // v2 embedding doesn't require the registerWith method anymore
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        pluginBinding = flutterPluginBinding;
        // Set up the method channel using the v2 embedding binding
        setUpChannel(pluginBinding.getApplicationContext(), pluginBinding.getBinaryMessenger(), null, null);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        pluginBinding = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        activityBinding = activityPluginBinding;
        // Set up the method channel using the v2 embedding activity binding
        setUpChannel(activityBinding.getActivity(), pluginBinding.getBinaryMessenger(), null, activityBinding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        onAttachedToActivity(activityPluginBinding);
    }

    @Override
    public void onDetachedFromActivity() {
        tearDown();
    }

    private void setUpChannel(Context context, BinaryMessenger messenger, PluginRegistry.Registrar registrar, ActivityPluginBinding activityBinding) {
        methodChannel = new MethodChannel(messenger, CHANNEL);
        share = new Share(context);
        callHandler = new MethodCallHandlerImpl(share);
        methodChannel.setMethodCallHandler(callHandler);
        // Permission listeners handled by v2 embedding
        if (activityBinding != null) {
            activityBinding.addRequestPermissionsResultListener(this);
        }
    }

    private void tearDown() {
        // Clean up the listeners and method channel
        if (activityBinding != null) {
            activityBinding.removeRequestPermissionsResultListener(this);
        }
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        activityBinding = null;
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] perms, int[] grantResults) {
        if (requestCode == CODE_ASK_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            share.share();
        }
        return false;
    }
}

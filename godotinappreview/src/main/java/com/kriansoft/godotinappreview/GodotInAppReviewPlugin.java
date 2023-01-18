package com.kriansoft.godotinappreview;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;

import java.util.Set;

public class GodotInAppReviewPlugin extends GodotPlugin {

    final ReviewManager reviewManager;
    final Activity activity;

    public GodotInAppReviewPlugin(Godot godot) {
        super(godot);
        activity = getActivity();
        reviewManager = ReviewManagerFactory.create(activity);
    }

    @UsedByGodot
    public void requestReview() {
        final Task<ReviewInfo> request = reviewManager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                final ReviewInfo reviewInfo = task.getResult();
                emitSignal("review_info_request_success", reviewInfo);
                final Task<Void> flow = reviewManager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(launchTask -> {
                    emitSignal("review_completed");
                });
            } else {
                emitSignal("review_info_request_failed");
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotInAppReview";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        final Set<SignalInfo> signals = new ArraySet<>();
        signals.add(new SignalInfo("review_info_request_success", ReviewInfo.class));
        signals.add(new SignalInfo("review_info_request_failed"));
        signals.add(new SignalInfo("review_completed"));
        return signals;
    }
}

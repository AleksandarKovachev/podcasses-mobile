package com.podcasses.util;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import com.podcasses.R;
import com.podcasses.model.entity.Podcast;
import com.podcasses.model.response.Comment;
import com.podcasses.retrofit.ApiCallInterface;

public class PopupMenuUtil {

    @SuppressLint("RestrictedApi")
    public static void podcastPopupMenu(View view, Podcast podcast, ApiCallInterface apiCallInterface, String token) {
        PopupMenu popupOptions = new PopupMenu(view.getContext(), view);
        popupOptions.getMenuInflater()
                .inflate(R.menu.podcast_options_menu, popupOptions.getMenu());
        MenuPopupHelper menuHelper = new MenuPopupHelper(view.getContext(), (MenuBuilder) popupOptions.getMenu(), view);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END);
        popupOptions.getMenu().getItem(0).setChecked(podcast.isMarkAsPlayed());
        popupOptions.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.markAsPlayed:
                    NetworkRequestsUtil.sendMarkAsPlayedRequest(item, view.getContext(), apiCallInterface, podcast, token);
                    break;
                case R.id.report:
                    DialogUtil.createReportDialog(view.getContext(), podcast.getId(), apiCallInterface, token, true);
                    break;
            }
            return true;
        });
        menuHelper.show();
    }

    @SuppressLint("RestrictedApi")
    public static void commentPopupMenu(View view, Comment comment, ApiCallInterface apiCallInterface, String token) {
        PopupMenu popupOptions = new PopupMenu(view.getContext(), view);
        popupOptions.getMenuInflater()
                .inflate(R.menu.comment_options_menu, popupOptions.getMenu());
        MenuPopupHelper menuHelper = new MenuPopupHelper(view.getContext(), (MenuBuilder) popupOptions.getMenu(), view);
        menuHelper.setForceShowIcon(true);
        menuHelper.setGravity(Gravity.END);
        popupOptions.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.report) {
                DialogUtil.createReportDialog(view.getContext(), comment.getId(), apiCallInterface, token, false);
            }
            return true;
        });
        menuHelper.show();
    }

}

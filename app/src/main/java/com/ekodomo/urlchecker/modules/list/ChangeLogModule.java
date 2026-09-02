package com.ekodomo.urlchecker.modules.list;

import android.view.View;
import android.widget.TextView;

import com.ekodomo.urlchecker.BuildConfig;
import com.ekodomo.urlchecker.R;
import com.ekodomo.urlchecker.activities.ModulesActivity;
import com.ekodomo.urlchecker.dialogs.MainDialog;
import com.ekodomo.urlchecker.modules.AModuleConfig;
import com.ekodomo.urlchecker.modules.AModuleData;
import com.ekodomo.urlchecker.modules.AModuleDialog;
import com.ekodomo.urlchecker.modules.DescriptionConfig;
import com.ekodomo.urlchecker.modules.companions.VersionManager;
import com.ekodomo.urlchecker.utilities.methods.AndroidUtils;

/** This module will show a message if the app was updated. */
public class ChangeLogModule extends AModuleData {

    @Override
    public String getId() {
        return "changelog";
    }

    @Override
    public int getName() {
        return R.string.mChg_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new ChangeLogModuleDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new DescriptionConfig(R.string.mChg_desc);
    }
}

class ChangeLogModuleDialog extends AModuleDialog {

    public ChangeLogModuleDialog(MainDialog dialog) {
        super(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_changelog;
    }

    @Override
    public void onInitialize(View views) {
        var versionManager = new VersionManager(getActivity());

        // set visibility
        var updated = versionManager.wasUpdated();
        setVisibility(updated);

        // updated text
        var txt_updated = views.<TextView>findViewById(R.id.updated);
        if (updated) {
            AndroidUtils.setRoundedColor(R.color.good, txt_updated);
        } else {
            txt_updated.setVisibility(View.GONE);
        }

        // version
        views.<TextView>findViewById(R.id.current).setText(getActivity().getString(R.string.mChg_current, BuildConfig.VERSION_NAME));

        // click dismiss to hide
        var dismiss = views.findViewById(R.id.dismiss);
        if (updated) {
            dismiss.setOnClickListener(v -> {
                versionManager.markSeen();

                txt_updated.setVisibility(View.GONE);
                dismiss.setVisibility(View.GONE);
                setVisibility(false);
            });
        } else {
            dismiss.setVisibility(View.GONE);
        }

        // click view to open the changes (the url)
        views.findViewById(R.id.viewChanges).setOnClickListener(v -> {
            // TODO: somehow redirect to the current locale and specific file
            // or, even better, load the changes and show inline (ask the user to get them)
            setUrl("https://github.com/TrianguloY/URLCheck/releases");

            // auto-dismiss
            dismiss.performClick();
        });
    }
}